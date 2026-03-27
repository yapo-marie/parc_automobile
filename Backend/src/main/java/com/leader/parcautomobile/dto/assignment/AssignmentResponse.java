package com.leader.parcautomobile.dto.assignment;

import com.leader.parcautomobile.entity.AssignmentStatus;
import java.time.Instant;
import java.util.UUID;

public record AssignmentResponse(
		UUID id,
		UUID vehicleId,
		String vehiclePlate,
		String vehicleLabel,
		UUID driverId,
		String driverEmail,
		String driverFirstname,
		String driverLastname,
		Instant startDatetime,
		Instant endDatetime,
		String assignmentType,
		Long mileageStart,
		Long mileageEnd,
		String reason,
		AssignmentStatus status,
		Instant withdrawnAt) {}

