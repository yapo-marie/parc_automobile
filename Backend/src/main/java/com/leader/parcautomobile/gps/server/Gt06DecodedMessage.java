package com.leader.parcautomobile.gps.server;

import com.leader.parcautomobile.gps.entity.AlertType;
import java.time.Instant;

/**
 * Représentation interne d'une trame GT06 décodée.
 * Champs optionnels car certains types de messages n'embarquent pas toutes les infos.
 */
public record Gt06DecodedMessage(
		String imei,
		Instant timestamp,
		Double latitude,
		Double longitude,
		Double speed,
		Integer heading,
		Double altitude,
		Integer satellites,
		Double accuracy,
		Boolean ignitionOn,
		Integer fuelLevel,
		AlertType alarmType,
		int messageTypeCode) {
}

