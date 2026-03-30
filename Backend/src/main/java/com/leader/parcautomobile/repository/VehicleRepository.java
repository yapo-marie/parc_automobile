package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.Vehicle;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID>, JpaSpecificationExecutor<Vehicle> {

	boolean existsByPlateNumberIgnoreCase(String plateNumber);

	Optional<Vehicle> findByImei(String imei);

	boolean existsByImei(String imei);

	@Query(
			"""
			select v
			from Vehicle v
			where v.deletedAt is null
			  and v.lastSeen is not null
			  and v.lastSeen > :cutoff
			  and v.status = com.leader.parcautomobile.entity.VehicleRecordStatus.ACTIVE
			""")
	List<Vehicle> findOnlineVehicles(@Param("cutoff") java.time.Instant cutoff);
}
