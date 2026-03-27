package com.leader.parcautomobile.dto.vehicle;

import com.leader.parcautomobile.entity.FuelType;
import com.leader.parcautomobile.entity.VehicleAvailability;
import com.leader.parcautomobile.entity.VehicleCategory;
import com.leader.parcautomobile.entity.VehicleRecordStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateVehicleRequest(
		@NotBlank @Size(max = 20) String plateNumber,
		@NotBlank @Size(max = 100) String brand,
		@NotBlank @Size(max = 100) String model,
		@Min(1900) @Max(2100) Integer year,
		@Size(max = 50) String color,
		VehicleCategory category,
		FuelType fuelType,
		@Min(0) Long mileage,
		Integer power,
		Integer seats,
		LocalDate acquisitionDate,
		BigDecimal acquisitionValue,
		LocalDate insuranceExpiry,
		VehicleAvailability availability,
		VehicleRecordStatus status,
		String photoUrl,
		String notes) {}
