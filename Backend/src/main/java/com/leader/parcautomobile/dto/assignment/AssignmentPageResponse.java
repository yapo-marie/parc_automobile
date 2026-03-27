package com.leader.parcautomobile.dto.assignment;

import java.util.List;

public record AssignmentPageResponse(
		List<AssignmentResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages) {}

