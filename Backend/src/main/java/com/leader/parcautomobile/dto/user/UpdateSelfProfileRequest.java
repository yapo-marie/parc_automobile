package com.leader.parcautomobile.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSelfProfileRequest(
		@NotBlank @Size(max = 100) String firstname,
		@NotBlank @Size(max = 100) String lastname,
		@Size(max = 20) String phone,
		@Size(max = 100) String position) {}
