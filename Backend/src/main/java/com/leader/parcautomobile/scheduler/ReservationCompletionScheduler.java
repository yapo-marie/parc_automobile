package com.leader.parcautomobile.scheduler;

import com.leader.parcautomobile.service.ReservationCompletionService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCompletionScheduler {

	private final ReservationCompletionService reservationCompletionService;

	/** Chaque jour à 02:00 : clôture des réservations confirmées dont la fin est passée. */
	@Scheduled(cron = "0 0 2 * * *", zone = "${app.mail.alerts-timezone:Europe/Paris}")
	public void run() {
		try {
			reservationCompletionService.closePastConfirmedReservations(Instant.now());
		} catch (Exception e) {
			log.error("Échec clôture réservations TERMINEE : {}", e.getMessage(), e);
		}
	}
}
