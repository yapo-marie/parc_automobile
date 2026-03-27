package com.leader.parcautomobile.gps.dto;

import com.leader.parcautomobile.gps.entity.AlertType;
import java.time.Instant;
import java.util.UUID;

public record GpsAlertDto(
		UUID id,
		UUID vehicleId,
		AlertType type,
		String message,
		Double latitude,
		Double longitude,
		Double speed,
		boolean acknowledged,
		Instant createdAt) {}

