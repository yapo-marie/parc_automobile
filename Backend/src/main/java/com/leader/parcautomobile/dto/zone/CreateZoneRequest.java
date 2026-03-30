package com.leader.parcautomobile.dto.zone;

import com.leader.parcautomobile.entity.ZoneType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateZoneRequest(
		@NotBlank @Size(max = 200) String name,
		@Size(max = 2000) String description,
		@Pattern(regexp = "^#([A-Fa-f0-9]{6})$") String color,
		ZoneType type,
		Double centerLat,
		Double centerLng,
		Integer radiusMeters,
		String polygonCoordinates,
		Integer maxSpeedKmh,
		Boolean active) {}

