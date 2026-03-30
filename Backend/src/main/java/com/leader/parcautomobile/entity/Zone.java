package com.leader.parcautomobile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "zones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Zone {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(columnDefinition = "text")
	private String description;

	@Column(length = 7)
	private String color;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ZoneType type;

	@Column(name = "center_lat")
	private Double centerLat;

	@Column(name = "center_lng")
	private Double centerLng;

	@Column(name = "radius_meters")
	private Integer radiusMeters;

	@Column(name = "polygon_coordinates", columnDefinition = "text")
	private String polygonCoordinates;

	@Column(name = "max_speed_kmh")
	private Integer maxSpeedKmh;

	@Column(nullable = false)
	private boolean active;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@ManyToMany
	@JoinTable(
			name = "vehicle_zones",
			joinColumns = @JoinColumn(name = "zone_id"),
			inverseJoinColumns = @JoinColumn(name = "vehicle_id"))
	@Builder.Default
	private Set<Vehicle> vehicles = new HashSet<>();

	@PrePersist
	void prePersist() {
		if (createdAt == null) createdAt = Instant.now();
		if (type == null) type = ZoneType.CIRCLE;
		if (color == null || color.isBlank()) color = "#2E75B6";
		if (radiusMeters == null) radiusMeters = 5000;
		if (maxSpeedKmh == null) maxSpeedKmh = 0;
	}
}

