package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.Reservation;
import com.leader.parcautomobile.entity.ReservationStatus;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class ReservationSpecifications {

	private ReservationSpecifications() {}

	public static Specification<Reservation> hasStatus(ReservationStatus status) {
		if (status == null) {
			return (root, q, cb) -> cb.conjunction();
		}
		return (root, q, cb) -> cb.equal(root.get("status"), status);
	}

	public static Specification<Reservation> vehicleId(UUID vehicleId) {
		if (vehicleId == null) {
			return (root, q, cb) -> cb.conjunction();
		}
		return (root, q, cb) -> cb.equal(root.join("vehicle").get("id"), vehicleId);
	}

	public static Specification<Reservation> requestedByEmail(String email) {
		if (email == null || email.isBlank()) {
			return (root, q, cb) -> cb.disjunction();
		}
		return (root, q, cb) ->
				cb.equal(cb.lower(root.join("requestedBy").get("email")), email.trim().toLowerCase());
	}
}
