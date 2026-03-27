package com.leader.parcautomobile.gps.service;

import com.leader.parcautomobile.gps.dto.GpsAlertDto;
import com.leader.parcautomobile.gps.entity.AlertType;
import com.leader.parcautomobile.gps.entity.GpsAlert;
import com.leader.parcautomobile.gps.entity.Geofence;
import com.leader.parcautomobile.gps.server.Gt06DecodedMessage;
import com.leader.parcautomobile.gps.repository.GpsAlertRepository;
import com.leader.parcautomobile.gps.repository.GeofenceRepository;
import com.leader.parcautomobile.gps.websocket.GpsWebSocketBroadcaster;
import com.leader.parcautomobile.entity.Vehicle;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GpsAlertService {

	private final GpsAlertRepository gpsAlertRepository;
	private final GeofenceRepository geofenceRepository;
	private final GpsWebSocketBroadcaster broadcaster;

	@Value("${gps.alerts.default-max-speed:120}")
	private double defaultMaxSpeed;

	@Transactional
	public void checkAlerts(Vehicle vehicle, Gt06DecodedMessage msg) {
		UUID vehicleId = vehicle.getId();

		if (msg.speed() != null && msg.speed() > defaultMaxSpeed) {
			createAlert(
					vehicle,
					AlertType.OVERSPEED,
					"Vitesse excessive : " + Math.round(msg.speed()) + " km/h (max " + (int) defaultMaxSpeed + ")",
					msg);
		}

		// Alarmes GT06 (collision / power cut / SOS)
		if (msg.alarmType() != null) {
			switch (msg.alarmType()) {
				case COLLISION -> createAlert(vehicle, AlertType.COLLISION, "Choc / collision détecté", msg);
				case POWER_CUT -> createAlert(vehicle, AlertType.POWER_CUT, "Alimentation coupée (vol potentiel)", msg);
				case LOW_FUEL -> createAlert(vehicle, AlertType.LOW_FUEL, "Niveau bas (alarme GT06)", msg);
				case SOS -> createAlert(vehicle, AlertType.SOS, "URGENCE SOS déclenché dans le véhicule", msg);
				default -> {
					// LOW_FUEL mapping possible si l’alarme/le capteur existe
				}
			}
		}

		// Géofence enter/exit (si on a une nouvelle position)
		if (msg.latitude() != null && msg.longitude() != null) {
			List<Geofence> fences = geofenceRepository.findActiveByVehicleId(vehicleId);
			Double prevLat = vehicle.getLastLatitude();
			Double prevLng = vehicle.getLastLongitude();
			if (prevLat == null || prevLng == null) {
				return;
			}
			for (Geofence fence : fences) {
				boolean wasInside = haversineMeters(prevLat, prevLng, fence.getCenterLat(), fence.getCenterLng())
						<= fence.getRadiusM();
				boolean inside = haversineMeters(msg.latitude(), msg.longitude(), fence.getCenterLat(), fence.getCenterLng())
						<= fence.getRadiusM();

				if (wasInside && !inside) {
					createAlert(vehicle, AlertType.GEOFENCE_EXIT, "Sortie de zone : " + fence.getName(), msg);
				} else if (!wasInside && inside) {
					createAlert(vehicle, AlertType.GEOFENCE_ENTER, "Entrée dans zone : " + fence.getName(), msg);
				}
			}
		}
	}

	private void createAlert(Vehicle vehicle, AlertType type, String message, Gt06DecodedMessage msg) {
		GpsAlert a = GpsAlert.builder()
				.vehicle(vehicle)
				.type(type)
				.message(message)
				.latitude(msg.latitude())
				.longitude(msg.longitude())
				.speed(msg.speed())
				.acknowledged(false)
				.acknowledgedBy(null)
				.acknowledgedAt(null)
				.createdAt(Instant.now())
				.build();
		GpsAlert saved = gpsAlertRepository.save(a);
		GpsAlertDto dto = new GpsAlertDto(
				saved.getId(),
				vehicle.getId(),
				saved.getType(),
				saved.getMessage(),
				saved.getLatitude(),
				saved.getLongitude(),
				saved.getSpeed(),
				saved.isAcknowledged(),
				saved.getCreatedAt());
		broadcaster.sendAlert(dto);
	}

	private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
		final int R = 6371000;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
				* Math.sin(dLon / 2) * Math.sin(dLon / 2);
		return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	}
}

