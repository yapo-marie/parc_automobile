package com.leader.parcautomobile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicle_zones")
@IdClass(VehicleZone.VehicleZoneId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleZone {

	@Id
	@Column(name = "vehicle_id", nullable = false)
	private UUID vehicleId;

	@Id
	@Column(name = "zone_id", nullable = false)
	private UUID zoneId;

	@Column(name = "assigned_at")
	private Instant assignedAt;

	@Column(name = "assigned_by")
	private UUID assignedBy;

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class VehicleZoneId implements Serializable {
		private UUID vehicleId;
		private UUID zoneId;
	}
}

