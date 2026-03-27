package com.leader.parcautomobile.service;

import com.leader.parcautomobile.dto.vehicle.AvailabilityPatchRequest;
import com.leader.parcautomobile.dto.vehicle.CreateVehicleRequest;
import com.leader.parcautomobile.dto.vehicle.UpdateVehicleRequest;
import com.leader.parcautomobile.dto.vehicle.VehiclePageResponse;
import com.leader.parcautomobile.dto.vehicle.VehicleResponse;
import com.leader.parcautomobile.entity.Vehicle;
import com.leader.parcautomobile.entity.VehicleAvailability;
import com.leader.parcautomobile.entity.VehicleCategory;
import com.leader.parcautomobile.entity.VehicleRecordStatus;
import com.leader.parcautomobile.exception.DuplicatePlateException;
import com.leader.parcautomobile.exception.ResourceNotFoundException;
import com.leader.parcautomobile.mapper.VehicleMapper;
import com.leader.parcautomobile.repository.VehicleRepository;
import com.leader.parcautomobile.repository.VehicleSpecifications;
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
public class VehicleService {

	private final VehicleRepository vehicleRepository;

	@Transactional(readOnly = true)
	public VehiclePageResponse list(
			String q,
			VehicleCategory category,
			VehicleAvailability availability,
			VehicleRecordStatus status,
			Pageable pageable) {
		Specification<Vehicle> spec = Specification.where(VehicleSpecifications.notDeleted());
		if (q != null && !q.isBlank()) {
			spec = spec.and(VehicleSpecifications.matchesSearch(q));
		}
		spec = spec.and(VehicleSpecifications.hasCategory(category));
		spec = spec.and(VehicleSpecifications.hasAvailability(availability));
		spec = spec.and(VehicleSpecifications.hasStatus(status));

		Page<Vehicle> page = vehicleRepository.findAll(spec, pageable);
		List<VehicleResponse> content = page.getContent().stream().map(VehicleMapper::toResponse).toList();
		return new VehiclePageResponse(
				content,
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages());
	}

	@Transactional(readOnly = true)
	public VehicleResponse getById(UUID id) {
		Vehicle v = vehicleRepository
				.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable"));
		if (v.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Véhicule introuvable");
		}
		return VehicleMapper.toResponse(v);
	}

	@Transactional
	public VehicleResponse create(CreateVehicleRequest request) {
		String plate = normalizePlate(request.plateNumber());
		if (vehicleRepository.existsByPlateNumberIgnoreCase(plate)) {
			throw new DuplicatePlateException("Cette immatriculation est déjà enregistrée");
		}
		Vehicle.VehicleBuilder b = Vehicle.builder();
		VehicleMapper.applyCreate(b, request, plate);
		Vehicle saved = vehicleRepository.save(b.build());
		return VehicleMapper.toResponse(saved);
	}

	@Transactional
	public VehicleResponse update(UUID id, UpdateVehicleRequest request) {
		Vehicle v = requireActive(id);
		String plate = normalizePlate(request.plateNumber());
		if (!plate.equalsIgnoreCase(v.getPlateNumber())
				&& vehicleRepository.existsByPlateNumberIgnoreCase(plate)) {
			throw new DuplicatePlateException("Cette immatriculation est déjà enregistrée");
		}
		VehicleMapper.applyUpdate(v, request, plate);
		return VehicleMapper.toResponse(vehicleRepository.save(v));
	}

	@Transactional
	public VehicleResponse patchAvailability(UUID id, AvailabilityPatchRequest body) {
		Vehicle v = requireActive(id);
		v.setAvailability(body.availability());
		return VehicleMapper.toResponse(vehicleRepository.save(v));
	}

	@Transactional
	public void softDelete(UUID id) {
		Vehicle v = vehicleRepository
				.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable"));
		if (v.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Véhicule introuvable");
		}
		v.setDeletedAt(Instant.now());
		v.setStatus(VehicleRecordStatus.ARCHIVED);
		vehicleRepository.save(v);
	}

	private Vehicle requireActive(UUID id) {
		Vehicle v = vehicleRepository
				.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable"));
		if (v.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Véhicule introuvable");
		}
		return v;
	}

	static String normalizePlate(String raw) {
		return raw.trim().replaceAll("\\s+", "").toUpperCase();
	}
}
