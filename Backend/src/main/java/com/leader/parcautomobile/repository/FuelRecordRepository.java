package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.FuelRecord;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FuelRecordRepository
		extends JpaRepository<FuelRecord, UUID>, JpaSpecificationExecutor<FuelRecord> {

	@Query(
			"""
			select f from FuelRecord f
			where f.vehicle.id = :vehicleId
			order by f.fillDate asc, f.mileage asc nulls last, f.id asc
			""")
	List<FuelRecord> findAllByVehicleIdForStats(@Param("vehicleId") UUID vehicleId);
}

