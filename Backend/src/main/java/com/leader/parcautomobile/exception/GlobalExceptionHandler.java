package com.leader.parcautomobile.exception;

import com.leader.parcautomobile.dto.error.ApiError;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
		List<String> details = ex.getBindingResult().getFieldErrors().stream()
				.map(err -> err.getField() + ": " + err.getDefaultMessage())
				.collect(Collectors.toList());
		ApiError body = ApiError.of("VALIDATION_ERROR", "Données invalides", details);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
		String msg = ex.getMessage() != null && !ex.getMessage().isBlank()
				? ex.getMessage()
				: "Identifiants invalides";
		ApiError body = ApiError.of("UNAUTHORIZED", msg, List.of());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}

	@ExceptionHandler(LockedException.class)
	public ResponseEntity<ApiError> handleLocked(LockedException ex) {
		ApiError body = ApiError.of("LOCKED", ex.getMessage(), List.of());
		return ResponseEntity.status(HttpStatus.LOCKED).body(body);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
		ApiError body = ApiError.of("NOT_FOUND", ex.getMessage(), List.of());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	@ExceptionHandler(DuplicateEmailException.class)
	public ResponseEntity<ApiError> handleDuplicate(DuplicateEmailException ex) {
		ApiError body = ApiError.of("DUPLICATE_EMAIL", ex.getMessage(), List.of());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
	}

	@ExceptionHandler(DuplicatePlateException.class)
	public ResponseEntity<ApiError> handleDuplicatePlate(DuplicatePlateException ex) {
		ApiError body = ApiError.of("DUPLICATE_PLATE", ex.getMessage(), List.of());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
	}

	@ExceptionHandler(DuplicateRoleNameException.class)
	public ResponseEntity<ApiError> handleDuplicateRoleName(DuplicateRoleNameException ex) {
		ApiError body = ApiError.of("DUPLICATE_ROLE_NAME", ex.getMessage(), List.of());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiError> handleBadArg(IllegalArgumentException ex) {
		ApiError body = ApiError.of("BAD_REQUEST", ex.getMessage(), List.of());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiError> handleDenied(AccessDeniedException ex) {
		ApiError body =
				ApiError.of("FORBIDDEN", "Accès refusé", List.of());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
	}

	/**
	 * Spring Security 7 : les refus de {@code @PreAuthorize} lèvent souvent cette exception,
	 * qui n'hérite pas de {@link AccessDeniedException} — sans handler dédié → HTTP 500.
	 */
	@ExceptionHandler(AuthorizationDeniedException.class)
	public ResponseEntity<ApiError> handleAuthorizationDenied(AuthorizationDeniedException ex) {
		ApiError body = ApiError.of("FORBIDDEN", "Accès refusé", List.of());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleOther(Exception ex) {
		log.error("Erreur non gérée ({})", ex.getClass().getName(), ex);
		ApiError body = ApiError.of("INTERNAL_ERROR", "Erreur interne", List.of());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}
}
