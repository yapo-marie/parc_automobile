package com.leader.parcautomobile.gps.controller;

import com.leader.parcautomobile.gps.dto.GpsGeofenceCreateRequest;
import com.leader.parcautomobile.gps.dto.GpsGeofenceUpdateRequest;
import com.leader.parcautomobile.gps.dto.GeofenceDto;
import com.leader.parcautomobile.gps.service.GeofenceService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/geofences")
@RequiredArgsConstructor
public class GeofenceController {

	private final GeofenceService geofenceService;

	@GetMapping
	@PreAuthorize("hasAuthority('REPORT_VIEW') or hasAuthority('FLEET_MANAGE')")
	public List<GeofenceDto> list() {
		// Les géo-clôtures sont en accès lecture pour le MVP
		return geofenceService.listAll();
	}

	@PostMapping
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public GeofenceDto create(
			@AuthenticationPrincipal UserDetails principal,
			@RequestBody @jakarta.validation.Valid GpsGeofenceCreateRequest body) {
		return geofenceService.create(principal.getUsername(), body);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public GeofenceDto update(
			@AuthenticationPrincipal UserDetails principal,
			@PathVariable UUID id,
			@RequestBody @jakarta.validation.Valid GpsGeofenceUpdateRequest body) {
		return geofenceService.update(principal.getUsername(), id, body);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public void delete(@PathVariable UUID id) {
		geofenceService.delete(id);
	}

	@PostMapping("/{id}/vehicles/{vid}")
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public void assignVehicle(@PathVariable UUID id, @PathVariable UUID vid) {
		geofenceService.assignVehicleToGeofence(id, vid);
	}

	@DeleteMapping("/{id}/vehicles/{vid}")
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public void unassignVehicle(@PathVariable UUID id, @PathVariable UUID vid) {
		geofenceService.unassignVehicleToGeofence(id, vid);
	}
}

