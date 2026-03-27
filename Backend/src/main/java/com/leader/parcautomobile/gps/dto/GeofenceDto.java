package com.leader.parcautomobile.gps.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record GeofenceDto(
		UUID id,
		String name,
		String description,
		double centerLat,
		double centerLng,
		int radiusM,
		boolean active,
		Set<UUID> vehicleIds,
		Instant createdAt) {}

