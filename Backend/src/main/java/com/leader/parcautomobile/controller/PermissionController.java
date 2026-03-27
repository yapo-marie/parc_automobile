package com.leader.parcautomobile.controller;

import com.leader.parcautomobile.dto.role.PermissionOptionResponse;
import com.leader.parcautomobile.repository.PermissionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

	private final PermissionRepository permissionRepository;

	@GetMapping
	@PreAuthorize("hasAuthority('ROLE_MANAGE')")
	public List<PermissionOptionResponse> list() {
		return permissionRepository.findAll(Sort.by("code")).stream()
				.map(p -> new PermissionOptionResponse(
						p.getCode(), p.getDescription() == null ? "" : p.getDescription()))
				.toList();
	}
}
