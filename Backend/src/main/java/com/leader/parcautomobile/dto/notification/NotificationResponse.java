package com.leader.parcautomobile.dto.notification;

import com.leader.parcautomobile.entity.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
		UUID id,
		String title,
		String message,
		NotificationType type,
		String link,
		boolean read,
		Instant createdAt) {}

