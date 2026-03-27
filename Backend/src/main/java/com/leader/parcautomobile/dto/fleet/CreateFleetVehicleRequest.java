package com.leader.parcautomobile.dto.fleet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateFleetVehicleRequest(
		@NotNull UUID vehicleId,
		@NotBlank @Size(max = 200) String administration,
		BigDecimal dailyCost,
		BigDecimal costPerKm,
		BigDecimal annualBudget,
		LocalDate startDate,
		LocalDate endDate,
		String notes) {}
