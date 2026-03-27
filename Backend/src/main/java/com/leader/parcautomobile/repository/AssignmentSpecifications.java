package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.Assignment;
import com.leader.parcautomobile.entity.AssignmentStatus;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class AssignmentSpecifications {

	private AssignmentSpecifications() {}

	public static Specification<Assignment> hasStatus(AssignmentStatus status) {
		if (status == null) {
			return (root, q, cb) -> cb.conjunction();
		}
		return (root, q, cb) -> cb.equal(root.get("status"), status);
	}

	public static Specification<Assignment> vehicleId(UUID vehicleId) {
		if (vehicleId == null) {
			return (root, q, cb) -> cb.conjunction();
		}
		return (root, q, cb) -> cb.equal(root.join("vehicle").get("id"), vehicleId);
	}

	public static Specification<Assignment> driverId(UUID driverId) {
		if (driverId == null) {
			return (root, q, cb) -> cb.conjunction();
		}
		return (root, q, cb) -> cb.equal(root.join("driver").get("id"), driverId);
	}

	public static Specification<Assignment> driverEmail(String email) {
		if (email == null || email.isBlank()) {
			return (root, q, cb) -> cb.disjunction();
		}
		return (root, q, cb) -> cb.equal(
				cb.lower(root.join("driver").get("email")),
				email.trim().toLowerCase());
	}
}

