package com.leader.parcautomobile.security;

import com.leader.parcautomobile.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	@Value("${app.jwt.secret}")
	private String secret;

	@Value("${app.jwt.access-token-minutes}")
	private long accessTokenMinutes;

	private SecretKey key() {
		byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
		if (raw.length < 32) {
			throw new IllegalStateException("app.jwt.secret doit faire au moins 32 octets (UTF-8)");
		}
		return Keys.hmacShaKeyFor(raw);
	}

	public String createAccessToken(User user) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(user.getId().toString())
				.claim("email", user.getEmail())
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(accessTokenMinutes, ChronoUnit.MINUTES)))
				.signWith(key())
				.compact();
	}

	public Claims parseAndValidate(String token) {
		return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
	}

	public long getAccessTokenMinutes() {
		return accessTokenMinutes;
	}
}
