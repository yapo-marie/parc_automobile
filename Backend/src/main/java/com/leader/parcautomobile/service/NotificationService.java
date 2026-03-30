package com.leader.parcautomobile.service;

import com.leader.parcautomobile.dto.notification.NotificationResponse;
import com.leader.parcautomobile.entity.Notification;
import com.leader.parcautomobile.entity.NotificationType;
import com.leader.parcautomobile.entity.User;
import com.leader.parcautomobile.exception.ResourceNotFoundException;
import com.leader.parcautomobile.gps.websocket.GpsWebSocketBroadcaster;
import com.leader.parcautomobile.repository.NotificationRepository;
import com.leader.parcautomobile.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;
	private final GpsWebSocketBroadcaster broadcaster;

	@Transactional(readOnly = true)
	public Page<NotificationResponse> list(String email, Boolean read, Pageable pageable) {
		User user = findByEmail(email);
		return notificationRepository.findForUser(user.getId(), read, pageable).map(this::toDto);
	}

	@Transactional(readOnly = true)
	public long countUnread(String email) {
		User user = findByEmail(email);
		return notificationRepository.countByUserIdAndReadFalse(user.getId());
	}

	@Transactional
	public NotificationResponse markRead(String email, UUID notificationId) {
		User user = findByEmail(email);
		Notification n = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new ResourceNotFoundException("Notification introuvable"));
		if (!n.getUser().getId().equals(user.getId())) {
			throw new ResourceNotFoundException("Notification introuvable");
		}
		n.setRead(true);
		return toDto(notificationRepository.save(n));
	}

	@Transactional
	public int markAllRead(String email) {
		User user = findByEmail(email);
		return notificationRepository.markAllRead(user.getId());
	}

	@Transactional
	public void delete(String email, UUID id) {
		User user = findByEmail(email);
		Notification n = notificationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Notification introuvable"));
		if (!n.getUser().getId().equals(user.getId())) {
			throw new ResourceNotFoundException("Notification introuvable");
		}
		notificationRepository.delete(n);
	}

	@Transactional
	public NotificationResponse send(UUID userId, String title, String message, NotificationType type, String link) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
		Notification n = Notification.builder()
				.user(user)
				.title(title)
				.message(message)
				.type(type)
				.link(link)
				.read(false)
				.build();
		Notification saved = notificationRepository.save(n);
		NotificationResponse dto = toDto(saved);
		broadcaster.sendNotification(user.getId(), dto);
		return dto;
	}

	private User findByEmail(String email) {
		return userRepository.findByEmailWithRoles(email)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
	}

	private NotificationResponse toDto(Notification n) {
		return new NotificationResponse(
				n.getId(),
				n.getTitle(),
				n.getMessage(),
				n.getType(),
				n.getLink(),
				n.isRead(),
				n.getCreatedAt());
	}
}

