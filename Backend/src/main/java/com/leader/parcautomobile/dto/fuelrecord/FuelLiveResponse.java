package com.leader.parcautomobile.dto.fuelrecord;

import java.time.Instant;
import java.util.UUID;

public record FuelLiveResponse(
		UUID vehicleId,
		String plateNumber,
		String vehicleLabel,
		String photoUrl,
		Integer fuelLevel,
		Instant lastSeen) {}

