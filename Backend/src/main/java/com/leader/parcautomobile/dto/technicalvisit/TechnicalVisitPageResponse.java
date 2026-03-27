package com.leader.parcautomobile.dto.technicalvisit;

import java.util.List;

public record TechnicalVisitPageResponse(
		List<TechnicalVisitResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages) {}

