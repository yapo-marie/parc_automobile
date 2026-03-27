package com.leader.parcautomobile.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final JwtService jwtService;
	private final DatabaseUserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		String token = header.substring(7).trim();
		if (token.isEmpty()) {
			filterChain.doFilter(request, response);
			return;
		}
		try {
			Claims claims = jwtService.parseAndValidate(token);
			String email = claims.get("email", String.class);
			if (email == null || email.isBlank()) {
				writeUnauthorized(response);
				return;
			}
			UserDetails userDetails = userDetailsService.loadUserByUsername(email);
			var auth = new UsernamePasswordAuthenticationToken(
					userDetails, null, userDetails.getAuthorities());
			auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(auth);
		}
		catch (Exception ex) {
			SecurityContextHolder.clearContext();
			/*
			 * Important : ne pas continuer la chaîne avec un contexte vide. Sinon
			 * AnonymousAuthenticationFilter recrée un utilisateur anonyme → 403 sur /api/**,
			 * et le front ne déclenche pas le refresh (il n'écoute que 401).
			 */
			writeUnauthorized(response);
			return;
		}
		filterChain.doFilter(request, response);
	}

	private static void writeUnauthorized(HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		byte[] body = MAPPER.writeValueAsBytes(
				Map.of("code", "INVALID_OR_EXPIRED_TOKEN", "message", "Jeton invalide ou expiré"));
		response.getOutputStream().write(body);
	}
}
