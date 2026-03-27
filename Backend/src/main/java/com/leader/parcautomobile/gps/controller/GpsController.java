package com.leader.parcautomobile.gps.controller;

import com.leader.parcautomobile.entity.User;
import com.leader.parcautomobile.entity.Vehicle;
import com.leader.parcautomobile.gps.dto.GpsAlertDto;
import com.leader.parcautomobile.gps.dto.GpsEngineStatusResponse;
import com.leader.parcautomobile.gps.dto.GpsPositionDto;
import com.leader.parcautomobile.gps.dto.GpsVehicleStatsResponse;
import com.leader.parcautomobile.gps.entity.AlertType;
import com.leader.parcautomobile.gps.entity.GpsAlert;
import com.leader.parcautomobile.gps.entity.GpsPosition;
import com.leader.parcautomobile.gps.repository.GpsAlertRepository;
import com.leader.parcautomobile.gps.repository.GpsPositionRepository;
import com.leader.parcautomobile.gps.service.GpsStatsService;
import com.leader.parcautomobile.gps.service.MotorCutoffService;
import com.leader.parcautomobile.repository.UserRepository;
import com.leader.parcautomobile.repository.VehicleRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gps")
@RequiredArgsConstructor
public class GpsController {

	private final VehicleRepository vehicleRepository;
	private final GpsPositionRepository gpsPositionRepository;
	private final GpsAlertRepository gpsAlertRepository;
	private final GpsStatsService gpsStatsService;
	private final MotorCutoffService motorCutoffService;
	private final UserRepository userRepository;

	@GetMapping("/vehicles/{id}/position")
	@PreAuthorize("hasAuthority('REPORT_VIEW') or hasAuthority('VEHICLE_READ')")
	public GpsPositionDto lastPosition(@PathVariable UUID id) {
		Vehicle v = vehicleRepository.findById(id)
				.orElseThrow(() -> new com.leader.parcautomobile.exception.ResourceNotFoundException("Véhicule introuvable"));
		var pos = gpsPositionRepository.findTopByVehicleIdOrderByRecordedAtDesc(id)
				.orElseThrow(() -> new com.leader.parcautomobile.exception.ResourceNotFoundException("Aucune position"));
		return new GpsPositionDto(
				v.getId(),
				v.getPlateNumber(),
				pos.getImei(),
				pos.getLatitude(),
				pos.getLongitude(),
				pos.getSpeed(),
				pos.getHeading(),
				v.isIgnitionOn(),
				pos.getRecordedAt());
	}

	@GetMapping("/vehicles/{id}/history")
	@PreAuthorize("hasAuthority('REPORT_VIEW') or hasAuthority('VEHICLE_READ')")
	public List<GpsPositionDto> history(
			@PathVariable UUID id,
			@RequestParam Instant from,
			@RequestParam Instant to) {
		Vehicle v = vehicleRepository.findById(id)
				.orElseThrow(() -> new com.leader.parcautomobile.exception.ResourceNotFoundException("Véhicule introuvable"));
		List<GpsPosition> pts = gpsPositionRepository.findByVehicleIdAndRecordedAtBetweenOrderByRecordedAtAsc(id, from, to);
		return pts.stream().map(p -> new GpsPositionDto(
				v.getId(),
				v.getPlateNumber(),
				p.getImei(),
				p.getLatitude(),
				p.getLongitude(),
				p.getSpeed(),
				p.getHeading(),
				p.isIgnition(),
				p.getRecordedAt())).toList();
	}

	@GetMapping("/vehicles/{id}/trip/{date}")
	@PreAuthorize("hasAuthority('REPORT_VIEW') or hasAuthority('VEHICLE_READ')")
	public List<GpsPositionDto> trip(
			@PathVariable UUID id,
			@PathVariable LocalDate date) {
		Vehicle v = vehicleRepository.findById(id)
				.orElseThrow(() -> new com.leader.parcautomobile.exception.ResourceNotFoundException("Véhicule introuvable"));
		Instant dayStart = date.atStartOfDay(ZoneOffset.UTC).toInstant();
		Instant dayEnd = dayStart.plusSeconds(86400);
		List<GpsPosition> pts = gpsPositionRepository.findTripForDay(id, dayStart, dayEnd);
		return pts.stream().map(p -> new GpsPositionDto(
				v.getId(),
				v.getPlateNumber(),
				p.getImei(),
				p.getLatitude(),
				p.getLongitude(),
				p.getSpeed(),
				p.getHeading(),
				p.isIgnition(),
				p.getRecordedAt())).toList();
	}

