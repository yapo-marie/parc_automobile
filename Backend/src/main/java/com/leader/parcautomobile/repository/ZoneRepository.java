package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.Zone;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ZoneRepository extends JpaRepository<Zone, UUID> {

	List<Zone> findByActiveTrueOrderByNameAsc();

	@Query("""
			select distinct z from Zone z
			join z.vehicles v
			where z.active = true and v.id = :vehicleId
			""")
	List<Zone> findActiveByVehicleId(@Param("vehicleId") UUID vehicleId);

	@Query("""
			select distinct z from Zone z
			join z.vehicles v
			where z.id = :zoneId
			""")
	java.util.Optional<Zone> findByIdWithVehicles(@Param("zoneId") UUID zoneId);
}

