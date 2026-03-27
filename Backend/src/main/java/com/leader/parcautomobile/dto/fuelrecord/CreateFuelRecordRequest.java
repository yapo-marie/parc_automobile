package com.leader.parcautomobile.dto.fuelrecord;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateFuelRecordRequest(
		@NotNull UUID vehicleId,
		@NotNull LocalDate fillDate,
		@NotNull @DecimalMin("0.01") BigDecimal liters,
		@DecimalMin("0.0") BigDecimal unitPrice,
		@DecimalMin("0.0") BigDecimal totalCost,
		@Min(0) Long mileage,
		@Size(max = 200) String station) {}

