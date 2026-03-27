package com.leader.parcautomobile.dto.reservation;

import java.util.List;

public record ReservationPageResponse(
		List<ReservationResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages) {}
