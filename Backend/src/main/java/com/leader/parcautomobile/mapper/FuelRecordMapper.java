package com.leader.parcautomobile.mapper;

import com.leader.parcautomobile.dto.fuelrecord.FuelRecordResponse;
import com.leader.parcautomobile.entity.FuelRecord;
import java.util.Objects;

public final class FuelRecordMapper {

	private FuelRecordMapper() {}

	public static FuelRecordResponse toResponse(FuelRecord f) {
		String label =
				Objects.toString(f.getVehicle().getBrand(), "")
						+ " "
						+ Objects.toString(f.getVehicle().getModel(), "");
		label = label.trim();
		return new FuelRecordResponse(
				f.getId(),
				f.getVehicle().getId(),
				f.getVehicle().getPlateNumber(),
				label,
				f.getFilledBy().getId(),
				f.getFilledBy().getEmail(),
				f.getFillDate(),
				f.getLiters(),
				f.getUnitPrice(),
				f.getTotalCost(),
				f.getMileage(),
				f.getStation(),
				f.getCreatedAt());
	}
}

