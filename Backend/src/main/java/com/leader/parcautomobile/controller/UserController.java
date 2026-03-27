package com.leader.parcautomobile.controller;

import com.leader.parcautomobile.dto.user.ChangePasswordRequest;
import com.leader.parcautomobile.dto.user.CreateUserRequest;
import com.leader.parcautomobile.dto.user.UpdateSelfProfileRequest;
import com.leader.parcautomobile.dto.user.UpdateUserRequest;
import com.leader.parcautomobile.dto.user.UserPageResponse;
import com.leader.parcautomobile.dto.user.UserResponse;
import com.leader.parcautomobile.dto.user.UserStatusPatchRequest;
import com.leader.parcautomobile.entity.UserStatus;
import com.leader.parcautomobile.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping
	@PreAuthorize("hasAuthority('USER_READ')")
	public UserPageResponse list(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) UserStatus status,
			@RequestParam(required = false) String role,
			@PageableDefault(size = 20, sort = "lastname", direction = Sort.Direction.ASC) Pageable pageable) {
		return userService.list(q, status, role, pageable);
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public UserResponse me(Authentication authentication) {
		return userService.getByEmail(authentication.getName());
	}

	@PutMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public UserResponse updateMe(
			Authentication authentication, @Valid @RequestBody UpdateSelfProfileRequest body) {
		return userService.updateSelf(authentication.getName(), body);
	}

	@PutMapping("/me/password")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> changePassword(
			Authentication authentication, @Valid @RequestBody ChangePasswordRequest body) {
		userService.changePassword(authentication.getName(), body);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('USER_READ') or @authUser.isSelf(authentication, #id)")
	public UserResponse getById(@PathVariable UUID id) {
		return userService.getById(id);
	}

	@PostMapping
	@PreAuthorize("hasAuthority('USER_CREATE')")
	public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest body) {
		UserResponse created = userService.create(body);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('USER_UPDATE')")
	public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest body) {
		return userService.update(id, body);
	}

	@PatchMapping("/{id}/status")
	@PreAuthorize("hasAuthority('USER_UPDATE')")
	public UserResponse patchStatus(
			@PathVariable UUID id, @Valid @RequestBody UserStatusPatchRequest body) {
		return userService.patchStatus(id, body);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('USER_DELETE')")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		userService.softDelete(id);
		return ResponseEntity.noContent().build();
	}
}
