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
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "vehicle_id", nullable = false)
	private Vehicle vehicle;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "requested_by", nullable = false)
	private User requestedBy;

	@Column(name = "start_datetime", nullable = false)
	private Instant startDatetime;

	@Column(name = "end_datetime", nullable = false)
	private Instant endDatetime;

	@Column(length = 500)
	private String reason;

	@Column(length = 300)
	private String destination;

	@Column(name = "estimated_km")
	private Integer estimatedKm;

	@Column(name = "passenger_count")
	private Integer passengerCount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	@Builder.Default
	private ReservationStatus status = ReservationStatus.EN_ATTENTE;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "confirmed_by")
	private User confirmedBy;

	@Column(name = "confirmed_at")
	private Instant confirmedAt;

	@Column(name = "rejection_reason", columnDefinition = "text")
	private String rejectionReason;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
		if (status == null) {
			status = ReservationStatus.EN_ATTENTE;
		}
	}
}
