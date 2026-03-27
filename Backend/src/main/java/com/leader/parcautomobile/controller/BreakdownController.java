package com.leader.parcautomobile.controller;

import com.leader.parcautomobile.dto.breakdown.BreakdownPageResponse;
import com.leader.parcautomobile.dto.breakdown.BreakdownResponse;
import com.leader.parcautomobile.dto.breakdown.CreateBreakdownRequest;
import com.leader.parcautomobile.dto.breakdown.ResolveBreakdownRequest;
import com.leader.parcautomobile.entity.BreakdownStatus;
import com.leader.parcautomobile.service.BreakdownService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/breakdowns")
@RequiredArgsConstructor
public class BreakdownController {

	private final BreakdownService breakdownService;

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public BreakdownPageResponse list(
			@RequestParam(required = false) UUID vehicleId,
			@RequestParam(required = false) BreakdownStatus status,
			@PageableDefault(size = 20, sort = "declaredAt", direction = Sort.Direction.DESC)
					Pageable pageable) {
		return breakdownService.listAll(vehicleId, status, pageable);
	}

	@PostMapping
	@PreAuthorize("hasAnyAuthority('FLEET_MANAGE', 'VEHICLE_READ')")
	public ResponseEntity<BreakdownResponse> create(
			Authentication authentication,
			@Valid @RequestBody CreateBreakdownRequest body) {
		BreakdownResponse created = breakdownService.create(authentication.getName(), body);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PostMapping("/{id}/resolve")
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public BreakdownResponse resolve(
			@PathVariable UUID id,
			@Valid @RequestBody ResolveBreakdownRequest body) {
		return breakdownService.resolve(id, body);
	}
}

