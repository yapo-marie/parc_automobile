package com.leader.parcautomobile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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
@Table(name = "fuel_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "vehicle_id", nullable = false)
	private Vehicle vehicle;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "filled_by", nullable = false)
	private User filledBy;

	@Column(name = "fill_date", nullable = false)
	private LocalDate fillDate;

	@Column(name = "liters", nullable = false, precision = 8, scale = 2)
	private BigDecimal liters;

	@Column(name = "unit_price", precision = 10, scale = 2)
	private BigDecimal unitPrice;

	@Column(name = "total_cost", precision = 12, scale = 2)
	private BigDecimal totalCost;

	@Column(name = "mileage")
	private Long mileage;

	@Column(name = "station", length = 200)
	private String station;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}

