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
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "vehicle_id", nullable = false)
	private Vehicle vehicle;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User driver;

	@Column(name = "assignment_type", nullable = false, length = 30)
	private String assignmentType;

	@Column(name = "start_date", nullable = false)
	private Instant startDate;

	@Column(name = "end_date")
	private Instant endDate;

	@Column(name = "withdrawn_at")
	private Instant withdrawnAt;

	@Column(name = "mileage_start")
	private Long mileageStart;

	@Column(name = "mileage_end")
	private Long mileageEnd;

	@Column(name = "reason", columnDefinition = "text")
	private String reason;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20)
	@Builder.Default
	private AssignmentStatus status = AssignmentStatus.ACTIVE;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
		if (status == null) {
			status = AssignmentStatus.ACTIVE;
		}
	}
}

