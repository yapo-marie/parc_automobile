package com.leader.parcautomobile.gps.dto;

import java.time.Instant;
import java.util.UUID;

public record GpsEngineStatusResponse(
		UUID vehicleId,
		String imei,
		boolean online,
		boolean connected,
		Instant lastSeen,
		double lastSpeed) {}

