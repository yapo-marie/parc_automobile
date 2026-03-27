package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.Assignment;
import com.leader.parcautomobile.entity.AssignmentStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssignmentRepository
		extends JpaRepository<Assignment, UUID>, JpaSpecificationExecutor<Assignment> {

	@Query(
			"""
			select count(a) from Assignment a
			where a.vehicle.id = :vehicleId
			and a.status in :blockingStatuses
			and a.startDate < :end
			and (a.endDate is null or a.endDate > :start)
			and (:excludeId is null or a.id <> :excludeId)
			""")
	long countOverlapping(
			@Param("vehicleId") UUID vehicleId,
			@Param("start") Instant start,
			@Param("end") Instant end,
			@Param("blockingStatuses") List<AssignmentStatus> blockingStatuses,
			@Param("excludeId") UUID excludeId);

	@Query(
			"""
			select distinct a from Assignment a
			join fetch a.vehicle v
			join fetch a.driver d
			left join fetch a.createdBy c
			where a.id = :id
			""")
	Optional<Assignment> findByIdFetched(@Param("id") UUID id);
}

