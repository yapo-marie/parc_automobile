package com.leader.parcautomobile.dto.technicalvisit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTechnicalVisitRequest(
		@NotNull UUID vehicleId,
		@NotBlank @Size(max = 50) String type,
		@NotNull LocalDate scheduledDate,
		LocalDate completedDate,
		@Size(max = 30) String result,
		@Size(max = 200) String garage,
		BigDecimal cost,
		LocalDate nextDueDate,
		String comments) {}

