package com.leader.parcautomobile.controller;

import com.leader.parcautomobile.dto.zone.CreateZoneRequest;
import com.leader.parcautomobile.dto.zone.ZoneResponse;
import com.leader.parcautomobile.service.ZoneService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public class ZoneController {

	private final ZoneService zoneService;

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public List<ZoneResponse> list() {
		return zoneService.listActive();
	}

	@PostMapping
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public ZoneResponse create(Authentication authentication, @Valid @RequestBody CreateZoneRequest body) {
		return zoneService.create(authentication.getName(), body);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public ZoneResponse update(@PathVariable UUID id, @Valid @RequestBody CreateZoneRequest body) {
		return zoneService.update(id, body);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public void delete(@PathVariable UUID id) {
		zoneService.delete(id);
	}

	@PatchMapping("/{id}/toggle")
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public ZoneResponse toggle(@PathVariable UUID id) {
		return zoneService.toggle(id);
	}

	@PostMapping("/{id}/vehicles/{vid}")
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public void assign(Authentication authentication, @PathVariable UUID id, @PathVariable UUID vid) {
		zoneService.assignVehicle(authentication.getName(), id, vid);
	}

	@DeleteMapping("/{id}/vehicles/{vid}")
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public void unassign(@PathVariable UUID id, @PathVariable UUID vid) {
		zoneService.unassignVehicle(id, vid);
	}

	@GetMapping("/{id}/vehicles")
	@PreAuthorize("isAuthenticated()")
	public Map<String, List<UUID>> vehicles(@PathVariable UUID id) {
		return Map.of("vehicleIds", zoneService.vehiclesInZone(id));
	}
}

