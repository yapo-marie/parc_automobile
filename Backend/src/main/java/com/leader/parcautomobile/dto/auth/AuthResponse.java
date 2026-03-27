package com.leader.parcautomobile.dto.auth;

public record AuthResponse(
		String accessToken,
		String refreshToken,
		String tokenType,
		long expiresInSeconds,
		boolean mustChangePassword) {}
