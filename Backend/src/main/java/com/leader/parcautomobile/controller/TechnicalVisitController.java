package com.leader.parcautomobile.controller;

import com.leader.parcautomobile.dto.technicalvisit.CreateTechnicalVisitRequest;
import com.leader.parcautomobile.dto.technicalvisit.TechnicalVisitPageResponse;
import com.leader.parcautomobile.dto.technicalvisit.TechnicalVisitResponse;
import com.leader.parcautomobile.dto.technicalvisit.UpdateTechnicalVisitRequest;
import com.leader.parcautomobile.service.TechnicalVisitService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/technical-visits")
@RequiredArgsConstructor
public class TechnicalVisitController {

	private final TechnicalVisitService technicalVisitService;

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public TechnicalVisitPageResponse list(
			@RequestParam(required = false) UUID vehicleId,
			@RequestParam(required = false) String type,
			@RequestParam(required = false) String result,
			@PageableDefault(size = 20, sort = "scheduledDate", direction = Sort.Direction.DESC)
					Pageable pageable) {
		return technicalVisitService.listAll(vehicleId, type, result, pageable);
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public TechnicalVisitResponse getById(@PathVariable UUID id) {
		return technicalVisitService.getById(id);
	}

	@PostMapping
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public ResponseEntity<Void> create(@Valid @RequestBody CreateTechnicalVisitRequest body) {
		technicalVisitService.create(body);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public ResponseEntity<Void> update(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateTechnicalVisitRequest body) {
		technicalVisitService.update(id, body);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}

