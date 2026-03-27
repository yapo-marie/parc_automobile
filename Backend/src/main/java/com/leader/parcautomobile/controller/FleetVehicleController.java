package com.leader.parcautomobile.controller;

import com.leader.parcautomobile.dto.fleet.CreateFleetVehicleRequest;
import com.leader.parcautomobile.dto.fleet.FleetStatsResponse;
import com.leader.parcautomobile.dto.fleet.FleetVehiclePageResponse;
import com.leader.parcautomobile.dto.fleet.FleetVehicleResponse;
import com.leader.parcautomobile.dto.fleet.UpdateFleetVehicleRequest;
import com.leader.parcautomobile.service.FleetVehicleService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fleet")
@RequiredArgsConstructor
public class FleetVehicleController {

	private final FleetVehicleService fleetVehicleService;

	@GetMapping("/stats")
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public FleetStatsResponse stats() {
		return fleetVehicleService.stats();
	}

	@GetMapping
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public FleetVehiclePageResponse list(
			@RequestParam(required = false) String administration,
			@RequestParam(required = false) UUID vehicleId,
			@PageableDefault(size = 20, sort = "administration", direction = Sort.Direction.ASC)
					Pageable pageable) {
		return fleetVehicleService.list(administration, vehicleId, pageable);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public FleetVehicleResponse get(@PathVariable UUID id) {
		return fleetVehicleService.getById(id);
	}

	@PostMapping
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public ResponseEntity<FleetVehicleResponse> create(@Valid @RequestBody CreateFleetVehicleRequest body) {
		FleetVehicleResponse created = fleetVehicleService.create(body);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public FleetVehicleResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateFleetVehicleRequest body) {
		return fleetVehicleService.update(id, body);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		fleetVehicleService.softDelete(id);
		return ResponseEntity.noContent().build();
	}
}
