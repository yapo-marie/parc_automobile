package com.leader.parcautomobile.gps.repository;

import com.leader.parcautomobile.gps.entity.GpsPosition;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GpsPositionRepository extends JpaRepository<GpsPosition, UUID> {

	Optional<GpsPosition> findTopByVehicleIdOrderByRecordedAtDesc(UUID vehicleId);

	List<GpsPosition> findByVehicleIdAndRecordedAtBetweenOrderByRecordedAtAsc(
			UUID vehicleId, Instant from, Instant to);

	@Query(
			"""
			select p from GpsPosition p
			where p.vehicle.id = :vehicleId
			  and p.recordedAt >= :dayStart
			  and p.recordedAt < :dayEnd
			order by p.recordedAt asc
			""")
	List<GpsPosition> findTripForDay(
			@Param("vehicleId") UUID vehicleId,
			@Param("dayStart") Instant dayStart,
			@Param("dayEnd") Instant dayEnd);

	@Modifying
	@Transactional
	@org.springframework.data.jpa.repository.Query(
			"""
			delete from GpsPosition p
			where p.recordedAt < :cutoff
			""")
	int deleteOlderThan(@org.springframework.data.repository.query.Param("cutoff") Instant cutoff);
}

