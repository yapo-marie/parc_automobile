package com.leader.parcautomobile.mapper;

import com.leader.parcautomobile.dto.assignment.AssignmentResponse;
import com.leader.parcautomobile.entity.Assignment;
import com.leader.parcautomobile.entity.User;
import java.util.Objects;

public final class AssignmentMapper {

	private AssignmentMapper() {}

	public static AssignmentResponse toResponse(Assignment a) {
		User d = a.getDriver();
		String label =
				Objects.toString(a.getVehicle().getBrand(), "") + " " + Objects.toString(a.getVehicle().getModel(), "");
		label = label.trim();
		return new AssignmentResponse(
				a.getId(),
				a.getVehicle().getId(),
				a.getVehicle().getPlateNumber(),
				label,
				d.getId(),
				d.getEmail(),
				d.getFirstname(),
				d.getLastname(),
				a.getStartDate(),
				a.getEndDate(),
				a.getAssignmentType(),
				a.getMileageStart(),
				a.getMileageEnd(),
				a.getReason(),
				a.getStatus(),
				a.getWithdrawnAt());
	}
}

