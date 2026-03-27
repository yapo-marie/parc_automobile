package com.leader.parcautomobile.dto.fuelrecord;

import java.math.BigDecimal;

public record FuelMonthlySeriesPoint(String yearMonth, BigDecimal avgLitersPer100km, BigDecimal totalLiters) {}
