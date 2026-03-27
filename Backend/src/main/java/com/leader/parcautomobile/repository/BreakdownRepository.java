package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.Breakdown;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BreakdownRepository
		extends JpaRepository<Breakdown, UUID>, JpaSpecificationExecutor<Breakdown> {

	@Query(
			"""
			select distinct b from Breakdown b
			join fetch b.vehicle v
			join fetch b.declaredBy d
			where b.id = :id
			""")
	Optional<Breakdown> findByIdFetched(@Param("id") UUID id);
}

