package com.leader.parcautomobile.dto.breakdown;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateBreakdownRequest(
		@NotNull UUID vehicleId,
		@NotBlank @Size(max = 5000) String description,
		@Min(0) Long mileageAtBreakdown,
		@Size(max = 200) String garage,
		@Min(0) BigDecimal repairCost) {}

