package com.leader.parcautomobile.dto.reservation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectReservationRequest(
		@NotBlank @Size(max = 2000) String reason) {}
