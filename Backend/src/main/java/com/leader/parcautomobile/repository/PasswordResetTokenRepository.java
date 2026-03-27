package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.PasswordResetToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

	Optional<PasswordResetToken> findByTokenHash(String tokenHash);

	@Modifying
	@Query(
			"""
			update PasswordResetToken t
			set t.usedAt = :when
			where t.user.id = :userId and t.usedAt is null
			""")
	int markUnusedUsedForUser(@Param("userId") UUID userId, @Param("when") Instant when);
}
