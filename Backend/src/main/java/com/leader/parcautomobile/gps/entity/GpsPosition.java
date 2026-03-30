package com.leader.parcautomobile.gps.entity;

import com.leader.parcautomobile.entity.Vehicle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "gps_positions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsPosition {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private java.util.UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "vehicle_id", nullable = false)
	private Vehicle vehicle;

	@Column(nullable = false, length = 20)
	private String imei;

	@Column(nullable = false)
	private double latitude;

	@Column(nullable = false)
	private double longitude;

	@Column(nullable = false)
	private double speed;

	@Column(nullable = false)
	private int heading;

	@Column(nullable = false)
	private double altitude;

	@Column(nullable = false)
	private int satellites;

	@Column(nullable = false)
	private double accuracy;

	@Column(nullable = false)
	private boolean ignition;

	@Column(name = "fuel_level")
	private Integer fuelLevel;

	@Column(name = "recorded_at", nullable = false)
	private Instant recordedAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

}

