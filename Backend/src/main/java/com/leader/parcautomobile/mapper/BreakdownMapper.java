package com.leader.parcautomobile.mapper;

import com.leader.parcautomobile.dto.breakdown.BreakdownResponse;
import com.leader.parcautomobile.entity.Breakdown;
import com.leader.parcautomobile.entity.User;
import java.util.Objects;

public final class BreakdownMapper {

	private BreakdownMapper() {}

	public static BreakdownResponse toResponse(Breakdown b) {
		User d = b.getDeclaredBy();
		var v = b.getVehicle();
		String label =
				Objects.toString(v.getBrand(), "") + " " + Objects.toString(v.getModel(), "");
		label = label.trim();
		return new BreakdownResponse(
				b.getId(),
				v.getId(),
				v.getPlateNumber(),
				label,
				d.getId(),
				d.getEmail(),
				b.getDescription(),
				b.getDeclaredAt(),
				b.getMileageAtBreakdown(),
				b.getGarage(),
				b.getRepairCost(),
				b.getResolvedAt(),
				b.getStatus());
	}
}

