package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.FleetVehicle;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FleetVehicleRepository
		extends JpaRepository<FleetVehicle, UUID>, JpaSpecificationExecutor<FleetVehicle> {

	@Query(
			"""
			select distinct f from FleetVehicle f
			join fetch f.vehicle v
			where f.id = :id
			""")
	Optional<FleetVehicle> findByIdFetched(@Param("id") UUID id);

	boolean existsByVehicle_IdAndDeletedAtIsNull(UUID vehicleId);

	boolean existsByVehicle_IdAndIdNotAndDeletedAtIsNull(UUID vehicleId, UUID excludeFleetId);
}
