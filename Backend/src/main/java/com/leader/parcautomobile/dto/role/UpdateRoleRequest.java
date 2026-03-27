package com.leader.parcautomobile.dto.role;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateRoleRequest(
		@Size(max = 50) @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]*$", message = "Nom invalide (lettres, chiffres, _)")
				String name,
		@Size(max = 2000) String description) {}
