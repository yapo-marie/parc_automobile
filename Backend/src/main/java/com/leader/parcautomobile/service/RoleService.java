package com.leader.parcautomobile.service;

import com.leader.parcautomobile.dto.role.CreateRoleRequest;
import com.leader.parcautomobile.dto.role.RoleDetailResponse;
import com.leader.parcautomobile.dto.role.SetRolePermissionsRequest;
import com.leader.parcautomobile.dto.role.UpdateRoleRequest;
import com.leader.parcautomobile.entity.Permission;
import com.leader.parcautomobile.entity.Role;
import com.leader.parcautomobile.exception.DuplicateRoleNameException;
import com.leader.parcautomobile.exception.ResourceNotFoundException;
import com.leader.parcautomobile.repository.PermissionRepository;
import com.leader.parcautomobile.repository.RoleRepository;
import com.leader.parcautomobile.repository.UserRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleService {

	private static final Set<String> SEEDED_ROLE_NAMES = Set.of(
			"SUPER_ADMIN", "ADMIN", "FLEET_MANAGER", "DRIVER", "VIEWER");

	private final RoleRepository roleRepository;
	private final PermissionRepository permissionRepository;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public List<RoleDetailResponse> listAll() {
		List<Role> roles = roleRepository.findAllWithPermissions();
		roles.sort(Comparator.comparing(Role::getName, String.CASE_INSENSITIVE_ORDER));
		return roles.stream().map(this::toDetail).toList();
	}

	@Transactional(readOnly = true)
	public RoleDetailResponse getById(UUID id) {
		return toDetail(requireWithPermissions(id));
	}

	@Transactional
	public RoleDetailResponse create(CreateRoleRequest body) {
		String name = normalizeRoleName(body.name());
		if (roleRepository.findByName(name).isPresent()) {
			throw new DuplicateRoleNameException("Ce nom de rôle existe déjà");
		}
		Set<Permission> permissions = resolvePermissions(codesOrEmpty(body.permissionCodes()));
		Role r = Role.builder()
				.name(name)
				.description(trimToNull(body.description()))
				.createdAt(Instant.now())
				.permissions(permissions)
				.build();
		Role saved = roleRepository.save(r);
		return toDetail(requireWithPermissions(saved.getId()));
	}

	@Transactional
	public RoleDetailResponse update(UUID id, UpdateRoleRequest body) {
		Role r = requireWithPermissions(id);
		if (body.name() != null && !body.name().isBlank()) {
			String nn = normalizeRoleName(body.name());
			if (!nn.equals(r.getName()) && roleRepository.findByName(nn).isPresent()) {
				throw new DuplicateRoleNameException("Ce nom de rôle existe déjà");
			}
			if (SEEDED_ROLE_NAMES.contains(r.getName()) && !nn.equals(r.getName())) {
				throw new IllegalArgumentException(
						"Le nom des rôles prédéfinis ne peut pas être modifié.");
			}
			r.setName(nn);
		}
		if (body.description() != null) {
			r.setDescription(trimToNull(body.description()));
		}
		roleRepository.save(r);
		return toDetail(requireWithPermissions(id));
	}

	@Transactional
	public RoleDetailResponse setPermissions(UUID id, SetRolePermissionsRequest body) {
		Role r = requireWithPermissions(id);
		r.setPermissions(resolvePermissions(body.permissionCodes()));
		roleRepository.save(r);
		return toDetail(requireWithPermissions(id));
	}

	@Transactional
	public void delete(UUID id) {
		Role r = roleRepository
				.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Rôle introuvable"));
		if (SEEDED_ROLE_NAMES.contains(r.getName())) {
			throw new IllegalArgumentException("Les rôles prédéfinis ne peuvent pas être supprimés.");
		}
		long n = userRepository.countActiveUsersWithRole(id);
		if (n > 0) {
			throw new IllegalArgumentException(
					"Impossible de supprimer : " + n + " utilisateur(s) actif(s) ont encore ce rôle.");
		}
		roleRepository.delete(r);
	}

	private RoleDetailResponse toDetail(Role r) {
		List<String> codes = r.getPermissions().stream()
				.map(Permission::getCode)
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.toList();
		long count = userRepository.countActiveUsersWithRole(r.getId());
		return new RoleDetailResponse(r.getId(), r.getName(), r.getDescription(), codes, count);
	}

	private Role requireWithPermissions(UUID id) {
		return roleRepository
				.findByIdWithPermissions(id)
				.orElseThrow(() -> new ResourceNotFoundException("Rôle introuvable"));
	}

	private Set<Permission> resolvePermissions(List<String> rawCodes) {
		if (rawCodes == null || rawCodes.isEmpty()) {
			return new HashSet<>();
		}
		Set<String> normalized = rawCodes.stream()
				.map(RoleService::normalizePermissionCode)
				.collect(Collectors.toCollection(HashSet::new));
		List<Permission> found = permissionRepository.findAll().stream()
				.filter(p -> normalized.contains(p.getCode()))
				.toList();
		if (found.size() != normalized.size()) {
			var foundCodes = found.stream().map(Permission::getCode).collect(Collectors.toSet());
			var missing = normalized.stream().filter(c -> !foundCodes.contains(c)).toList();
			throw new IllegalArgumentException("Permissions inconnues : " + missing);
		}
		return new HashSet<>(found);
	}

	private static List<String> codesOrEmpty(List<String> c) {
		return c == null ? List.of() : c;
	}

	private static String normalizeRoleName(String raw) {
		return raw.trim().toUpperCase().replace(' ', '_');
	}

	private static String normalizePermissionCode(String raw) {
		return raw.trim().toUpperCase().replace(' ', '_');
	}

	private static String trimToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}
