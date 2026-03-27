package com.leader.parcautomobile.dto.user;

import com.leader.parcautomobile.entity.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UserStatusPatchRequest(@NotNull UserStatus status) {}
