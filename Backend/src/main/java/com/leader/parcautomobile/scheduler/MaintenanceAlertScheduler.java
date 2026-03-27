package com.leader.parcautomobile.scheduler;

import com.leader.parcautomobile.service.MaintenanceAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MaintenanceAlertScheduler {

	private final MaintenanceAlertService maintenanceAlertService;

	/** Tous les jours à 08:00 (fuseau {@code app.mail.alerts-timezone}). */
	@Scheduled(cron = "0 0 8 * * *", zone = "${app.mail.alerts-timezone:Europe/Paris}")
	public void runMaintenanceAlerts() {
		try {
			maintenanceAlertService.runDailyMaintenanceAlerts();
		} catch (Exception e) {
			log.error("Échec de la tâche planifiée « alertes visites techniques » : {}", e.getMessage(), e);
		}
	}
}
