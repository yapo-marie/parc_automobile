package com.leader.parcautomobile.controller;

import com.leader.parcautomobile.dto.fuelrecord.CreateFuelRecordRequest;
import com.leader.parcautomobile.dto.fuelrecord.FuelRecordPageResponse;
import com.leader.parcautomobile.dto.fuelrecord.FuelRecordResponse;
import com.leader.parcautomobile.dto.fuelrecord.FuelRecordStatsResponse;
import com.leader.parcautomobile.service.FuelRecordService;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fuel-records")
@RequiredArgsConstructor
public class FuelRecordController {

	private final FuelRecordService fuelRecordService;

	@GetMapping("/stats")
	@PreAuthorize("isAuthenticated()")
	public FuelRecordStatsResponse stats(@RequestParam UUID vehicleId) {
		return fuelRecordService.stats(vehicleId);
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public FuelRecordPageResponse list(
			@RequestParam(required = false) UUID vehicleId,
			@RequestParam(required = false) LocalDate fromDate,
			@RequestParam(required = false) LocalDate toDate,
			@PageableDefault(size = 20, sort = "fillDate", direction = Sort.Direction.DESC)
					Pageable pageable) {
		return fuelRecordService.list(vehicleId, fromDate, toDate, pageable);
	}

	@PostMapping
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public ResponseEntity<FuelRecordResponse> create(
			Authentication authentication, @Valid @RequestBody CreateFuelRecordRequest body) {
		FuelRecordResponse created = fuelRecordService.create(authentication.getName(), body);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}
}

