package com.leader.parcautomobile.controller;

import com.leader.parcautomobile.dto.reservation.CancelReservationRequest;
import com.leader.parcautomobile.dto.reservation.CreateReservationRequest;
import com.leader.parcautomobile.dto.reservation.RejectReservationRequest;
import com.leader.parcautomobile.dto.reservation.ReservationPageResponse;
import com.leader.parcautomobile.dto.reservation.ReservationResponse;
import com.leader.parcautomobile.entity.ReservationStatus;
import com.leader.parcautomobile.service.ReservationService;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

	private final ReservationService reservationService;

	@GetMapping
	@PreAuthorize("hasAuthority('RESERVATION_MANAGE')")
	public ReservationPageResponse list(
			@RequestParam(required = false) ReservationStatus status,
			@RequestParam(required = false) UUID vehicleId,
			@PageableDefault(size = 20, sort = "startDatetime", direction = Sort.Direction.DESC)
					Pageable pageable) {
		return reservationService.listAll(status, vehicleId, pageable);
	}

	@GetMapping("/my")
	@PreAuthorize("isAuthenticated()")
	public ReservationPageResponse my(
			Authentication authentication,
			@PageableDefault(size = 20, sort = "startDatetime", direction = Sort.Direction.DESC)
					Pageable pageable) {
		return reservationService.listMine(authentication.getName(), pageable);
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public ReservationResponse get(@PathVariable UUID id, Authentication authentication) {
		ReservationResponse r = reservationService.getById(id);
		if (!canSee(authentication, r)) {
			throw new org.springframework.security.access.AccessDeniedException(
					"Accès réservé au demandeur ou au gestionnaire");
		}
		return r;
	}

	private static boolean canSee(Authentication authentication, ReservationResponse r) {
		if (authentication == null) {
			return false;
		}
		if (authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch("RESERVATION_MANAGE"::equals)) {
			return true;
		}
		return authentication.getName().equalsIgnoreCase(r.requesterEmail());
	}

	@PostMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ReservationResponse> create(
			Authentication authentication, @Valid @RequestBody CreateReservationRequest body) {
		ReservationResponse created =
				reservationService.create(authentication.getName(), body);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PatchMapping("/{id}/confirm")
	@PreAuthorize("hasAuthority('RESERVATION_MANAGE')")
	public ReservationResponse confirm(Authentication authentication, @PathVariable UUID id) {
		return reservationService.confirm(authentication.getName(), id);
	}

	@PostMapping("/{id}/reject")
	@PreAuthorize("hasAuthority('RESERVATION_MANAGE')")
	public ReservationResponse reject(
			Authentication authentication,
			@PathVariable UUID id,
			@Valid @RequestBody RejectReservationRequest body) {
		return reservationService.reject(authentication.getName(), id, body);
	}

	@PatchMapping("/{id}/cancel")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> cancel(
			Authentication authentication,
			@PathVariable UUID id,
			@RequestBody(required = false) CancelReservationRequest body) {
		boolean manage = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch("RESERVATION_MANAGE"::equals);
		String reason = body != null ? body.reason() : null;
		reservationService.cancel(authentication.getName(), manage, id, reason);
		return ResponseEntity.noContent().build();
	}
}
