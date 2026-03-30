package com.leader.parcautomobile.gps.service;

import com.leader.parcautomobile.gps.dto.GpsAlertDto;
import com.leader.parcautomobile.gps.entity.AlertType;
import com.leader.parcautomobile.gps.entity.GpsAlert;
import com.leader.parcautomobile.gps.entity.Geofence;
import com.leader.parcautomobile.gps.server.Gt06DecodedMessage;
import com.leader.parcautomobile.entity.NotificationType;
import com.leader.parcautomobile.entity.Zone;
import com.leader.parcautomobile.gps.repository.GpsAlertRepository;
import com.leader.parcautomobile.gps.repository.GeofenceRepository;
import com.leader.parcautomobile.gps.websocket.GpsWebSocketBroadcaster;
import com.leader.parcautomobile.entity.Vehicle;
import com.leader.parcautomobile.repository.UserRepository;
import com.leader.parcautomobile.repository.ZoneRepository;
import com.leader.parcautomobile.service.NotificationService;
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
	private final ZoneRepository zoneRepository;
	private final GpsWebSocketBroadcaster broadcaster;
	private final NotificationService notificationService;
	private final UserRepository userRepository;

	@Value("${gps.alerts.default-max-speed:120}")
	private double defaultMaxSpeed;
	@Value("${gps.alerts.fuel-alert-threshold:15}")
	private int fuelAlertThreshold;

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
		if (msg.fuelLevel() != null && msg.fuelLevel() < fuelAlertThreshold) {
			createAlert(vehicle, AlertType.LOW_FUEL, "Niveau carburant bas : " + msg.fuelLevel() + "%", msg);
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

			checkZoneAlerts(vehicle, msg);
		}
	}

	private void checkZoneAlerts(Vehicle vehicle, Gt06DecodedMessage msg) {
		if (msg.latitude() == null || msg.longitude() == null) return;
		List<Zone> zones = zoneRepository.findActiveByVehicleId(vehicle.getId());
		Double prevLat = vehicle.getLastLatitude();
		Double prevLng = vehicle.getLastLongitude();
		for (Zone zone : zones) {
			if (zone.getCenterLat() == null || zone.getCenterLng() == null || zone.getRadiusMeters() == null) continue;
			boolean inside = haversineMeters(msg.latitude(), msg.longitude(), zone.getCenterLat(), zone.getCenterLng())
					<= zone.getRadiusMeters();
			boolean wasInside = prevLat != null && prevLng != null
					&& haversineMeters(prevLat, prevLng, zone.getCenterLat(), zone.getCenterLng()) <= zone.getRadiusMeters();

			if (wasInside && !inside) {
				createAlert(vehicle, AlertType.ZONE_EXIT, "Sortie de zone : " + zone.getName(), msg);
				sendManagersNotification(
						"Sortie de zone",
						vehicle.getPlateNumber() + " a quitté " + zone.getName(),
						"/gps/alerts");
			} else if (!wasInside && inside) {
				createAlert(vehicle, AlertType.ZONE_ENTER, "Entrée dans zone : " + zone.getName(), msg);
			}

			if (inside && zone.getMaxSpeedKmh() != null && zone.getMaxSpeedKmh() > 0
					&& msg.speed() != null && msg.speed() > zone.getMaxSpeedKmh()) {
				createAlert(
						vehicle,
						AlertType.ZONE_OVERSPEED,
						"Vitesse excessive dans " + zone.getName() + " : " + Math.round(msg.speed())
								+ " km/h (max " + zone.getMaxSpeedKmh() + ")",
						msg);
			}
		}
	}

	private void sendManagersNotification(String title, String message, String link) {
		List<String> emails = new java.util.ArrayList<>();
		emails.addAll(userRepository.findActiveEmailsByRoleName("SUPER_ADMIN"));
		emails.addAll(userRepository.findActiveEmailsByRoleName("ADMIN"));
		emails.addAll(userRepository.findActiveEmailsByRoleName("FLEET_MANAGER"));
		emails.stream().distinct().forEach(email -> userRepository.findByEmailWithRoles(email).ifPresent(
				u -> notificationService.send(u.getId(), title, message, NotificationType.ALERT_GPS, link)));
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

