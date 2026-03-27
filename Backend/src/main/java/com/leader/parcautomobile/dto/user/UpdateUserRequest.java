package com.leader.parcautomobile.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateUserRequest(
		@NotBlank @Size(max = 100) String firstname,
		@NotBlank @Size(max = 100) String lastname,
		@Size(max = 20) String phone,
		@Size(max = 100) String position,
		@NotEmpty List<@NotBlank @Size(max = 50) String> roleNames) {}
