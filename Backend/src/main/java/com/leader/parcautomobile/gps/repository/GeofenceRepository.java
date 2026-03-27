package com.leader.parcautomobile.gps.repository;

import com.leader.parcautomobile.gps.entity.Geofence;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GeofenceRepository extends JpaRepository<Geofence, UUID> {

	@Query(
			"""
			select distinct g from Geofence g
			join g.vehicles v
			where g.active = true
			  and v.id = :vehicleId
			""")
	List<Geofence> findActiveByVehicleId(@Param("vehicleId") UUID vehicleId);
}

