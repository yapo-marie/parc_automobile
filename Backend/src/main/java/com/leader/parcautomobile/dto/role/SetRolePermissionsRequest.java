package com.leader.parcautomobile.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SetRolePermissionsRequest(
		@NotNull List<@NotBlank @Size(max = 100) String> permissionCodes) {}
