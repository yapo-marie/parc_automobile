package com.leader.parcautomobile.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

	private final @Nullable JavaMailSender mailSender;

	@Value("${app.mail.from:noreply@fleetpro.local}")
	private String fromAddress;

	public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
		this.mailSender = mailSenderProvider.getIfAvailable();
	}

	public void sendHtml(List<String> to, String subject, String htmlBody) {
		if (to == null || to.isEmpty()) {
			log.warn("Envoi mail ignoré : aucun destinataire (sujet: {}).", subject);
			return;
		}
		if (mailSender == null) {
			log.info(
					"[Mail non configuré] Destinataires: {} | Sujet: {} | Définir spring.mail.host (SMTP).",
					to,
					subject);
			return;
		}
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(fromAddress);
			helper.setTo(to.toArray(String[]::new));
			helper.setSubject(subject);
			helper.setText(htmlBody, true);
			mailSender.send(message);
			log.debug("Mail envoyé : {}", subject);
		} catch (MessagingException e) {
			log.error("Échec envoi mail « {} » : {}", subject, e.getMessage());
		}
	}
}
