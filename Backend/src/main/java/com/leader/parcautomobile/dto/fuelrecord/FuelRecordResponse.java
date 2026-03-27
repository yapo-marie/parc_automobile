package com.leader.parcautomobile.dto.fuelrecord;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FuelRecordResponse(
		UUID id,
		UUID vehicleId,
		String vehiclePlate,
		String vehicleLabel,
		UUID filledById,
		String filledByEmail,
		LocalDate fillDate,
		BigDecimal liters,
		BigDecimal unitPrice,
		BigDecimal totalCost,
		Long mileage,
		String station,
		Instant createdAt) {}

