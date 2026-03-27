package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.FuelRecord;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class FuelRecordSpecifications {

	private FuelRecordSpecifications() {}

	public static Specification<FuelRecord> vehicleId(UUID vehicleId) {
		if (vehicleId == null) {
			return (root, q, cb) -> cb.conjunction();
		}
		return (root, q, cb) -> cb.equal(root.join("vehicle").get("id"), vehicleId);
	}

	public static Specification<FuelRecord> fromDate(LocalDate from) {
		if (from == null) {
			return (root, q, cb) -> cb.conjunction();
		}
		return (root, q, cb) -> cb.greaterThanOrEqualTo(root.get("fillDate"), from);
	}

	public static Specification<FuelRecord> toDate(LocalDate to) {
		if (to == null) {
			return (root, q, cb) -> cb.conjunction();
		}
		return (root, q, cb) -> cb.lessThanOrEqualTo(root.get("fillDate"), to);
	}
}

