package com.leader.parcautomobile.dto.breakdown;

import java.util.List;

public record BreakdownPageResponse(
		List<BreakdownResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages) {}

