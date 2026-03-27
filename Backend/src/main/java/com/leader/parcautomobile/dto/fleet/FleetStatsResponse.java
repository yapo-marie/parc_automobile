package com.leader.parcautomobile.dto.fleet;

import java.math.BigDecimal;

public record FleetStatsResponse(
		long totalFleetVehicles,
		BigDecimal totalAnnualBudget,
		BigDecimal globalBudgetUsedPercent,
		BigDecimal fleetUtilizationPercent) {}
