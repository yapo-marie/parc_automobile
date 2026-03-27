package com.leader.parcautomobile.dto.reservation;

import jakarta.validation.constraints.Size;

public record CancelReservationRequest(@Size(max = 2000) String reason) {}
