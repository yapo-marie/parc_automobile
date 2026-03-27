package com.leader.parcautomobile.gps.service;

import com.leader.parcautomobile.entity.Vehicle;
import com.leader.parcautomobile.exception.ResourceNotFoundException;
import com.leader.parcautomobile.repository.VehicleRepository;
import io.netty.channel.Channel;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MotorCutoffService {

	private final VehicleRepository vehicleRepository;

	// IMEI -> Channel TCP (connexion active)
	private final ConcurrentHashMap<String, Channel> activeChannels = new ConcurrentHashMap<>();

	@Value("${gps.commands.engine-cut:DY}")
	private String engineCutCommand;

	@Value("${gps.commands.engine-restore:HF}")
	private String engineRestoreCommand;

	public void registerChannel(String imei, Channel channel) {
		if (imei == null || imei.isBlank() || channel == null) return;
		activeChannels.put(imei, channel);
	}

	public void unregisterChannel(String imei) {
		if (imei == null || imei.isBlank()) return;
		activeChannels.remove(imei);
	}

	public boolean isVehicleOnline(UUID vehicleId) {
		Vehicle v = vehicleRepository
				.findById(vehicleId)
				.orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable"));
		return v.getLastSeen() != null && v.getLastSeen().isAfter(java.time.Instant.now().minusSeconds(600));
	}

	public boolean isVehicleConnected(UUID vehicleId) {
		Vehicle v = vehicleRepository
				.findById(vehicleId)
				.orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable"));
		String imei = v.getImei();
		if (imei == null || imei.isBlank()) return false;
		Channel ch = activeChannels.get(imei);
		return ch != null && ch.isActive();
	}

	public void cutEngine(UUID vehicleId) {
		Vehicle v = vehicleRepository
				.findById(vehicleId)
				.orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable"));
		if (v.getImei() == null || v.getImei().isBlank()) {
			throw new IllegalArgumentException("IMEI manquant pour ce véhicule");
		}
		double speed = v.getLastSpeed() == null ? 0D : v.getLastSpeed();
		// Limitation hardware GT903K (règle du prompt)
		if (speed >= 20D) {
			throw new IllegalArgumentException("Coupure moteur refusée : vitesse >= 20 km/h");
		}
		Channel ch = activeChannels.get(v.getImei());
		if (ch == null || !ch.isActive()) {
			throw new IllegalArgumentException("Véhicule non connecté");
		}

		// TODO: Construire le frame GT06 de commande serveur (recommandé via doc GT06).
		log.warn(
				"[GPS] commande coupure moteur non finalisée (placeholder) pour IMEI {} (cmd={})",
				v.getImei(),
				engineCutCommand);
		// ch.writeAndFlush(buildServerCommand(...));
	}

	public void restoreEngine(UUID vehicleId) {
		Vehicle v = vehicleRepository
				.findById(vehicleId)
				.orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable"));
		if (v.getImei() == null || v.getImei().isBlank()) {
			throw new IllegalArgumentException("IMEI manquant pour ce véhicule");
		}
		Channel ch = activeChannels.get(v.getImei());
		if (ch == null || !ch.isActive()) {
			throw new IllegalArgumentException("Véhicule non connecté");
		}

		// TODO: Construire le frame GT06 de commande serveur (recommandé via doc GT06).
		log.warn(
				"[GPS] commande rétablissement moteur non finalisée (placeholder) pour IMEI {} (cmd={})",
				v.getImei(),
				engineRestoreCommand);
		// ch.writeAndFlush(buildServerCommand(...));
	}
}

