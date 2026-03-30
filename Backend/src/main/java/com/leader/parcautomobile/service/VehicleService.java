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
import com.leader.parcautomobile.exception.DuplicateImeiException;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
		if (vehicleRepository.existsByImei(request.imei().trim())) {
			throw new DuplicateImeiException("Cet IMEI est déjà enregistré");
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
		String imei = request.imei().trim();
		if (!imei.equals(v.getImei()) && vehicleRepository.existsByImei(imei)) {
			throw new DuplicateImeiException("Cet IMEI est déjà enregistré");
		}
		VehicleMapper.applyUpdate(v, request, plate);
		return VehicleMapper.toResponse(vehicleRepository.save(v));
	}

	@Transactional
	public VehicleResponse uploadPhoto(UUID id, MultipartFile file) {
		Vehicle v = requireActive(id);
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("Fichier image requis");
		}
		if (file.getSize() > 5L * 1024 * 1024) {
			throw new IllegalArgumentException("Image trop volumineuse (max 5 Mo)");
		}

		String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
		String ext = "";
		int dot = original.lastIndexOf('.');
		if (dot >= 0) ext = original.substring(dot + 1);
		if (!java.util.Set.of("jpg", "jpeg", "png", "webp").contains(ext)) {
			throw new IllegalArgumentException("Format image non supporté (jpg/jpeg/png/webp)");
		}

		try {
			Path uploadDir = Paths.get("uploads", "vehicles");
			Files.createDirectories(uploadDir);
			String filename = id + "_" + java.util.UUID.randomUUID() + "." + ext;
			Path target = uploadDir.resolve(filename);
			Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
			v.setPhotoUrl("/api/vehicles/photos/" + filename);
			Vehicle saved = vehicleRepository.save(v);
			return VehicleMapper.toResponse(saved);
		} catch (Exception e) {
			throw new IllegalArgumentException("Impossible de sauvegarder la photo");
		}
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
