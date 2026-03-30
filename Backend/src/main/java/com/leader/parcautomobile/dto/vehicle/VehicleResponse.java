package com.leader.parcautomobile.dto.vehicle;

import com.leader.parcautomobile.entity.FuelType;
import com.leader.parcautomobile.entity.VehicleAvailability;
import com.leader.parcautomobile.entity.VehicleCategory;
import com.leader.parcautomobile.entity.VehicleRecordStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record VehicleResponse(
		UUID id,
		String plateNumber,
		String brand,
		String model,
		Integer year,
		String color,
		VehicleCategory category,
		FuelType fuelType,
		long mileage,
		Integer power,
		Integer seats,
		LocalDate acquisitionDate,
		BigDecimal acquisitionValue,
		LocalDate insuranceExpiry,
		VehicleAvailability availability,
		VehicleRecordStatus status,
		String photoUrl,
		String notes,
		String imei,
		Integer fuelLevel,
		Instant createdAt,
		Instant updatedAt) {}
