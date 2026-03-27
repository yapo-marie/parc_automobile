package com.leader.parcautomobile.dto.reservation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record CreateReservationRequest(
		@NotNull UUID vehicleId,
		@NotNull Instant startDatetime,
		@NotNull Instant endDatetime,
		@Size(max = 500) String reason,
		@Size(max = 300) String destination,
		@Min(0) Integer estimatedKm,
		@Max(50) Integer passengerCount) {}
