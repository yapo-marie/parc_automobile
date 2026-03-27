package com.leader.parcautomobile.dto.assignment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record CreateAssignmentRequest(
		@NotNull UUID vehicleId,
		@NotNull UUID driverId,
		@NotBlank @Size(max = 30) String assignmentType,
		@NotNull Instant startDatetime,
		@NotNull Instant endDatetime,
		@Min(0) Long mileageStart,
		@Min(0) Long mileageEnd,
		@Size(max = 2000) String reason) {

	public CreateAssignmentRequest {
		if (startDatetime != null && endDatetime != null && !endDatetime.isAfter(startDatetime)) {
			throw new IllegalArgumentException("La fin doit être après le début");
		}
	}
}

