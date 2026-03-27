package com.leader.parcautomobile.gps.service;

import com.leader.parcautomobile.gps.repository.GpsPositionRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GpsPurgeScheduler {

	private final GpsPositionRepository gpsPositionRepository;

	// Purge quotidienne à 02:00 (UTC/DB timezone) : historique GPS > 90 jours
	@Scheduled(cron = "0 0 2 * * *")
	public void purgeOlderThan90Days() {
		Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);
		int deleted = gpsPositionRepository.deleteOlderThan(cutoff);
		if (deleted > 0) {
			log.info("Purge GPS : {} position(s) supprimée(s) avant {}", deleted, cutoff);
		}
	}
}

