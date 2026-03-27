package com.leader.parcautomobile.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateRoleRequest(
		@NotBlank @Size(max = 50) @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]*$", message = "Nom invalide (lettres, chiffres, _)")
				String name,
		@Size(max = 2000) String description,
		List<@NotBlank @Size(max = 100) String> permissionCodes) {}
