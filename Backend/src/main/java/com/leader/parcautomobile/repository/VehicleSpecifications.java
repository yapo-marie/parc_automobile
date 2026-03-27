package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.Vehicle;
import com.leader.parcautomobile.entity.VehicleAvailability;
import com.leader.parcautomobile.entity.VehicleCategory;
import com.leader.parcautomobile.entity.VehicleRecordStatus;
import org.springframework.data.jpa.domain.Specification;

public final class VehicleSpecifications {

	private VehicleSpecifications() {}

	public static Specification<Vehicle> notDeleted() {
		return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
	}

	public static Specification<Vehicle> matchesSearch(String raw) {
		String q = raw.trim().toLowerCase();
		String pattern = "%" + q + "%";
		return (root, query, cb) -> cb.or(
				cb.like(cb.lower(root.get("plateNumber")), pattern),
				cb.like(cb.lower(root.get("brand")), pattern),
				cb.like(cb.lower(root.get("model")), pattern));
	}

	public static Specification<Vehicle> hasCategory(VehicleCategory category) {
		if (category == null) {
			return (root, query, cb) -> cb.conjunction();
		}
		return (root, query, cb) -> cb.equal(root.get("category"), category);
	}

	public static Specification<Vehicle> hasAvailability(VehicleAvailability availability) {
		if (availability == null) {
			return (root, query, cb) -> cb.conjunction();
		}
		return (root, query, cb) -> cb.equal(root.get("availability"), availability);
	}

	public static Specification<Vehicle> hasStatus(VehicleRecordStatus status) {
		if (status == null) {
			return (root, query, cb) -> cb.conjunction();
		}
		return (root, query, cb) -> cb.equal(root.get("status"), status);
	}
}
