package com.leader.parcautomobile.dto.zone;

import com.leader.parcautomobile.entity.ZoneType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ZoneResponse(
		UUID id,
		String name,
		String description,
		String color,
		ZoneType type,
		Double centerLat,
		Double centerLng,
		Integer radiusMeters,
		String polygonCoordinates,
		Integer maxSpeedKmh,
		boolean active,
		List<UUID> vehicleIds,
		Instant createdAt) {}

