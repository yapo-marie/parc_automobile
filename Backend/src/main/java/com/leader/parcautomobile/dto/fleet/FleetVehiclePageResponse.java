package com.leader.parcautomobile.dto.fleet;

import java.util.List;

public record FleetVehiclePageResponse(
		List<FleetVehicleResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages) {}
