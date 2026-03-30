package com.leader.parcautomobile.gps.repository;

import com.leader.parcautomobile.gps.entity.AlertType;
import com.leader.parcautomobile.gps.entity.GpsAlert;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GpsAlertRepository extends JpaRepository<GpsAlert, UUID> {

	@EntityGraph(attributePaths = "vehicle")
	@Query(
			"""
			select a from GpsAlert a
			where (:vehicleId is null or a.vehicle.id = :vehicleId)
			  and (:type is null or a.type = :type)
			  and (:acknowledged is null or a.acknowledged = :acknowledged)
			order by a.createdAt desc
			""")
	Page<GpsAlert> search(
			@Param("vehicleId") UUID vehicleId,
			@Param("type") AlertType type,
			@Param("acknowledged") Boolean acknowledged,
			Pageable pageable);

	Optional<GpsAlert> findByIdAndAcknowledgedFalse(UUID id);
}

