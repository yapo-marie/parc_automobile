package com.leader.parcautomobile.dto.vehicle;

import com.leader.parcautomobile.entity.VehicleAvailability;
import jakarta.validation.constraints.NotNull;

public record AvailabilityPatchRequest(@NotNull VehicleAvailability availability) {}
