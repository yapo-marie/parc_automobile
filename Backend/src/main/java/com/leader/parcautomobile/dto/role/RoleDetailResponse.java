package com.leader.parcautomobile.dto.role;

import java.util.List;
import java.util.UUID;

public record RoleDetailResponse(
		UUID id,
		String name,
		String description,
		List<String> permissionCodes,
		long assignedUserCount) {}
