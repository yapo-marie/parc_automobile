package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.TechnicalVisit;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class TechnicalVisitSpecifications {

	private TechnicalVisitSpecifications() {}

	public static Specification<TechnicalVisit> vehicleId(UUID vehicleId) {
		if (vehicleId == null) {
			return (root, q, cb) -> cb.conjunction();
		}
		return (root, q, cb) -> cb.equal(root.join("vehicle").get("id"), vehicleId);
	}

	public static Specification<TechnicalVisit> type(String type) {
		if (type == null || type.isBlank()) {
			return (root, q, cb) -> cb.conjunction();
		}
		return (root, q, cb) -> cb.equal(root.get("type"), type.trim());
	}

	public static Specification<TechnicalVisit> result(String result) {
		if (result == null || result.isBlank()) {
			return (root, q, cb) -> cb.conjunction();
		}
		return (root, q, cb) -> cb.equal(root.get("result"), result.trim());
	}
}

