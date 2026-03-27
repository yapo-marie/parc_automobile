package com.leader.parcautomobile.mapper;

import com.leader.parcautomobile.dto.vehicle.CreateVehicleRequest;
import com.leader.parcautomobile.dto.vehicle.UpdateVehicleRequest;
import com.leader.parcautomobile.dto.vehicle.VehicleResponse;
import com.leader.parcautomobile.entity.Vehicle;
import com.leader.parcautomobile.entity.VehicleAvailability;
import com.leader.parcautomobile.entity.VehicleRecordStatus;

public final class VehicleMapper {

	private VehicleMapper() {}

	public static VehicleResponse toResponse(Vehicle v) {
		return new VehicleResponse(
				v.getId(),
				v.getPlateNumber(),
				v.getBrand(),
				v.getModel(),
				v.getYear(),
				v.getColor(),
				v.getCategory(),
				v.getFuelType(),
				v.getMileage(),
				v.getPower(),
				v.getSeats(),
				v.getAcquisitionDate(),
				v.getAcquisitionValue(),
				v.getInsuranceExpiry(),
				v.getAvailability(),
				v.getStatus(),
				v.getPhotoUrl(),
				v.getNotes(),
				v.getCreatedAt(),
				v.getUpdatedAt());
	}

	public static void applyCreate(Vehicle.VehicleBuilder b, CreateVehicleRequest r, String plate) {
		long mileage = r.mileage() != null ? r.mileage() : 0L;
		VehicleAvailability av = r.availability() != null ? r.availability() : VehicleAvailability.AVAILABLE;
		VehicleRecordStatus st = r.status() != null ? r.status() : VehicleRecordStatus.ACTIVE;
		b.plateNumber(plate)
				.brand(r.brand().trim())
				.model(r.model().trim())
				.year(r.year())
				.color(trimToNull(r.color()))
				.category(r.category())
				.fuelType(r.fuelType())
				.mileage(mileage)
				.power(r.power())
				.seats(r.seats())
				.acquisitionDate(r.acquisitionDate())
				.acquisitionValue(r.acquisitionValue())
				.insuranceExpiry(r.insuranceExpiry())
				.availability(av)
				.status(st)
				.photoUrl(trimToNull(r.photoUrl()))
				.notes(trimToNull(r.notes()));
	}

	public static void applyUpdate(Vehicle v, UpdateVehicleRequest r, String plate) {
		long mileage = r.mileage() != null ? r.mileage() : 0L;
		VehicleAvailability av = r.availability() != null ? r.availability() : VehicleAvailability.AVAILABLE;
		VehicleRecordStatus st = r.status() != null ? r.status() : VehicleRecordStatus.ACTIVE;
		v.setPlateNumber(plate);
		v.setBrand(r.brand().trim());
		v.setModel(r.model().trim());
		v.setYear(r.year());
		v.setColor(trimToNull(r.color()));
		v.setCategory(r.category());
		v.setFuelType(r.fuelType());
		v.setMileage(mileage);
		v.setPower(r.power());
		v.setSeats(r.seats());
		v.setAcquisitionDate(r.acquisitionDate());
		v.setAcquisitionValue(r.acquisitionValue());
		v.setInsuranceExpiry(r.insuranceExpiry());
		v.setAvailability(av);
		v.setStatus(st);
		v.setPhotoUrl(trimToNull(r.photoUrl()));
		v.setNotes(trimToNull(r.notes()));
	}

	private static String trimToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}
