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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "breakdowns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Breakdown {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "vehicle_id", nullable = false)
	private Vehicle vehicle;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "declared_by", nullable = false)
	private User declaredBy;

	@Column(name = "description", nullable = false, columnDefinition = "text")
	private String description;

	@Column(name = "declared_at", nullable = false)
	private Instant declaredAt;

	@Column(name = "mileage_at_breakdown")
	private Long mileageAtBreakdown;

	@Column(name = "garage", length = 200)
	private String garage;

	@Column(name = "repair_cost", precision = 12, scale = 2)
	private BigDecimal repairCost;

	@Column(name = "resolved_at")
	private Instant resolvedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 30)
	@Builder.Default
	private BreakdownStatus status = BreakdownStatus.DECLAREE;

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		if (declaredAt == null) {
			declaredAt = now;
		}
		if (status == null) {
			status = BreakdownStatus.DECLAREE;
		}
	}
}

