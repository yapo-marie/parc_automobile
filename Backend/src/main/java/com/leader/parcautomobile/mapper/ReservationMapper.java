package com.leader.parcautomobile.mapper;

import com.leader.parcautomobile.dto.reservation.ReservationResponse;
import com.leader.parcautomobile.entity.Reservation;
import com.leader.parcautomobile.entity.User;
import com.leader.parcautomobile.entity.Vehicle;

public final class ReservationMapper {

	private ReservationMapper() {}

	public static ReservationResponse toResponse(Reservation r) {
		Vehicle v = r.getVehicle();
		User req = r.getRequestedBy();
		User conf = r.getConfirmedBy();
		String label = (v.getBrand() != null ? v.getBrand() : "") + " " + (v.getModel() != null ? v.getModel() : "");
		return new ReservationResponse(
				r.getId(),
				v.getId(),
				v.getPlateNumber(),
				label.trim(),
				req.getId(),
				req.getEmail(),
				req.getFirstname(),
				req.getLastname(),
				r.getStartDatetime(),
				r.getEndDatetime(),
				r.getReason(),
				r.getDestination(),
				r.getEstimatedKm(),
				r.getPassengerCount(),
				r.getStatus(),
				conf != null ? conf.getId() : null,
				conf != null ? conf.getEmail() : null,
				r.getConfirmedAt(),
				r.getRejectionReason(),
				r.getCreatedAt());
	}
}
