package com.leader.parcautomobile.mapper;

import com.leader.parcautomobile.dto.fleet.FleetVehicleResponse;
import com.leader.parcautomobile.dto.fleet.UpdateFleetVehicleRequest;
import com.leader.parcautomobile.entity.FleetVehicle;
import com.leader.parcautomobile.entity.Vehicle;

public final class FleetVehicleMapper {

	private FleetVehicleMapper() {
	}

	public static FleetVehicleResponse toResponse(FleetVehicle f) {
		Vehicle v = f.getVehicle();
		return new FleetVehicleResponse(
				f.getId(),
				v.getId(),
				v.getPlateNumber(),
				v.getBrand(),
				v.getModel(),
				v.getAvailability(),
				f.getAdministration(),
				f.getDailyCost(),
				f.getCostPerKm(),
				f.getAnnualBudget(),
				f.getStartDate(),
				f.getEndDate(),
				f.getNotes(),
				f.getCreatedAt(),
				f.getUpdatedAt());
	}

	public static void applyUpdate(FleetVehicle f, UpdateFleetVehicleRequest r) {
		f.setAdministration(r.administration().trim());
		f.setDailyCost(r.dailyCost());
		f.setCostPerKm(r.costPerKm());
		f.setAnnualBudget(r.annualBudget());
		f.setStartDate(r.startDate());
		f.setEndDate(r.endDate());
		f.setNotes(trimToNull(r.notes()));
	}

	private static String trimToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}
