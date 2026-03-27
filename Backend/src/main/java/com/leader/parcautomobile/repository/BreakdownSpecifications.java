package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.Breakdown;
import com.leader.parcautomobile.entity.BreakdownStatus;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class BreakdownSpecifications {

	private BreakdownSpecifications() {}

	public static Specification<Breakdown> hasStatus(BreakdownStatus status) {
		if (status == null) {
			return (root, q, cb) -> cb.conjunction();
		}
		return (root, q, cb) -> cb.equal(root.get("status"), status);
	}

	public static Specification<Breakdown> vehicleId(UUID vehicleId) {
		if (vehicleId == null) {
			return (root, q, cb) -> cb.conjunction();
		}
		return (root, q, cb) -> cb.equal(root.join("vehicle").get("id"), vehicleId);
	}
}

