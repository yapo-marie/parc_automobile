package com.leader.parcautomobile.controller;

import com.leader.parcautomobile.dto.assignment.AssignmentPageResponse;
import com.leader.parcautomobile.dto.assignment.AssignmentResponse;
import com.leader.parcautomobile.dto.assignment.CreateAssignmentRequest;
import com.leader.parcautomobile.dto.assignment.CreatePoolAssignmentRequest;
import com.leader.parcautomobile.dto.assignment.EndAssignmentRequest;
import com.leader.parcautomobile.dto.assignment.WithdrawAssignmentRequest;
import com.leader.parcautomobile.entity.AssignmentStatus;
import com.leader.parcautomobile.service.AssignmentService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

	private final AssignmentService assignmentService;

	@GetMapping
	@PreAuthorize("hasAuthority('ASSIGNMENT_MANAGE')")
	public AssignmentPageResponse listAll(
			@RequestParam(required = false) AssignmentStatus status,
			@RequestParam(required = false) UUID vehicleId,
			@RequestParam(required = false) UUID driverId,
			@PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.DESC)
					Pageable pageable) {
		return assignmentService.listAll(status, vehicleId, driverId, pageable);
	}

	@GetMapping("/my")
	@PreAuthorize("isAuthenticated()")
	public AssignmentPageResponse my(
			Authentication authentication,
			@PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.DESC)
					Pageable pageable) {
		return assignmentService.listMine(authentication.getName(), pageable);
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public AssignmentResponse get(
			@PathVariable UUID id,
			Authentication authentication) {
		AssignmentResponse r = assignmentService.getById(id);
		boolean manage = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch("ASSIGNMENT_MANAGE"::equals);
		if (!manage && !authentication.getName().equalsIgnoreCase(r.driverEmail())) {
			throw new AccessDeniedException("Accès réservé au gestionnaire ou au conducteur");
		}
		return r;
	}

	@PostMapping
	@PreAuthorize("hasAuthority('ASSIGNMENT_MANAGE')")
	public ResponseEntity<AssignmentResponse> create(
			Authentication authentication,
			@Valid @RequestBody CreateAssignmentRequest body) {
		AssignmentResponse created = assignmentService.create(authentication.getName(), body);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PostMapping("/pool")
	@PreAuthorize("hasAuthority('ASSIGNMENT_MANAGE')")
	public ResponseEntity<AssignmentResponse> createFromPool(
			Authentication authentication,
			@Valid @RequestBody CreatePoolAssignmentRequest body) {
		AssignmentResponse created =
				assignmentService.createFromPool(authentication.getName(), body);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PostMapping("/{id}/end")
	@PreAuthorize("hasAuthority('ASSIGNMENT_MANAGE')")
	public AssignmentResponse end(
			Authentication authentication,
			@PathVariable UUID id,
			@Valid @RequestBody EndAssignmentRequest body) {
		return assignmentService.end(authentication.getName(), id, body);
	}

	@PostMapping("/{id}/withdraw")
	@PreAuthorize("hasAuthority('ASSIGNMENT_MANAGE')")
	public AssignmentResponse withdraw(
			Authentication authentication,
			@PathVariable UUID id,
			@Valid @RequestBody WithdrawAssignmentRequest body) {
		return assignmentService.withdraw(authentication.getName(), id, body);
	}
}

