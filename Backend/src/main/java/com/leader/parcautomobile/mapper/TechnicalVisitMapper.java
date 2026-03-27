package com.leader.parcautomobile.mapper;

import com.leader.parcautomobile.dto.technicalvisit.TechnicalVisitResponse;
import com.leader.parcautomobile.entity.TechnicalVisit;
import com.leader.parcautomobile.entity.Vehicle;
import java.util.Objects;

public final class TechnicalVisitMapper {

	private TechnicalVisitMapper() {}

	public static TechnicalVisitResponse toResponse(TechnicalVisit t) {
		Vehicle v = t.getVehicle();
		String label =
				Objects.toString(v.getBrand(), "") + " " + Objects.toString(v.getModel(), "");
		label = label.trim();
		return new TechnicalVisitResponse(
				t.getId(),
				v.getId(),
				v.getPlateNumber(),
				label,
				t.getType(),
				t.getScheduledDate(),
				t.getCompletedDate(),
				t.getResult(),
				t.getGarage(),
				t.getCost(),
				t.getNextDueDate(),
				t.getComments(),
				t.getCreatedAt());
	}
}

