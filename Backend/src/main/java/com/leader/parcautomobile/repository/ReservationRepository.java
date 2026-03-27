package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.Reservation;
import com.leader.parcautomobile.entity.ReservationStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface ReservationRepository
		extends JpaRepository<Reservation, UUID>, JpaSpecificationExecutor<Reservation> {

	@Query(
			"""
			select count(r) from Reservation r
			where r.vehicle.id = :vehicleId
			and r.status in :blockingStatuses
			and r.startDatetime < :end
			and r.endDatetime > :start
			and (:excludeId is null or r.id <> :excludeId)
			""")
	long countOverlapping(
			@Param("vehicleId") UUID vehicleId,
			@Param("start") Instant start,
			@Param("end") Instant end,
			@Param("blockingStatuses") List<ReservationStatus> blockingStatuses,
			@Param("excludeId") UUID excludeId);

	@Query(
			"""
			select distinct r from Reservation r
			join fetch r.vehicle v
			join fetch r.requestedBy u
			left join fetch r.confirmedBy
			where r.id = :id
			""")
	Optional<Reservation> findByIdFetched(@Param("id") UUID id);

	@Modifying
	@Query(
			"""
			update Reservation r set r.status = :newStatus
			where r.status = :oldStatus and r.endDatetime < :now
			""")
	int updateStatusForEndedBefore(
			@Param("oldStatus") ReservationStatus oldStatus,
			@Param("newStatus") ReservationStatus newStatus,
			@Param("now") Instant now);
}
