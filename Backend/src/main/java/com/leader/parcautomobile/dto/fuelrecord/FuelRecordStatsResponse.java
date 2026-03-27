package com.leader.parcautomobile.dto.fuelrecord;

import java.math.BigDecimal;
import java.util.List;

public record FuelRecordStatsResponse(
		BigDecimal averageLitersPer100km,
		BigDecimal currentMonthLiters,
		BigDecimal currentMonthCost,
		long currentMonthFillCount,
		long totalFillCount,
		List<FuelMonthlySeriesPoint> lastSixMonths) {}
