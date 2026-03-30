package com.leader.parcautomobile.controller;

import com.leader.parcautomobile.dto.fuelrecord.FuelHistoryPointResponse;
import com.leader.parcautomobile.dto.fuelrecord.FuelLiveResponse;
import com.leader.parcautomobile.gps.dto.GpsAlertDto;
import com.leader.parcautomobile.gps.entity.AlertType;
import com.leader.parcautomobile.gps.entity.GpsPosition;
import com.leader.parcautomobile.gps.repository.GpsAlertRepository;
import com.leader.parcautomobile.gps.repository.GpsPositionRepository;
import com.leader.parcautomobile.repository.VehicleRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fuel")
@RequiredArgsConstructor
public class FuelLiveController {

	private final VehicleRepository vehicleRepository;
	private final GpsPositionRepository gpsPositionRepository;
	private final GpsAlertRepository gpsAlertRepository;

	@GetMapping("/live")
	@PreAuthorize("isAuthenticated()")
	public List<FuelLiveResponse> live() {
		return vehicleRepository.findAll().stream()
				.filter(v -> v.getDeletedAt() == null)
				.map(v -> new FuelLiveResponse(
						v.getId(),
						v.getPlateNumber(),
						v.getBrand() + " " + v.getModel(),
						v.getPhotoUrl(),
						v.getFuelLevel(),
						v.getLastSeen()))
				.toList();
	}

	@GetMapping("/history/{id}")
	@PreAuthorize("isAuthenticated()")
	public List<FuelHistoryPointResponse> history(@PathVariable UUID id) {
		List<GpsPosition> points = gpsPositionRepository.findTop200ByVehicleIdOrderByRecordedAtDesc(id);
		return points.stream()
				.map(p -> new FuelHistoryPointResponse(p.getRecordedAt(), p.getFuelLevel(), p.getSpeed()))
				.toList();
	}

	@GetMapping("/alerts")
	@PreAuthorize("isAuthenticated()")
	public List<GpsAlertDto> alerts() {
		return gpsAlertRepository.search(null, AlertType.LOW_FUEL, false, PageRequest.of(0, 100))
				.stream()
				.map(a -> new GpsAlertDto(
						a.getId(),
						a.getVehicle().getId(),
						a.getType(),
						a.getMessage(),
						a.getLatitude(),
						a.getLongitude(),
						a.getSpeed(),
						a.isAcknowledged(),
						a.getCreatedAt()))
				.toList();
	}
}

