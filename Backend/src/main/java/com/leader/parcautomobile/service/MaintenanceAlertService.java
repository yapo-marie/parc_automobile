package com.leader.parcautomobile.service;

import com.leader.parcautomobile.entity.TechnicalVisit;
import com.leader.parcautomobile.repository.TechnicalVisitRepository;
import com.leader.parcautomobile.repository.UserRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceAlertService {

	private final TechnicalVisitRepository technicalVisitRepository;
	private final UserRepository userRepository;
	private final EmailService emailService;
	private final OverdueVisitVehicleService overdueVisitVehicleService;

	@Value("${app.mail.alerts-timezone:Europe/Paris}")
	private String alertsTimezone;

	public void runDailyMaintenanceAlerts() {
		ZoneId zone = ZoneId.of(alertsTimezone);
		LocalDate today = LocalDate.now(zone);
		List<String> recipients = userRepository.findActiveEmailsByRoleName("FLEET_MANAGER");
		if (recipients.isEmpty()) {
			log.warn("Alertes visites : aucun e-mail FLEET_MANAGER actif ; notifications non envoyées.");
		}

		sendScheduledReminders(today.plusDays(30), recipients, "Visite technique — dans 30 jours");
		sendScheduledReminders(today.plusDays(15), recipients, "Visite technique — dans 15 jours");
		sendScheduledReminders(today, recipients, "Visite technique — aujourd’hui");

		overdueVisitVehicleService.markVehiclesControleRequisForOverdueVisits(today);

		List<TechnicalVisit> overdue = technicalVisitRepository.findPendingOverdueBefore(today);
		sendOverdueNotifications(overdue, recipients);
		log.info(
				"Alertes visites ({} / {}) : {} visite(s) en retard notifiée(s), statut véhicules mis à jour si besoin.",
				today,
				alertsTimezone,
				overdue.size());
	}

	private void sendScheduledReminders(LocalDate scheduledOn, List<String> recipients, String subjectPrefix) {
		List<TechnicalVisit> visits = technicalVisitRepository.findPendingByScheduledDate(scheduledOn);
		for (TechnicalVisit t : visits) {
			String plate = t.getVehicle().getPlateNumber();
			String subject = subjectPrefix + " — " + plate;
			String body =
					htmlDoc(
							subjectPrefix,
							visitSummaryHtml(t)
									+ "<p>Échéance prévue le <strong>"
									+ scheduledOn
									+ "</strong>.</p>");
			emailService.sendHtml(recipients, subject, body);
		}
	}

	private void sendOverdueNotifications(List<TechnicalVisit> overdue, List<String> recipients) {
		for (TechnicalVisit t : overdue) {
			String plate = t.getVehicle().getPlateNumber();
			String subject = "Visite technique en retard — " + plate;
			String body =
					htmlDoc(
							"Visite en retard",
							visitSummaryHtml(t)
									+ "<p><strong>La date prévue ("
									+ t.getScheduledDate()
									+ ") est dépassée.</strong></p>"
									+ "<p>Le véhicule a été marqué <strong>CONTROLE_REQUIS</strong> "
									+ "(sauf s’il était en réparation ou hors service).</p>");
			emailService.sendHtml(recipients, subject, body);
		}
	}

	private static String visitSummaryHtml(TechnicalVisit t) {
		var v = t.getVehicle();
		return "<p>"
				+ "Véhicule : <strong>"
				+ esc(v.getBrand())
				+ " "
				+ esc(v.getModel())
				+ "</strong> ("
				+ esc(v.getPlateNumber())
				+ ")<br/>"
				+ "Type de visite : "
				+ esc(t.getType())
				+ "<br/>"
				+ "Résultat actuel : "
				+ esc(t.getResult())
				+ "</p>";
	}

	private static String htmlDoc(String title, String innerHtml) {
		return "<html><body style=\"font-family:system-ui,sans-serif;font-size:15px;color:#0f172a\">"
				+ "<h2 style=\"margin:0 0 12px;font-size:18px\">"
				+ esc(title)
				+ "</h2>"
				+ innerHtml
				+ "<hr style=\"border:none;border-top:1px solid #e2e8f0;margin:20px 0\"/>"
				+ "<p style=\"margin:0;font-size:12px;color:#64748b\">FleetPro — notification automatique</p>"
				+ "</body></html>";
	}

	private static String esc(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;");
	}
}
