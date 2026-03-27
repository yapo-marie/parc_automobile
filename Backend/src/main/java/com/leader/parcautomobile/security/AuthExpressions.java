package com.leader.parcautomobile.security;

import com.leader.parcautomobile.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("authUser")
@RequiredArgsConstructor
public class AuthExpressions {

	private final UserRepository userRepository;

	public boolean isSelf(Authentication authentication, UUID id) {
		if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails ud)) {
			return false;
		}
		return userRepository
				.findByEmailWithRoles(ud.getUsername())
				.map(u -> u.getId().equals(id))
				.orElse(false);
	}
}