	@GetMapping("/fleet/positions")
	@PreAuthorize("hasAuthority('REPORT_VIEW') or hasAuthority('VEHICLE_READ')")
	public List<GpsPositionDto> fleetPositions() {
		Instant cutoff = Instant.now().minusSeconds(600);
		List<Vehicle> online = vehicleRepository.findOnlineVehicles(cutoff);
		return online.stream()
				.flatMap(v -> gpsPositionRepository.findTopByVehicleIdOrderByRecordedAtDesc(v.getId()).stream()
						.map(p -> new GpsPositionDto(
								v.getId(),
								v.getPlateNumber(),
								p.getImei(),
								p.getLatitude(),
								p.getLongitude(),
								p.getSpeed(),
								p.getHeading(),
								p.isIgnition(),
								p.getRecordedAt())))
				.toList();
	}

	@GetMapping("/alerts")
	@PreAuthorize("hasAuthority('REPORT_VIEW') or hasAuthority('FLEET_MANAGE')")
	public Page<GpsAlertDto> alerts(
			@RequestParam(required = false) AlertType type,
			@RequestParam(required = false) UUID vehicleId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		Pageable pageable = PageRequest.of(page, size);
		return gpsAlertRepository.search(vehicleId, type, null, pageable)
				.map(this::toDto);
	}

	@GetMapping("/alerts/active")
	@PreAuthorize("hasAuthority('REPORT_VIEW') or hasAuthority('FLEET_MANAGE')")
	public Page<GpsAlertDto> activeAlerts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		Pageable pageable = PageRequest.of(page, size);
		return gpsAlertRepository.search(null, null, false, pageable)
				.map(this::toDto);
	}

	@PatchMapping("/alerts/{id}/ack")
	@PreAuthorize("hasAuthority('REPORT_VIEW') or hasAuthority('FLEET_MANAGE')")
	public GpsAlertDto acknowledge(
			@PathVariable UUID id,
			@AuthenticationPrincipal UserDetails principal) {
		User user = userRepository.findByEmailWithRoles(principal.getUsername())
				.orElseThrow(() -> new com.leader.parcautomobile.exception.ResourceNotFoundException("Utilisateur introuvable"));
		GpsAlert a = gpsAlertRepository.findByIdAndAcknowledgedFalse(id)
				.orElseThrow(() -> new com.leader.parcautomobile.exception.ResourceNotFoundException("Alerte introuvable"));
		a.setAcknowledged(true);
		a.setAcknowledgedBy(user);
		a.setAcknowledgedAt(Instant.now());
		GpsAlert saved = gpsAlertRepository.save(a);
		return toDto(saved);
	}

	@DeleteMapping("/alerts/{id}")
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public void deleteAlert(@PathVariable UUID id) {
		gpsAlertRepository.deleteById(id);
	}

	@PostMapping("/vehicles/{id}/engine/cut")
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public void cutEngine(@PathVariable UUID id) {
		motorCutoffService.cutEngine(id);
	}

	@PostMapping("/vehicles/{id}/engine/restore")
	@PreAuthorize("hasAuthority('FLEET_MANAGE')")
	public void restoreEngine(@PathVariable UUID id) {
		motorCutoffService.restoreEngine(id);
	}

	@GetMapping("/vehicles/{id}/engine/status")
	@PreAuthorize("hasAuthority('REPORT_VIEW') or hasAuthority('FLEET_MANAGE')")
	public GpsEngineStatusResponse engineStatus(@PathVariable UUID id) {
		Vehicle v = vehicleRepository.findById(id)
				.orElseThrow(() -> new com.leader.parcautomobile.exception.ResourceNotFoundException("Véhicule introuvable"));
		boolean online = motorCutoffService.isVehicleOnline(id);
		boolean connected = motorCutoffService.isVehicleConnected(id);
		return new GpsEngineStatusResponse(
				v.getId(),
				v.getImei(),
				online,
				connected,
				v.getLastSeen(),
				v.getLastSpeed() == null ? 0D : v.getLastSpeed());
	}

	@GetMapping("/vehicles/{id}/stats")
	@PreAuthorize("hasAuthority('REPORT_VIEW') or hasAuthority('FLEET_MANAGE')")
	public GpsVehicleStatsResponse stats(
			@PathVariable UUID id,
			@RequestParam Instant from,
			@RequestParam Instant to) {
		return gpsStatsService.computeStats(id, from, to);
	}

	@GetMapping("/vehicles/{id}/fuel-estimate")
	@PreAuthorize("hasAuthority('REPORT_VIEW') or hasAuthority('FLEET_MANAGE')")
	public void fuelEstimate(@PathVariable UUID id) {
		// Non implémenté : dépend du capteur carburant GT06 (pas encore décodé dans ce sprint).
	}

	private GpsAlertDto toDto(GpsAlert a) {
		return new GpsAlertDto(
				a.getId(),
				a.getVehicle().getId(),
				a.getType(),
				a.getMessage(),
				a.getLatitude(),
				a.getLongitude(),
				a.getSpeed(),
				a.isAcknowledged(),
				a.getCreatedAt());
	}
}

