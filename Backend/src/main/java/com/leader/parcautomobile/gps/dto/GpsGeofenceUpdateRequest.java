package com.leader.parcautomobile.gps.dto;

import jakarta.validation.constraints.Size;

public record GpsGeofenceUpdateRequest(
		@Size(max = 200) String name,
		@Size(max = 2000) String description,
		double centerLat,
		double centerLng,
		int radiusM,
		boolean active) {}

