package com.leader.parcautomobile.gps.service;

import com.leader.parcautomobile.entity.User;
import com.leader.parcautomobile.entity.Vehicle;
import com.leader.parcautomobile.gps.dto.GpsGeofenceCreateRequest;
import com.leader.parcautomobile.gps.dto.GpsGeofenceUpdateRequest;
import com.leader.parcautomobile.gps.dto.GeofenceDto;
import com.leader.parcautomobile.gps.entity.Geofence;
import com.leader.parcautomobile.gps.repository.GeofenceRepository;
import com.leader.parcautomobile.repository.UserRepository;
import com.leader.parcautomobile.repository.VehicleRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GeofenceService {

	private final GeofenceRepository geofenceRepository;
	private final VehicleRepository vehicleRepository;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public List<GeofenceDto> listAll() {
		return geofenceRepository.findAll().stream().map(this::toDto).toList();
	}

	@Transactional
	public GeofenceDto create(String managerEmail, GpsGeofenceCreateRequest body) {
		User createdBy = userRepository.findByEmailWithRoles(managerEmail)
				.orElseThrow(() -> new com.leader.parcautomobile.exception.ResourceNotFoundException("Utilisateur introuvable"));
		Geofence saved = geofenceRepository.save(
				Geofence.builder()
						.name(body.name())
						.description(body.description())
						.centerLat(body.centerLat())
						.centerLng(body.centerLng())
						.radiusM(body.radiusM())
						.active(body.active())
						.createdBy(createdBy)
						.createdAt(Instant.now())
						.build());
		return toDto(saved);
	}

	@Transactional
	public GeofenceDto update(String managerEmail, UUID id, GpsGeofenceUpdateRequest body) {
		Geofence g = geofenceRepository
				.findById(id)
				.orElseThrow(() -> new com.leader.parcautomobile.exception.ResourceNotFoundException("Géo-clôture introuvable"));
		g.setName(body.name());
		g.setDescription(body.description());
		g.setCenterLat(body.centerLat());
		g.setCenterLng(body.centerLng());
		g.setRadiusM(body.radiusM());
		g.setActive(body.active());
		Geofence saved = geofenceRepository.save(g);
		return toDto(saved);
	}

	@Transactional
	public void delete(UUID id) {
		geofenceRepository.deleteById(id);
	}

	@Transactional
	public void assignVehicleToGeofence(UUID geofenceId, UUID vehicleId) {
		Geofence g = geofenceRepository
				.findById(geofenceId)
				.orElseThrow(() -> new com.leader.parcautomobile.exception.ResourceNotFoundException("Géo-clôture introuvable"));
		Vehicle v = vehicleRepository
				.findById(vehicleId)
				.orElseThrow(() -> new com.leader.parcautomobile.exception.ResourceNotFoundException("Véhicule introuvable"));
		if (v.getDeletedAt() != null) {
			throw new com.leader.parcautomobile.exception.ResourceNotFoundException("Véhicule introuvable");
		}
		g.getVehicles().add(v);
		geofenceRepository.save(g);
	}

	@Transactional
	public void unassignVehicleToGeofence(UUID geofenceId, UUID vehicleId) {
		Geofence g = geofenceRepository
				.findById(geofenceId)
				.orElseThrow(() -> new com.leader.parcautomobile.exception.ResourceNotFoundException("Géo-clôture introuvable"));
		g.getVehicles().removeIf(v -> v.getId().equals(vehicleId));
		geofenceRepository.save(g);
	}

	private GeofenceDto toDto(com.leader.parcautomobile.gps.entity.Geofence g) {
		Set<UUID> vehicleIds = g.getVehicles() == null ? Set.of() :
				g.getVehicles().stream().map(Vehicle::getId).collect(java.util.stream.Collectors.toSet());
		return new GeofenceDto(
				g.getId(),
				g.getName(),
				g.getDescription(),
				g.getCenterLat(),
				g.getCenterLng(),
				g.getRadiusM(),
				g.isActive(),
				vehicleIds,
				g.getCreatedAt());
	}
}

