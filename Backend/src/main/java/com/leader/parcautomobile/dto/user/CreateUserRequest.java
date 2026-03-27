package com.leader.parcautomobile.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateUserRequest(
		@NotBlank @Size(max = 100) String firstname,
		@NotBlank @Size(max = 100) String lastname,
		@NotBlank @Email @Size(max = 255) String email,
		@Size(max = 20) String phone,
		@Size(max = 100) String position,
		@NotBlank
				@Pattern(
						regexp =
								"^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$",
						message =
								"Le mot de passe doit avoir au moins 8 caractères, une majuscule, un chiffre et un caractère spécial")
				String password,
		@NotEmpty List<@NotBlank @Size(max = 50) String> roleNames,
		Boolean mustChangePassword) {

	public boolean requirePasswordChange() {
		return mustChangePassword == null || mustChangePassword;
	}
}
