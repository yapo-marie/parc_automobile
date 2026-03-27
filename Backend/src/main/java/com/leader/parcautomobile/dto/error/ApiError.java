package com.leader.parcautomobile.dto.error;

import java.time.Instant;
import java.util.List;

public record ApiError(
		String code,
		String message,
		Instant timestamp,
		List<String> details) {

	public static ApiError of(String code, String message, List<String> details) {
		return new ApiError(code, message, Instant.now(), details);
	}
}
