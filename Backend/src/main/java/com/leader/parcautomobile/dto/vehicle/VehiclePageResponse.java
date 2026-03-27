package com.leader.parcautomobile.dto.vehicle;

import java.util.List;

public record VehiclePageResponse(
		List<VehicleResponse> content, int page, int size, long totalElements,
		int totalPages) {}
