package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.User;
import com.leader.parcautomobile.entity.UserStatus;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecifications {

	private UserSpecifications() {}

	public static Specification<User> notDeleted() {
		return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
	}

	public static Specification<User> matchesSearch(String raw) {
		String q = raw.trim().toLowerCase();
		String pattern = "%" + q + "%";
		return (root, query, cb) -> cb.or(
				cb.like(cb.lower(root.get("firstname")), pattern),
				cb.like(cb.lower(root.get("lastname")), pattern),
				cb.like(cb.lower(root.get("email")), pattern));
	}

	public static Specification<User> hasStatus(UserStatus status) {
		if (status == null) {
			return (root, query, cb) -> cb.conjunction();
		}
		return (root, query, cb) -> cb.equal(root.get("status"), status);
	}

	public static Specification<User> hasRoleName(String roleName) {
		if (roleName == null || roleName.isBlank()) {
			return (root, query, cb) -> cb.conjunction();
		}
		return (root, query, cb) -> {
			if (query != null) {
				query.distinct(true);
			}
			var rolesJoin = root.join("roles");
			return cb.equal(rolesJoin.get("name"), roleName.trim());
		};
	}
}
