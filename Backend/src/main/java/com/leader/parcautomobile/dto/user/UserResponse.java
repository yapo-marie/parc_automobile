package com.leader.parcautomobile.dto.user;

import com.leader.parcautomobile.entity.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResponse(
		UUID id,
		String firstname,
		String lastname,
		String email,
		String phone,
		String position,
		UserStatus status,
		boolean mustChangePassword,
		Instant createdAt,
		Instant lastLogin,
		List<String> roleNames) {}
