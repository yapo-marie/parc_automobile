package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.Notification;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

	@Query("""
			select n from Notification n
			where n.user.id = :userId
			  and (:read is null or n.read = :read)
			order by n.createdAt desc
			""")
	Page<Notification> findForUser(@Param("userId") UUID userId, @Param("read") Boolean read, Pageable pageable);

	long countByUserIdAndReadFalse(UUID userId);

	@Modifying
	@Query("update Notification n set n.read = true where n.user.id = :userId and n.read = false")
	int markAllRead(@Param("userId") UUID userId);
}

