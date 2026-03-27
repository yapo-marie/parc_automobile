package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.FleetVehicle;
import jakarta.persistence.criteria.JoinType;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class FleetVehicleSpecifications {

	private FleetVehicleSpecifications() {
	}

	public static Specification<FleetVehicle> notDeleted() {
		return (root, q, cb) -> cb.isNull(root.get("deletedAt"));
	}

	public static Specification<FleetVehicle> administrationContains(String administration) {
		if (administration == null || administration.isBlank()) {
			return (root, q, cb) -> cb.conjunction();
		}
		String pattern = "%" + administration.trim().toLowerCase() + "%";
		return (root, q, cb) -> cb.like(cb.lower(root.get("administration")), pattern);
	}

	public static Specification<FleetVehicle> vehicleId(UUID vehicleId) {
		if (vehicleId == null) {
			return (root, q, cb) -> cb.conjunction();
		}
		return (root, q, cb) -> {
			var v = root.join("vehicle", JoinType.INNER);
			return cb.equal(v.get("id"), vehicleId);
		};
	}
}
