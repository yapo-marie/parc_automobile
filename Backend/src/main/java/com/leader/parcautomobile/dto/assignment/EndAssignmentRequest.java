package com.leader.parcautomobile.dto.assignment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record EndAssignmentRequest(
		@NotNull Instant endDatetime,
		@Size(max = 2000) String reason) {}

