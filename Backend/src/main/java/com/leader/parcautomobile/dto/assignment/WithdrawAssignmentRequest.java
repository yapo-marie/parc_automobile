package com.leader.parcautomobile.dto.assignment;

import jakarta.validation.constraints.Size;

public record WithdrawAssignmentRequest(@Size(max = 2000) String reason) {}

