package com.leader.parcautomobile.service;

import com.leader.parcautomobile.dto.zone.CreateZoneRequest;
import com.leader.parcautomobile.dto.zone.ZoneResponse;
import com.leader.parcautomobile.entity.User;
import com.leader.parcautomobile.entity.Vehicle;
import com.leader.parcautomobile.entity.Zone;
import com.leader.parcautomobile.exception.ResourceNotFoundException;
import com.leader.parcautomobile.repository.UserRepository;
import com.leader.parcautomobile.repository.VehicleRepository;
import com.leader.parcautomobile.repository.ZoneRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ZoneService {

	private final ZoneRepository zoneRepository;
	private final UserRepository userRepository;
	private final VehicleRepository vehicleRepository;

	@Transactional(readOnly = true)
	public List<ZoneResponse> listActive() {
		return zoneRepository.findByActiveTrueOrderByNameAsc().stream().map(this::toDto).toList();
	}

	@Transactional
	public ZoneResponse create(String email, CreateZoneRequest body) {
		User creator = userRepository.findByEmailWithRoles(email)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
		Zone z = Zone.builder()
				.name(body.name())
				.description(body.description())
				.color(body.color())
				.type(body.type())
				.centerLat(body.centerLat())
				.centerLng(body.centerLng())
				.radiusMeters(body.radiusMeters())
				.polygonCoordinates(body.polygonCoordinates())
				.maxSpeedKmh(body.maxSpeedKmh())
				.active(body.active() == null || body.active())
				.createdBy(creator)
				.build();
		return toDto(zoneRepository.save(z));
	}

	@Transactional
	public ZoneResponse update(UUID id, CreateZoneRequest body) {
		Zone z = zoneRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Zone introuvable"));
		z.setName(body.name());
		z.setDescription(body.description());
		z.setColor(body.color());
		z.setType(body.type());
		z.setCenterLat(body.centerLat());
		z.setCenterLng(body.centerLng());
		z.setRadiusMeters(body.radiusMeters());
		z.setPolygonCoordinates(body.polygonCoordinates());
		z.setMaxSpeedKmh(body.maxSpeedKmh());
		if (body.active() != null) z.setActive(body.active());
		return toDto(zoneRepository.save(z));
	}

	@Transactional
	public ZoneResponse toggle(UUID id) {
		Zone z = zoneRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Zone introuvable"));
		z.setActive(!z.isActive());
		return toDto(zoneRepository.save(z));
	}

	@Transactional
	public void delete(UUID id) {
		Zone z = zoneRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Zone introuvable"));
		z.setActive(false);
		zoneRepository.save(z);
	}

	@Transactional
	public void assignVehicle(String email, UUID zoneId, UUID vehicleId) {
		Zone z = zoneRepository.findById(zoneId).orElseThrow(() -> new ResourceNotFoundException("Zone introuvable"));
		Vehicle v = vehicleRepository.findById(vehicleId).orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable"));
		z.getVehicles().add(v);
		zoneRepository.save(z);
	}

	@Transactional
	public void unassignVehicle(UUID zoneId, UUID vehicleId) {
		Zone z = zoneRepository.findByIdWithVehicles(zoneId).orElseThrow(() -> new ResourceNotFoundException("Zone introuvable"));
		z.getVehicles().removeIf(v -> v.getId().equals(vehicleId));
		zoneRepository.save(z);
	}

	@Transactional(readOnly = true)
	public List<UUID> vehiclesInZone(UUID zoneId) {
		Zone z = zoneRepository.findByIdWithVehicles(zoneId).orElseThrow(() -> new ResourceNotFoundException("Zone introuvable"));
		return z.getVehicles().stream().map(Vehicle::getId).toList();
	}

	@Transactional(readOnly = true)
	public List<ZoneResponse> zonesForVehicle(UUID vehicleId) {
		return zoneRepository.findActiveByVehicleId(vehicleId).stream().map(this::toDto).toList();
	}

	private ZoneResponse toDto(Zone z) {
		return new ZoneResponse(
				z.getId(),
				z.getName(),
				z.getDescription(),
				z.getColor(),
				z.getType(),
				z.getCenterLat(),
				z.getCenterLng(),
				z.getRadiusMeters(),
				z.getPolygonCoordinates(),
				z.getMaxSpeedKmh(),
				z.isActive(),
				z.getVehicles().stream().map(Vehicle::getId).toList(),
				z.getCreatedAt());
	}
}

