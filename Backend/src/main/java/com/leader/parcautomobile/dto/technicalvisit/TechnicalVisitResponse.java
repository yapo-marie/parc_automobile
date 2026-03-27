package com.leader.parcautomobile.dto.technicalvisit;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TechnicalVisitResponse(
		UUID id,
		UUID vehicleId,
		String vehiclePlate,
		String vehicleLabel,
		String type,
		LocalDate scheduledDate,
		LocalDate completedDate,
		String result,
		String garage,
		BigDecimal cost,
		LocalDate nextDueDate,
		String comments,
		Instant createdAt) {}

