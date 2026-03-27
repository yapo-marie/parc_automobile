package com.leader.parcautomobile.security;

import com.leader.parcautomobile.entity.Role;
import com.leader.parcautomobile.entity.User;
import com.leader.parcautomobile.entity.UserStatus;
import com.leader.parcautomobile.repository.UserRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository
				.findByEmailWithRoles(email)
				.orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
		if (user.getDeletedAt() != null) {
			throw new UsernameNotFoundException("Utilisateur introuvable");
		}
		boolean timeLocked =
				user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now());
		boolean locked = user.getStatus() == UserStatus.LOCKED || timeLocked;
		boolean enabled = user.getStatus() == UserStatus.ACTIVE && !locked;

		Set<GrantedAuthority> authorities = user.getRoles().stream()
				.flatMap(r -> r.getPermissions().stream())
				.map(p -> new SimpleGrantedAuthority(p.getCode()))
				.collect(Collectors.toCollection(HashSet::new));

		for (Role r : user.getRoles()) {
			authorities.add(new SimpleGrantedAuthority("ROLE_" + r.getName()));
		}

		return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
				.password(user.getPassword())
				.disabled(!enabled)
				.accountLocked(locked)
				.authorities(authorities)
				.build();
	}
}
