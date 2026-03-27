package com.leader.parcautomobile.service;

import com.leader.parcautomobile.dto.auth.AuthResponse;
import com.leader.parcautomobile.dto.auth.ForgotPasswordRequest;
import com.leader.parcautomobile.dto.auth.LoginRequest;
import com.leader.parcautomobile.dto.auth.RefreshRequest;
import com.leader.parcautomobile.dto.auth.ResetPasswordRequest;
import com.leader.parcautomobile.entity.PasswordResetToken;
import com.leader.parcautomobile.entity.RefreshToken;
import com.leader.parcautomobile.entity.User;
import com.leader.parcautomobile.entity.UserStatus;
import com.leader.parcautomobile.repository.PasswordResetTokenRepository;
import com.leader.parcautomobile.repository.RefreshTokenRepository;
import com.leader.parcautomobile.repository.UserRepository;
import com.leader.parcautomobile.security.JwtService;
import com.leader.parcautomobile.security.RefreshTokenGenerator;
import com.leader.parcautomobile.security.TokenHasher;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private static final long PASSWORD_RESET_TOKEN_HOURS = 24;

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final TokenHasher tokenHasher;
	private final RefreshTokenGenerator refreshTokenGenerator;
	private final EmailService emailService;

	@Value("${app.jwt.refresh-token-days}")
	private long refreshTokenDays;

	@Value("${app.public.frontend-base-url:http://localhost:5173}")
	private String frontendBaseUrl;

	@Value("${app.security.max-failed-logins:5}")
	private int maxFailedLogins;

	@Value("${app.security.lockout-minutes:15}")
	private long lockoutMinutes;

	@Transactional
	public AuthResponse login(LoginRequest request) {
		User user = userRepository
				.findByEmailWithRoles(request.email().trim())
				.orElseThrow(() -> new BadCredentialsException("Identifiants invalides"));

		if (user.getDeletedAt() != null) {
			throw new BadCredentialsException("Identifiants invalides");
		}
		if (user.getStatus() == UserStatus.INACTIVE) {
			throw new BadCredentialsException("Compte inactif");
		}
		if (user.getStatus() == UserStatus.LOCKED) {
			throw new LockedException("Compte verrouillé");
		}
		if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
			throw new LockedException("Compte temporairement verrouillé après plusieurs échecs");
		}
		if (user.getLockedUntil() != null) {
			user.setLockedUntil(null);
			user.setFailedLoginAttempts(0);
		}

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			recordFailedAttempt(user);
			throw new BadCredentialsException("Identifiants invalides");
		}

		user.setFailedLoginAttempts(0);
		user.setLockedUntil(null);
		user.setLastLogin(Instant.now());
		userRepository.save(user);

		String access = jwtService.createAccessToken(user);
		String rawRefresh = refreshTokenGenerator.newToken();
		String hash = tokenHasher.sha256Hex(rawRefresh);
		Instant refreshExpiry = Instant.now().plus(refreshTokenDays, ChronoUnit.DAYS);
		refreshTokenRepository.save(RefreshToken.builder()
				.user(user)
				.tokenHash(hash)
				.expiresAt(refreshExpiry)
				.revoked(false)
				.build());

		long expiresInSeconds = jwtService.getAccessTokenMinutes() * 60;
		return new AuthResponse(access, rawRefresh, "Bearer", expiresInSeconds, user.isMustChangePassword());
	}

	private void recordFailedAttempt(User user) {
		int next = user.getFailedLoginAttempts() + 1;
		user.setFailedLoginAttempts(next);
		if (next >= maxFailedLogins) {
			user.setLockedUntil(Instant.now().plus(lockoutMinutes, ChronoUnit.MINUTES));
		}
		userRepository.save(user);
	}

	@Transactional(readOnly = true)
	public AuthResponse refresh(RefreshRequest request) {
		String hash = tokenHasher.sha256Hex(request.refreshToken());
		RefreshToken stored = refreshTokenRepository
				.findByTokenHashAndRevokedIsFalse(hash)
				.orElseThrow(() -> new BadCredentialsException("Session invalide"));

		if (stored.getExpiresAt().isBefore(Instant.now())) {
			throw new BadCredentialsException("Session expirée");
		}

		User user = userRepository
				.findByIdWithRoles(stored.getUser().getId())
				.orElseThrow(() -> new BadCredentialsException("Utilisateur introuvable"));

		if (user.getDeletedAt() != null || user.getStatus() != UserStatus.ACTIVE) {
			throw new BadCredentialsException("Compte non utilisable");
		}

		String access = jwtService.createAccessToken(user);
		long expiresInSeconds = jwtService.getAccessTokenMinutes() * 60;
		return new AuthResponse(
				access, request.refreshToken(), "Bearer", expiresInSeconds,user.isMustChangePassword());
	}

	@Transactional
	public void logout(RefreshRequest request) {
		if (request.refreshToken() == null || request.refreshToken().isBlank()) {
			return;
		}
		String hash = tokenHasher.sha256Hex(request.refreshToken());
		refreshTokenRepository
				.findByTokenHashAndRevokedIsFalse(hash)
				.ifPresent(rt -> {
					rt.setRevoked(true);
					refreshTokenRepository.save(rt);
				});
	}

	/**
	 * Ne révèle pas si l’email existe : réponse identique côté API. Envoie un mail uniquement pour un
	 * compte actif non supprimé.
	 */
	@Transactional
	public void requestPasswordReset(ForgotPasswordRequest request) {
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		User user = userRepository.findByEmailWithRoles(email).orElse(null);
		if (user == null || user.getDeletedAt() != null || user.getStatus() != UserStatus.ACTIVE) {
			return;
		}
		passwordResetTokenRepository.markUnusedUsedForUser(user.getId(), Instant.now());
		String rawToken = refreshTokenGenerator.newToken();
		String hash = tokenHasher.sha256Hex(rawToken);
		Instant expiresAt = Instant.now().plus(PASSWORD_RESET_TOKEN_HOURS, ChronoUnit.HOURS);
		passwordResetTokenRepository.save(PasswordResetToken.builder()
				.user(user)
				.tokenHash(hash)
				.expiresAt(expiresAt)
				.build());

		String base = frontendBaseUrl.replaceAll("/+$", "");
		String encodedToken = URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
		String resetLink = base + "/reset-password?token=" + encodedToken;
		String html =
				"<p>Bonjour,</p>"
						+ "<p>Vous avez demandé la réinitialisation du mot de passe FleetPro pour <strong>"
						+ escapeHtml(user.getEmail())
						+ "</strong>.</p>"
						+ "<p>Ce lien est valable <strong>24 heures</strong> :</p>"
						+ "<p><a href=\""
						+ resetLink
						+ "\">Choisir un nouveau mot de passe</a></p>"
						+ "<p>Si vous n’êtes pas à l’origine de cette demande, ignorez ce message.</p>"
						+ "<p>— FleetPro</p>";
		emailService.sendHtml(List.of(user.getEmail()), "FleetPro — Réinitialisation du mot de passe", html);
	}

	@Transactional
	public void resetPassword(ResetPasswordRequest request) {
		String raw = request.token() == null ? "" : request.token().trim();
		if (raw.isEmpty()) {
			throw new BadCredentialsException("Lien invalide ou expiré");
		}
		String hash = tokenHasher.sha256Hex(raw);
		PasswordResetToken prt = passwordResetTokenRepository
				.findByTokenHash(hash)
				.orElseThrow(() -> new BadCredentialsException("Lien invalide ou expiré"));
		if (prt.getUsedAt() != null || !prt.getExpiresAt().isAfter(Instant.now())) {
			throw new BadCredentialsException("Lien invalide ou expiré");
		}
		User user = userRepository
				.findById(prt.getUser().getId())
				.orElseThrow(() -> new BadCredentialsException("Lien invalide ou expiré"));
		if (user.getDeletedAt() != null || user.getStatus() != UserStatus.ACTIVE) {
			throw new BadCredentialsException("Lien invalide ou expiré");
		}
		user.setPassword(passwordEncoder.encode(request.newPassword()));
		user.setMustChangePassword(false);
		user.setFailedLoginAttempts(0);
		user.setLockedUntil(null);
		userRepository.save(user);
		prt.setUsedAt(Instant.now());
		passwordResetTokenRepository.save(prt);
		refreshTokenRepository.revokeAllForUser(user.getId());
	}

	private static String escapeHtml(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;");
	}
}
