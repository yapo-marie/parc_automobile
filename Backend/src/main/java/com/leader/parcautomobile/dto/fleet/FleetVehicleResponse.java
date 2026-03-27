package com.leader.parcautomobile.dto.fleet;

import com.leader.parcautomobile.entity.VehicleAvailability;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FleetVehicleResponse(
		UUID id,
		UUID vehicleId,
		String plateNumber,
		String brand,
		String model,
		VehicleAvailability vehicleAvailability,
		String administration,
		BigDecimal dailyCost,
		BigDecimal costPerKm,
		BigDecimal annualBudget,
		LocalDate startDate,
		LocalDate endDate,
		String notes,
		Instant createdAt,
		Instant updatedAt) {}
