package com.leader.parcautomobile.gps.dto;

import java.time.Instant;
import java.util.UUID;

public record GpsPositionDto(
		UUID vehicleId,
		String vehiclePlate,
		String imei,
		double latitude,
		double longitude,
		double speed,
		int heading,
		boolean ignitionOn,
		Instant recordedAt) {}

