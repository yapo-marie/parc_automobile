package com.leader.parcautomobile.controller;

import com.leader.parcautomobile.dto.role.CreateRoleRequest;
import com.leader.parcautomobile.dto.role.RoleDetailResponse;
import com.leader.parcautomobile.dto.role.SetRolePermissionsRequest;
import com.leader.parcautomobile.dto.role.UpdateRoleRequest;
import com.leader.parcautomobile.entity.Role;
import com.leader.parcautomobile.repository.RoleRepository;
import com.leader.parcautomobile.service.RoleService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

	private final RoleRepository roleRepository;
	private final RoleService roleService;

	@GetMapping("/names")
	@PreAuthorize("hasAuthority('USER_READ')")
	public List<String> roleNames() {
		return roleRepository.findAll(Sort.by(Sort.Order.asc("name"))).stream()
				.map(Role::getName)
				.toList();
	}

	@GetMapping
	@PreAuthorize("hasAuthority('ROLE_MANAGE')")
	public List<RoleDetailResponse> list() {
		return roleService.listAll();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('ROLE_MANAGE')")
	public RoleDetailResponse get(@PathVariable UUID id) {
		return roleService.getById(id);
	}

	@PostMapping
	@PreAuthorize("hasAuthority('ROLE_MANAGE')")
	public RoleDetailResponse create(@Valid @RequestBody CreateRoleRequest body) {
		return roleService.create(body);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('ROLE_MANAGE')")
	public RoleDetailResponse update(
			@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest body) {
		return roleService.update(id, body);
	}

	@PostMapping("/{id}/permissions")
	@PreAuthorize("hasAuthority('ROLE_MANAGE')")
	public RoleDetailResponse setPermissions(
			@PathVariable UUID id, @Valid @RequestBody SetRolePermissionsRequest body) {
		return roleService.setPermissions(id, body);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('ROLE_MANAGE')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		roleService.delete(id);
	}
}
