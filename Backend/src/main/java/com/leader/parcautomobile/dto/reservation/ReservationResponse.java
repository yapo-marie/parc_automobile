package com.leader.parcautomobile.dto.reservation;

import com.leader.parcautomobile.entity.ReservationStatus;
import java.time.Instant;
import java.util.UUID;

public record ReservationResponse(
		UUID id,
		UUID vehicleId,
		String vehiclePlate,
		String vehicleLabel,
		UUID requesterId,
		String requesterEmail,
		String requesterFirstname,
		String requesterLastname,
		Instant startDatetime,
		Instant endDatetime,
		String reason,
		String destination,
		Integer estimatedKm,
		Integer passengerCount,
		ReservationStatus status,
		UUID confirmedById,
		String confirmedByEmail,
		Instant confirmedAt,
		String rejectionReason,
		Instant createdAt) {}
