package com.leader.parcautomobile.dto.fuelrecord;

import java.time.Instant;

public record FuelHistoryPointResponse(
		Instant recordedAt,
		Integer fuelLevel,
		Double speed) {}

