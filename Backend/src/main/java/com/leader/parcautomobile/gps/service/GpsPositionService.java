package com.leader.parcautomobile.gps.service;

import com.leader.parcautomobile.entity.Vehicle;
import com.leader.parcautomobile.gps.dto.GpsPositionDto;
import com.leader.parcautomobile.gps.entity.GpsPosition;
import com.leader.parcautomobile.gps.server.Gt06DecodedMessage;
import com.leader.parcautomobile.gps.repository.GpsPositionRepository;
import com.leader.parcautomobile.gps.websocket.GpsWebSocketBroadcaster;
import com.leader.parcautomobile.repository.VehicleRepository;
import com.leader.parcautomobile.exception.ResourceNotFoundException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GpsPositionService {

	private final VehicleRepository vehicleRepository;
	private final GpsPositionRepository gpsPositionRepository;
	private final GpsAlertService alertService;
	private final GpsWebSocketBroadcaster broadcaster;

	@Transactional
	public void processMessage(Gt06DecodedMessage msg) {
		if (msg.imei() == null || msg.imei().isBlank()) return;

		Vehicle vehicle = vehicleRepository
				.findByImei(msg.imei())
				.orElseThrow(() -> new ResourceNotFoundException("IMEI inconnu : " + msg.imei()));

		Instant now = Instant.now();

		// Alertes doivent pouvoir utiliser la “position précédente” stockée sur le véhicule,
		// donc on exécute la détection AVANT la mise à jour lastLatitude/lastLongitude.
		if (msg.latitude() != null && msg.longitude() != null) {
			alertService.checkAlerts(vehicle, msg);

			GpsPosition pos = GpsPosition.builder()
					.vehicle(vehicle)
					.imei(msg.imei())
					.latitude(msg.latitude())
					.longitude(msg.longitude())
					.speed(msg.speed() == null ? 0D : msg.speed())
					.heading(msg.heading() == null ? 0 : msg.heading())
					.altitude(msg.altitude() == null ? 0D : msg.altitude())
					.satellites(msg.satellites() == null ? 0 : msg.satellites())
					.accuracy(msg.accuracy() == null ? 0D : msg.accuracy())
					.ignition(msg.ignitionOn() != null ? msg.ignitionOn() : vehicle.isIgnitionOn())
					.fuelLevel(msg.fuelLevel())
					.recordedAt(msg.timestamp() != null ? msg.timestamp() : now)
					.createdAt(now)
					.build();
			gpsPositionRepository.save(pos);

			vehicle.setLastLatitude(msg.latitude());
			vehicle.setLastLongitude(msg.longitude());
			vehicle.setLastSpeed(pos.getSpeed());
			vehicle.setLastSeen(pos.getRecordedAt());
			vehicle.setIgnitionOn(pos.isIgnition());
			if (msg.fuelLevel() != null) {
				vehicle.setFuelLevel(msg.fuelLevel());
			}
			vehicleRepository.save(vehicle);

			GpsPositionDto dto = new GpsPositionDto(
					vehicle.getId(),
					vehicle.getPlateNumber(),
					msg.imei(),
					pos.getLatitude(),
					pos.getLongitude(),
					pos.getSpeed(),
					pos.getHeading(),
					vehicle.isIgnitionOn(),
					pos.getRecordedAt());
			broadcaster.sendPosition(dto);
			return;
		}

		// Messages “status/alarm” sans coordonnées : on met à jour l’horodatage + ignition.
		if (msg.ignitionOn() != null) {
			vehicle.setIgnitionOn(msg.ignitionOn());
		}
		vehicle.setLastSeen(msg.timestamp() != null ? msg.timestamp() : now);
		vehicleRepository.save(vehicle);
	}
}

