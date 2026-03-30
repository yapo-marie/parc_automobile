package com.leader.parcautomobile.gps.websocket;

import com.leader.parcautomobile.gps.dto.GpsAlertDto;
import com.leader.parcautomobile.gps.dto.GpsPositionDto;
import com.leader.parcautomobile.dto.notification.NotificationResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GpsWebSocketBroadcaster {

	private final SimpMessagingTemplate messagingTemplate;

	public void sendPosition(GpsPositionDto dto) {
		UUID vehicleId = dto.vehicleId();
		messagingTemplate.convertAndSend("/topic/gps/" + vehicleId, dto);
		messagingTemplate.convertAndSend("/topic/fleet", dto);
	}

	public void sendAlert(GpsAlertDto dto) {
		messagingTemplate.convertAndSend(
				"/topic/alerts/" + dto.vehicleId(),
				dto);
		// Pour le dashboard global (le prompt frontend s'abonne à /topic/alerts).
		messagingTemplate.convertAndSend("/topic/alerts", dto);
	}

	public void sendNotification(UUID userId, NotificationResponse dto) {
		messagingTemplate.convertAndSend("/topic/notifications/" + userId, dto);
	}
}

