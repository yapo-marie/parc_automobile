package com.leader.parcautomobile.service;

import com.leader.parcautomobile.dto.fleet.CreateFleetVehicleRequest;
import com.leader.parcautomobile.dto.fleet.FleetStatsResponse;
import com.leader.parcautomobile.dto.fleet.FleetVehiclePageResponse;
import com.leader.parcautomobile.dto.fleet.FleetVehicleResponse;
import com.leader.parcautomobile.dto.fleet.UpdateFleetVehicleRequest;
import com.leader.parcautomobile.entity.FleetVehicle;
import com.leader.parcautomobile.entity.Vehicle;
import com.leader.parcautomobile.entity.VehicleAvailability;
import com.leader.parcautomobile.entity.VehicleRecordStatus;
import com.leader.parcautomobile.exception.ResourceNotFoundException;
import com.leader.parcautomobile.mapper.FleetVehicleMapper;
import com.leader.parcautomobile.repository.FleetVehicleRepository;
import com.leader.parcautomobile.repository.FleetVehicleSpecifications;
import com.leader.parcautomobile.repository.TechnicalVisitRepository;
import com.leader.parcautomobile.repository.VehicleRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.Year;
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
public class FleetVehicleService {

	private final FleetVehicleRepository fleetVehicleRepository;
	private final VehicleRepository vehicleRepository;
	private final TechnicalVisitRepository technicalVisitRepository;

	@Transactional(readOnly = true)
	public FleetVehiclePageResponse list(String administration, UUID vehicleId, Pageable pageable) {
		Specification<FleetVehicle> spec = Specification.allOf(
				FleetVehicleSpecifications.notDeleted(),
				FleetVehicleSpecifications.administrationContains(administration),
				FleetVehicleSpecifications.vehicleId(vehicleId));
		Page<FleetVehicle> page = fleetVehicleRepository.findAll(spec, pageable);
		List<FleetVehicleResponse> content =
				page.getContent().stream().map(FleetVehicleMapper::toResponse).toList();
		return new FleetVehiclePageResponse(
				content,
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages());
	}

	@Transactional(readOnly = true)
	public FleetVehicleResponse getById(UUID id) {
		FleetVehicle f = fleetVehicleRepository
				.findByIdFetched(id)
				.orElseThrow(() -> new ResourceNotFoundException("Entrée de flotte introuvable"));
		if (f.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Entrée de flotte introuvable");
		}
		return FleetVehicleMapper.toResponse(f);
	}

	@Transactional(readOnly = true)
	public FleetStatsResponse stats() {
		List<FleetVehicle> all =
				fleetVehicleRepository.findAll(Specification.allOf(FleetVehicleSpecifications.notDeleted()));
		long total = all.size();
		BigDecimal sumBudget = all.stream()
				.map(FleetVehicle::getAnnualBudget)
				.filter(b -> b != null)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		long assigned = all.stream()
				.filter(f -> f.getVehicle().getAvailability() == VehicleAvailability.ASSIGNED)
				.count();
		BigDecimal utilPct = total == 0
				? BigDecimal.ZERO
				: BigDecimal.valueOf(100.0 * assigned / total).setScale(1, RoundingMode.HALF_UP);

		List<UUID> vehicleIds = all.stream().map(f -> f.getVehicle().getId()).toList();
		BigDecimal visitSpendYear = BigDecimal.ZERO;
		if (!vehicleIds.isEmpty()) {
			BigDecimal raw = technicalVisitRepository.sumCompletedCostsForVehiclesInYear(
					vehicleIds, Year.now().getValue());
			if (raw != null) {
				visitSpendYear = raw;
			}
		}
		BigDecimal usedPct = BigDecimal.ZERO;
		if (sumBudget.signum() > 0) {
			usedPct = visitSpendYear
					.multiply(BigDecimal.valueOf(100))
					.divide(sumBudget, 1, RoundingMode.HALF_UP);
			if (usedPct.compareTo(BigDecimal.valueOf(100)) > 0) {
				usedPct = BigDecimal.valueOf(100).setScale(1, RoundingMode.HALF_UP);
			}
		}

		return new FleetStatsResponse(total, sumBudget, usedPct, utilPct);
	}

	@Transactional
	public FleetVehicleResponse create(CreateFleetVehicleRequest body) {
		Vehicle vehicle = vehicleRepository
				.findById(body.vehicleId())
				.orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable"));
		if (vehicle.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Véhicule introuvable");
		}
		if (vehicle.getStatus() != VehicleRecordStatus.ACTIVE) {
			throw new IllegalArgumentException("Ce véhicule n'est pas actif");
		}
		if (fleetVehicleRepository.existsByVehicle_IdAndDeletedAtIsNull(vehicle.getId())) {
			throw new IllegalArgumentException("Ce véhicule est déjà enregistré dans la flotte");
		}

		FleetVehicle f = FleetVehicle.builder()
				.vehicle(vehicle)
				.administration(body.administration().trim())
				.dailyCost(body.dailyCost())
				.costPerKm(body.costPerKm())
				.annualBudget(body.annualBudget())
				.startDate(body.startDate())
				.endDate(body.endDate())
				.notes(trimToNull(body.notes()))
				.build();
		FleetVehicle saved = fleetVehicleRepository.save(f);
		return FleetVehicleMapper.toResponse(saved);
	}

	@Transactional
	public FleetVehicleResponse update(UUID id, UpdateFleetVehicleRequest body) {
		FleetVehicle f = fleetVehicleRepository
				.findByIdFetched(id)
				.orElseThrow(() -> new ResourceNotFoundException("Entrée de flotte introuvable"));
		if (f.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Entrée de flotte introuvable");
		}
		FleetVehicleMapper.applyUpdate(f, body);
		fleetVehicleRepository.save(f);
		return FleetVehicleMapper.toResponse(f);
	}

	@Transactional
	public void softDelete(UUID id) {
		FleetVehicle f = fleetVehicleRepository
				.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Entrée de flotte introuvable"));
		if (f.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Entrée de flotte introuvable");
		}
		f.setDeletedAt(Instant.now());
		fleetVehicleRepository.save(f);
	}

	private static String trimToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}
