package com.leader.parcautomobile.controller;

import com.leader.parcautomobile.dto.notification.NotificationResponse;
import com.leader.parcautomobile.service.NotificationService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public Page<NotificationResponse> list(
			Authentication authentication,
			@RequestParam(required = false) Boolean read,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return notificationService.list(authentication.getName(), read, pageable);
	}

	@GetMapping("/count")
	@PreAuthorize("isAuthenticated()")
	public Map<String, Long> count(Authentication authentication) {
		return Map.of("unread", notificationService.countUnread(authentication.getName()));
	}

	@PatchMapping("/{id}/read")
	@PreAuthorize("isAuthenticated()")
	public NotificationResponse read(Authentication authentication, @PathVariable UUID id) {
		return notificationService.markRead(authentication.getName(), id);
	}

	@PatchMapping("/read-all")
	@PreAuthorize("isAuthenticated()")
	public Map<String, Integer> readAll(Authentication authentication) {
		return Map.of("updated", notificationService.markAllRead(authentication.getName()));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public void delete(Authentication authentication, @PathVariable UUID id) {
		notificationService.delete(authentication.getName(), id);
	}
}

