package com.leader.parcautomobile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "plate_number", nullable = false, unique = true, length = 20)
	private String plateNumber;

	@Column(nullable = false, length = 100)
	private String brand;

	@Column(nullable = false, length = 100)
	private String model;

	private Integer year;

	@Column(length = 50)
	private String color;

	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	private VehicleCategory category;

	@Enumerated(EnumType.STRING)
	@Column(name = "fuel_type", length = 20)
	private FuelType fuelType;

	@Column(nullable = false)
	@Builder.Default
	private long mileage = 0L;

	private Integer power;

	private Integer seats;

	@Column(name = "acquisition_date")
	private LocalDate acquisitionDate;

	@Column(name = "acquisition_value", precision = 15, scale = 2)
	private BigDecimal acquisitionValue;

	@Column(name = "insurance_expiry")
	private LocalDate insuranceExpiry;

	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	@Builder.Default
	private VehicleAvailability availability = VehicleAvailability.AVAILABLE;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	@Builder.Default
	private VehicleRecordStatus status = VehicleRecordStatus.ACTIVE;

	@Column(name = "photo_url", columnDefinition = "text")
	private String photoUrl;

	@Column(columnDefinition = "text")
	private String notes;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	@Column(name = "imei", unique = true, length = 20)
	private String imei;

	@Column(name = "last_latitude")
	private Double lastLatitude;

	@Column(name = "last_longitude")
	private Double lastLongitude;

	@Column(name = "last_speed")
	private Double lastSpeed = 0D;

	@Column(name = "last_seen")
	private Instant lastSeen;

	@Column(name = "ignition_on", nullable = false)
	private boolean ignitionOn = false;

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		if (createdAt == null) {
			createdAt = now;
		}
		if (updatedAt == null) {
			updatedAt = now;
		}
		if (availability == null) {
			availability = VehicleAvailability.AVAILABLE;
		}
		if (status == null) {
			status = VehicleRecordStatus.ACTIVE;
		}
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = Instant.now();
	}
}
