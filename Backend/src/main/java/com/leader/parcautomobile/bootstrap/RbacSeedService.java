package com.leader.parcautomobile.bootstrap;

import com.leader.parcautomobile.entity.Permission;
import com.leader.parcautomobile.entity.Role;
import com.leader.parcautomobile.entity.User;
import com.leader.parcautomobile.entity.UserStatus;
import com.leader.parcautomobile.repository.PermissionRepository;
import com.leader.parcautomobile.repository.RoleRepository;
import com.leader.parcautomobile.repository.UserRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RbacSeedService {

	private final PermissionRepository permissionRepository;
	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.bootstrap.super-admin-email}")
	private String superAdminEmail;

	@Value("${app.bootstrap.super-admin-password}")
	private String superAdminPassword;

	@Transactional
	public void seedIfNeeded() {
		if (permissionRepository.count() == 0) {
			log.info("Initialisation RBAC (premier démarrage)");
			Map<String, Permission> byCode = new HashMap<>();
			for (String code : allPermissionCodes()) {
				Permission p = Permission.builder().code(code).description("").build();
				byCode.put(code, permissionRepository.save(p));
			}

			roleRepository.save(Role.builder()
					.name("SUPER_ADMIN")
					.description("Administrateur système")
					.createdAt(Instant.now())
					.permissions(new HashSet<>(byCode.values()))
					.build());

			roleRepository.save(Role.builder()
					.name("ADMIN")
					.description("Administrateur métier")
					.createdAt(Instant.now())
					.permissions(new HashSet<>(byCode.values()))
					.build());

			Set<Permission> fleetMgrPerms = new HashSet<>();
			addCodes(
					fleetMgrPerms,
					byCode,
					"USER_READ",
					"VEHICLE_CREATE",
					"VEHICLE_READ",
					"VEHICLE_UPDATE",
					"VEHICLE_DELETE",
					"ASSIGNMENT_MANAGE",
					"RESERVATION_MANAGE",
					"FLEET_MANAGE",
					"REPORT_VIEW");
			roleRepository.save(Role.builder()
					.name("FLEET_MANAGER")
					.description("Gestionnaire de flotte")
					.createdAt(Instant.now())
					.permissions(fleetMgrPerms)
					.build());

			Set<Permission> driverPerms = new HashSet<>();
			addCodes(driverPerms, byCode, "VEHICLE_READ");
			roleRepository.save(Role.builder()
					.name("DRIVER")
					.description("Conducteur")
					.createdAt(Instant.now())
					.permissions(driverPerms)
					.build());

			Set<Permission> viewerPerms = new HashSet<>();
			addCodes(viewerPerms, byCode, "VEHICLE_READ", "REPORT_VIEW", "USER_READ");
			roleRepository.save(Role.builder()
					.name("VIEWER")
					.description("Lecture seule")
					.createdAt(Instant.now())
					.permissions(viewerPerms)
					.build());
		}

		if (!userRepository.existsByEmailIgnoreCase(superAdminEmail)) {
			Role superRole = roleRepository
					.findByName("SUPER_ADMIN")
					.orElseThrow(() -> new IllegalStateException("Rôle SUPER_ADMIN manquant — base incomplète"));
			User admin = User.builder()
					.firstname("Super")
					.lastname("Admin")
					.email(superAdminEmail.toLowerCase())
					.password(passwordEncoder.encode(superAdminPassword))
					.status(UserStatus.ACTIVE)
					.mustChangePassword(true)
					.roles(new HashSet<>(Set.of(superRole)))
					.build();
			userRepository.save(admin);
			log.warn(
					"Compte super-admin créé : {} — modifiez impérativement le mot de passe en production.",
					superAdminEmail);
		}
	}

	private static void addCodes(Set<Permission> target, Map<String, Permission> byCode, String... codes) {
		for (String c : codes) {
			target.add(byCode.get(c));
		}
	}

	private static String[] allPermissionCodes() {
		return new String[] {
			"USER_CREATE",
			"USER_READ",
			"USER_UPDATE",
			"USER_DELETE",
			"ROLE_MANAGE",
			"VEHICLE_CREATE",
			"VEHICLE_READ",
			"VEHICLE_UPDATE",
			"VEHICLE_DELETE",
			"ASSIGNMENT_MANAGE",
			"RESERVATION_MANAGE",
			"FLEET_MANAGE",
			"REPORT_VIEW"
		};
	}
}
