package com.leader.parcautomobile.mapper;

import com.leader.parcautomobile.dto.user.UserResponse;
import com.leader.parcautomobile.entity.Role;
import com.leader.parcautomobile.entity.User;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class UserMapper {

	private UserMapper() {}

	public static UserResponse toResponse(User u) {
		Set<Role> roleSet = u.getRoles();
		List<String> roles = (roleSet == null ? Set.<Role>of() : roleSet).stream()
				.map(Role::getName)
				.sorted(Comparator.naturalOrder())
				.toList();
		return new UserResponse(
				u.getId(),
				u.getFirstname(),
				u.getLastname(),
				u.getEmail(),
				u.getPhone(),
				u.getPosition(),
				u.getStatus(),
				u.isMustChangePassword(),
				u.getCreatedAt(),
				u.getLastLogin(),
				roles);
	}
}
