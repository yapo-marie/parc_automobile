package com.leader.parcautomobile.dto.breakdown;

import com.leader.parcautomobile.entity.BreakdownStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BreakdownResponse(
		UUID id,
		UUID vehicleId,
		String vehiclePlate,
		String vehicleLabel,
		UUID declaredById,
		String declaredByEmail,
		String description,
		Instant declaredAt,
		Long mileageAtBreakdown,
		String garage,
		BigDecimal repairCost,
		Instant resolvedAt,
		BreakdownStatus status) {}

