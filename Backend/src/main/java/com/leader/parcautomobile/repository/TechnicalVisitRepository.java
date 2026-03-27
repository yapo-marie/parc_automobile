package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.TechnicalVisit;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TechnicalVisitRepository
		extends JpaRepository<TechnicalVisit, UUID>,
		JpaSpecificationExecutor<TechnicalVisit> {

	@Query(
			"""
			select distinct t from TechnicalVisit t
			join fetch t.vehicle v
			where t.id = :id
			""")
	Optional<TechnicalVisit> findByIdFetched(@Param("id") UUID id);

	@Query(
			"""
			select distinct t from TechnicalVisit t
			join fetch t.vehicle v
			where t.completedDate is null
			and v.deletedAt is null
			and t.scheduledDate = :scheduledDate
			""")
	List<TechnicalVisit> findPendingByScheduledDate(@Param("scheduledDate") LocalDate scheduledDate);

	@Query(
			"""
			select distinct t from TechnicalVisit t
			join fetch t.vehicle v
			where t.completedDate is null
			and v.deletedAt is null
			and t.scheduledDate < :today
			""")
	List<TechnicalVisit> findPendingOverdueBefore(@Param("today") LocalDate today);

	@Query(
			value =
					"""
					select coalesce(sum(t.cost), 0) from technical_visits t
					where t.vehicle_id in (:vehicleIds)
					and t.completed_date is not null
					and extract(year from t.completed_date) = :year
					""",
			nativeQuery = true)
	BigDecimal sumCompletedCostsForVehiclesInYear(
			@Param("vehicleIds") Collection<UUID> vehicleIds, @Param("year") int year);
}

