package com.leader.parcautomobile.dto.fuelrecord;

import java.util.List;

public record FuelRecordPageResponse(
		List<FuelRecordResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages) {}

