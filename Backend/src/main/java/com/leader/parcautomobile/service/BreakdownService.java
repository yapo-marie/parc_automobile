package com.leader.parcautomobile.service;

import com.leader.parcautomobile.dto.breakdown.BreakdownPageResponse;
import com.leader.parcautomobile.dto.breakdown.BreakdownResponse;
import com.leader.parcautomobile.dto.breakdown.CreateBreakdownRequest;
import com.leader.parcautomobile.dto.breakdown.ResolveBreakdownRequest;
import com.leader.parcautomobile.entity.Breakdown;
import com.leader.parcautomobile.entity.BreakdownStatus;
import com.leader.parcautomobile.entity.User;
import com.leader.parcautomobile.entity.Vehicle;
import com.leader.parcautomobile.entity.VehicleAvailability;
import com.leader.parcautomobile.entity.VehicleRecordStatus;
import com.leader.parcautomobile.exception.ResourceNotFoundException;
import com.leader.parcautomobile.mapper.BreakdownMapper;
import com.leader.parcautomobile.repository.BreakdownRepository;
import com.leader.parcautomobile.repository.BreakdownSpecifications;
import com.leader.parcautomobile.repository.UserRepository;
import com.leader.parcautomobile.repository.VehicleRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BreakdownService {

	private final BreakdownRepository breakdownRepository;
	private final VehicleRepository vehicleRepository;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public BreakdownPageResponse listAll(
			UUID vehicleId,
			BreakdownStatus status,
			Pageable pageable) {
		Specification<Breakdown> spec = Specification.allOf(
				BreakdownSpecifications.vehicleId(vehicleId),
				BreakdownSpecifications.hasStatus(status));
		Page<Breakdown> page = breakdownRepository.findAll(spec, pageable);
		List<BreakdownResponse> content =
				page.getContent().stream().map(BreakdownMapper::toResponse).toList();
		return new BreakdownPageResponse(
				content,
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages());
	}

	@Transactional
	public BreakdownResponse create(String declaredByEmail, CreateBreakdownRequest body) {
		User declaredBy = userRepository
				.findByEmailWithRoles(declaredByEmail)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
		if (declaredBy.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Utilisateur introuvable");
		}

		Vehicle vehicle = vehicleRepository
				.findById(body.vehicleId())
				.orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable"));
		if (vehicle.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Véhicule introuvable");
		}
		if (vehicle.getStatus() != VehicleRecordStatus.ACTIVE) {
			throw new IllegalArgumentException("Ce véhicule n'est pas actif");
		}

		Breakdown b = Breakdown.builder()
				.vehicle(vehicle)
				.declaredBy(declaredBy)
				.description(body.description().trim())
				.mileageAtBreakdown(body.mileageAtBreakdown())
				.garage(trimToNull(body.garage()))
				.repairCost(body.repairCost())
				.status(BreakdownStatus.DECLAREE)
				.build();
		Breakdown saved = breakdownRepository.save(b);

		if (vehicle.getAvailability() != VehicleAvailability.OUT_OF_SERVICE) {
			vehicle.setAvailability(VehicleAvailability.IN_REPAIR);
			vehicleRepository.save(vehicle);
		}

		return BreakdownMapper.toResponse(saved);
	}

	@Transactional
	public BreakdownResponse resolve(UUID id, ResolveBreakdownRequest body) {
		Breakdown b = breakdownRepository
				.findByIdFetched(id)
				.orElseThrow(() -> new ResourceNotFoundException("Panne introuvable"));
		if (b.getStatus() != BreakdownStatus.DECLAREE) {
			throw new IllegalArgumentException("Cette panne n'est pas déclarée");
		}
		Instant resolvedAt = body.resolvedAt() != null ? body.resolvedAt() : Instant.now();
		b.setResolvedAt(resolvedAt);
		b.setStatus(BreakdownStatus.RESOLUE);
		Breakdown saved = breakdownRepository.save(b);

		Vehicle v = saved.getVehicle();
		if (v.getDeletedAt() == null
				&& v.getStatus() == VehicleRecordStatus.ACTIVE
				&& v.getAvailability() != VehicleAvailability.OUT_OF_SERVICE) {
			v.setAvailability(VehicleAvailability.AVAILABLE);
			vehicleRepository.save(v);
		}

		return BreakdownMapper.toResponse(saved);
	}

	private static String trimToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}

