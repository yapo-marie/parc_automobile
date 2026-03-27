package com.leader.parcautomobile.service;

import com.leader.parcautomobile.dto.user.ChangePasswordRequest;
import com.leader.parcautomobile.dto.user.CreateUserRequest;
import com.leader.parcautomobile.dto.user.UpdateUserRequest;
import com.leader.parcautomobile.dto.user.UserPageResponse;
import com.leader.parcautomobile.dto.user.UserResponse;
import com.leader.parcautomobile.dto.user.UpdateSelfProfileRequest;
import com.leader.parcautomobile.dto.user.UserStatusPatchRequest;
import com.leader.parcautomobile.entity.Role;
import com.leader.parcautomobile.entity.User;
import com.leader.parcautomobile.entity.UserStatus;
import com.leader.parcautomobile.exception.DuplicateEmailException;
import com.leader.parcautomobile.exception.ResourceNotFoundException;
import com.leader.parcautomobile.mapper.UserMapper;
import com.leader.parcautomobile.repository.RoleRepository;
import com.leader.parcautomobile.repository.UserRepository;
import com.leader.parcautomobile.repository.UserSpecifications;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public UserPageResponse list(String q, UserStatus status, String role, Pageable pageable) {
		Specification<User> spec = Specification.where(UserSpecifications.notDeleted());
		if (q != null && !q.isBlank()) {
			spec = spec.and(UserSpecifications.matchesSearch(q));
		}
		spec = spec.and(UserSpecifications.hasStatus(status));
		if (role != null && !role.isBlank()) {
			spec = spec.and(UserSpecifications.hasRoleName(role));
		}

		Page<User> page = userRepository.findAll(spec, pageable);
		List<UserResponse> content = page.getContent().stream().map(UserMapper::toResponse).toList();
		return new UserPageResponse(
				content,
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages());
	}

	@Transactional(readOnly = true)
	public UserResponse getById(UUID id) {
		User u = userRepository
				.findByIdWithRoles(id)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
		if (u.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Utilisateur introuvable");
		}
		return UserMapper.toResponse(u);
	}

	@Transactional(readOnly = true)
	public UserResponse getByEmail(String email) {
		User u = userRepository
				.findByEmailWithRoles(email)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
		if (u.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Utilisateur introuvable");
		}
		return UserMapper.toResponse(u);
	}

	@Transactional
	public UserResponse create(CreateUserRequest request) {
		String email = request.email().trim().toLowerCase();
		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new DuplicateEmailException("Cet email est déjà utilisé");
		}

		Set<Role> roles = resolveRoles(request.roleNames());
		User u = User.builder()
				.firstname(request.firstname().trim())
				.lastname(request.lastname().trim())
				.email(email)
				.phone(trimToNull(request.phone()))
				.position(trimToNull(request.position()))
				.password(passwordEncoder.encode(request.password()))
				.status(UserStatus.ACTIVE)
				.mustChangePassword(request.requirePasswordChange())
				.roles(roles)
				.build();

		userRepository.save(u);
		return UserMapper.toResponse(
				userRepository.findByIdWithRoles(u.getId()).orElseThrow());
	}

	@Transactional
	public UserResponse update(UUID id, UpdateUserRequest request) {
		User u = requireActiveUser(id);
		u.setFirstname(request.firstname().trim());
		u.setLastname(request.lastname().trim());
		u.setPhone(trimToNull(request.phone()));
		u.setPosition(trimToNull(request.position()));
		u.setRoles(resolveRoles(request.roleNames()));
		userRepository.save(u);
		return UserMapper.toResponse(
				userRepository.findByIdWithRoles(id).orElseThrow());
	}

	@Transactional
	public UserResponse patchStatus(UUID id, UserStatusPatchRequest body) {
		User u = requireActiveUser(id);
		if (body.status() == UserStatus.DELETED) {
			throw new IllegalArgumentException("Utilisez DELETE pour archiver un utilisateur");
		}
		u.setStatus(body.status());
		userRepository.save(u);
		return UserMapper.toResponse(
				userRepository.findByIdWithRoles(id).orElseThrow());
	}

	@Transactional
	public void softDelete(UUID id) {
		User u = userRepository
				.findByIdWithRoles(id)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
		if (u.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Utilisateur introuvable");
		}
		u.setDeletedAt(Instant.now());
		u.setStatus(UserStatus.DELETED);
		userRepository.save(u);
	}

	@Transactional
	public UserResponse updateSelf(String email, UpdateSelfProfileRequest request) {
		User u = userRepository
				.findByEmailWithRoles(email)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
		if (u.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Utilisateur introuvable");
		}
		u.setFirstname(request.firstname().trim());
		u.setLastname(request.lastname().trim());
		u.setPhone(trimToNull(request.phone()));
		u.setPosition(trimToNull(request.position()));
		userRepository.save(u);
		return UserMapper.toResponse(
				userRepository.findByIdWithRoles(u.getId()).orElseThrow());
	}

	@Transactional
	public void changePassword(String email, ChangePasswordRequest request) {
		User u = userRepository
				.findByEmailWithRoles(email)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
		if (u.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Utilisateur introuvable");
		}
		if (!passwordEncoder.matches(request.currentPassword(), u.getPassword())) {
			throw new BadCredentialsException("Mot de passe actuel incorrect");
		}
		u.setPassword(passwordEncoder.encode(request.newPassword()));
		u.setMustChangePassword(false);
		userRepository.save(u);
	}

	private User requireActiveUser(UUID id) {
		User u = userRepository
				.findByIdWithRoles(id)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
		if (u.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Utilisateur introuvable");
		}
		return u;
	}

	private Set<Role> resolveRoles(List<String> roleNames) {
		List<String> normalized = roleNames.stream()
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.distinct()
				.toList();
		if (normalized.isEmpty()) {
			throw new IllegalArgumentException("Au moins un rôle est requis");
		}
		Set<Role> found = new HashSet<>(roleRepository.findByNameIn(normalized));
		if (found.size() != normalized.size()) {
			throw new IllegalArgumentException("Un ou plusieurs rôles sont inconnus");
		}
		return found;
	}

	private static String trimToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}
