package com.leader.parcautomobile.controller;

import com.leader.parcautomobile.dto.zone.ZoneResponse;
import com.leader.parcautomobile.service.ZoneService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleZoneController {

	private final ZoneService zoneService;

	@GetMapping("/{id}/zones")
	@PreAuthorize("isAuthenticated()")
	public List<ZoneResponse> zonesForVehicle(@PathVariable UUID id) {
		return zoneService.zonesForVehicle(id);
	}
}

