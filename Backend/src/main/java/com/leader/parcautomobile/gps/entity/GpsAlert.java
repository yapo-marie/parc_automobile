package com.leader.parcautomobile.gps.entity;

import com.leader.parcautomobile.entity.User;
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
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.FetchProfile;

@Entity
@Table(name = "gps_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsAlert {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "vehicle_id", nullable = false)
	private Vehicle vehicle;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AlertType type;

	@Column(nullable = false, columnDefinition = "text")
	private String message;

	@Column
	private Double latitude;

	@Column
	private Double longitude;

	@Column
	private Double speed;

	@Column(nullable = false)
	private boolean acknowledged;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "acknowledged_by")
	private User acknowledgedBy;

	@Column(name = "acknowledged_at")
	private Instant acknowledgedAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
}

