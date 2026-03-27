package com.leader.parcautomobile.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
		@NotBlank String token,
		@NotBlank
				@Pattern(
						regexp =
								"^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$",
						message =
								"Le mot de passe doit avoir au moins 8 caractères, une majuscule, un chiffre et un caractère spécial")
				String newPassword) {}
