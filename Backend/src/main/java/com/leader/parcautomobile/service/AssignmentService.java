package com.leader.parcautomobile.service;

import com.leader.parcautomobile.dto.assignment.AssignmentPageResponse;
import com.leader.parcautomobile.dto.assignment.AssignmentResponse;
import com.leader.parcautomobile.dto.assignment.CreateAssignmentRequest;
import com.leader.parcautomobile.dto.assignment.CreatePoolAssignmentRequest;
import com.leader.parcautomobile.dto.assignment.EndAssignmentRequest;
import com.leader.parcautomobile.dto.assignment.WithdrawAssignmentRequest;
import com.leader.parcautomobile.entity.Assignment;
import com.leader.parcautomobile.entity.AssignmentStatus;
import com.leader.parcautomobile.entity.FleetVehicle;
import com.leader.parcautomobile.entity.ReservationStatus;
import com.leader.parcautomobile.entity.User;
import com.leader.parcautomobile.entity.UserStatus;
import com.leader.parcautomobile.entity.Vehicle;
import com.leader.parcautomobile.entity.VehicleAvailability;
import com.leader.parcautomobile.entity.VehicleRecordStatus;
import com.leader.parcautomobile.exception.ResourceNotFoundException;
import com.leader.parcautomobile.mapper.AssignmentMapper;
import com.leader.parcautomobile.repository.AssignmentRepository;
import com.leader.parcautomobile.repository.AssignmentSpecifications;
import com.leader.parcautomobile.repository.FleetVehicleRepository;
import com.leader.parcautomobile.repository.ReservationRepository;
import com.leader.parcautomobile.repository.UserRepository;
import com.leader.parcautomobile.repository.VehicleRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignmentService {

	private static final List<AssignmentStatus> ASSIGNMENT_BLOCKING = List.of(
			AssignmentStatus.ACTIVE);

	private static final List<com.leader.parcautomobile.entity.ReservationStatus> RESERVATION_BLOCKING =
			List.of(ReservationStatus.EN_ATTENTE, ReservationStatus.CONFIRMEE);

	private final AssignmentRepository assignmentRepository;
	private final VehicleRepository vehicleRepository;
	private final UserRepository userRepository;
	private final ReservationRepository reservationRepository;
	private final FleetVehicleRepository fleetVehicleRepository;

	@Transactional(readOnly = true)
	public AssignmentPageResponse listAll(
			AssignmentStatus status,
			UUID vehicleId,
			UUID driverId,
			Pageable pageable) {
		Specification<Assignment> spec = Specification.allOf(
				AssignmentSpecifications.hasStatus(status),
				AssignmentSpecifications.vehicleId(vehicleId),
				AssignmentSpecifications.driverId(driverId));
		Page<Assignment> page = assignmentRepository.findAll(spec, pageable);
		List<AssignmentResponse> content =
				page.getContent().stream().map(AssignmentMapper::toResponse).toList();
		return new AssignmentPageResponse(
				content,
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages());
	}

	@Transactional(readOnly = true)
	public AssignmentPageResponse listMine(String email, Pageable pageable) {
		Specification<Assignment> spec = Specification.allOf(
				AssignmentSpecifications.driverEmail(email));
		Page<Assignment> page = assignmentRepository.findAll(spec, pageable);
		List<AssignmentResponse> content =
				page.getContent().stream().map(AssignmentMapper::toResponse).toList();
		return new AssignmentPageResponse(
				content,
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages());
	}

	@Transactional(readOnly = true)
	public AssignmentResponse getById(UUID id) {
		return AssignmentMapper.toResponse(requireFetched(id));
	}

	@Transactional
	public AssignmentResponse create(String managerEmail, CreateAssignmentRequest body) {
		Instant start = body.startDatetime();
		Instant end = body.endDatetime();
		if (end != null && !end.isAfter(start)) {
			throw new IllegalArgumentException("La fin doit être après le début");
		}

		User manager = userRepository
				.findByEmailWithRoles(managerEmail)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

		Vehicle vehicle = vehicleRepository
				.findById(body.vehicleId())
				.orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable"));
		if (vehicle.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Véhicule introuvable");
		}
		if (vehicle.getStatus() != VehicleRecordStatus.ACTIVE) {
			throw new IllegalArgumentException("Ce véhicule n'est pas actif");
		}
		if (vehicle.getAvailability() == VehicleAvailability.OUT_OF_SERVICE) {
			throw new IllegalArgumentException("Ce véhicule est hors service");
		}
		if (vehicle.getAvailability() == VehicleAvailability.IN_REPAIR) {
			throw new IllegalArgumentException(
					"Ce véhicule est en réparation : attribution impossible tant que la panne n'est pas résolue.");
		}

		User driver = userRepository
				.findByIdWithRoles(body.driverId())
				.orElseThrow(() -> new ResourceNotFoundException("Conducteur introuvable"));
		if (driver.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Conducteur introuvable");
		}
		if (driver.getStatus() == UserStatus.INACTIVE) {
			throw new IllegalArgumentException(
					"Ce compte conducteur est inactif : attribution impossible.");
		}

		if (hasAssignmentOverlap(body.vehicleId(), start, end, null)) {
			throw new IllegalArgumentException("Véhicule déjà attribué sur cette période");
		}
		if (hasReservationOverlap(body.vehicleId(), start, end, null)) {
			throw new IllegalArgumentException("Véhicule déjà réservé sur cette période");
		}

		Assignment a = Assignment.builder()
				.vehicle(vehicle)
				.driver(driver)
				.assignmentType(body.assignmentType().trim())
				.startDate(start)
				.endDate(end)
				.mileageStart(body.mileageStart())
				.mileageEnd(body.mileageEnd())
				.reason(trimToNull(body.reason()))
				.status(AssignmentStatus.ACTIVE)
				.createdBy(manager)
				.build();
		Assignment saved = assignmentRepository.save(a);

		vehicle.setAvailability(VehicleAvailability.ASSIGNED);
		vehicleRepository.save(vehicle);

		return AssignmentMapper.toResponse(saved);
	}

	@Transactional
	public AssignmentResponse createFromPool(String managerEmail, CreatePoolAssignmentRequest body) {
		FleetVehicle fleet = fleetVehicleRepository
				.findByIdFetched(body.fleetVehicleId())
				.orElseThrow(() -> new ResourceNotFoundException("Entrée de flotte introuvable"));
		if (fleet.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Entrée de flotte introuvable");
		}
		CreateAssignmentRequest inner = new CreateAssignmentRequest(
				fleet.getVehicle().getId(),
				body.driverId(),
				body.assignmentType(),
				body.startDatetime(),
				body.endDatetime(),
				body.mileageStart(),
				body.mileageEnd(),
				body.reason());
		return create(managerEmail, inner);
	}

	@Transactional
	public AssignmentResponse end(String managerEmail, UUID id, EndAssignmentRequest body) {
		User manager = userRepository
				.findByEmailWithRoles(managerEmail)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
		Assignment a = requireFetched(id);
		if (a.getStatus() != AssignmentStatus.ACTIVE) {
			throw new IllegalArgumentException("Cette attribution n'est pas active");
		}
		a.setStatus(AssignmentStatus.ENDED);
		a.setEndDate(body.endDatetime());
		a.setWithdrawnAt(null);
		a.setReason(trimToNull(body.reason()));
		Assignment saved = assignmentRepository.save(a);

		Vehicle v = vehicleRepository.findById(saved.getVehicle().getId())
				.orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable"));
		if (v.getStatus() == VehicleRecordStatus.ACTIVE
				&& v.getAvailability() != VehicleAvailability.OUT_OF_SERVICE) {
			v.setAvailability(VehicleAvailability.AVAILABLE);
			vehicleRepository.save(v);
		}

		return AssignmentMapper.toResponse(saved);
	}

	@Transactional
	public AssignmentResponse withdraw(String managerEmail, UUID id, WithdrawAssignmentRequest body) {
		userRepository
				.findByEmailWithRoles(managerEmail)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
		Assignment a = requireFetched(id);
		if (a.getStatus() != AssignmentStatus.ACTIVE) {
			throw new IllegalArgumentException("Cette attribution n'est pas active");
		}

		Instant now = Instant.now();
		a.setStatus(AssignmentStatus.WITHDRAWN);
		a.setWithdrawnAt(now);
		a.setEndDate(now);
		a.setReason(trimToNull(body.reason()));
		Assignment saved = assignmentRepository.save(a);

		Vehicle v = vehicleRepository.findById(saved.getVehicle().getId())
				.orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable"));
		if (v.getStatus() == VehicleRecordStatus.ACTIVE
				&& v.getAvailability() != VehicleAvailability.OUT_OF_SERVICE) {
			v.setAvailability(VehicleAvailability.AVAILABLE);
			vehicleRepository.save(v);
		}

		return AssignmentMapper.toResponse(saved);
	}

	private boolean hasAssignmentOverlap(UUID vehicleId, Instant start, Instant end, UUID excludeId) {
		return assignmentRepository
				.countOverlapping(vehicleId, start, end, ASSIGNMENT_BLOCKING, excludeId) > 0;
	}

	private boolean hasReservationOverlap(UUID vehicleId, Instant start, Instant end, UUID excludeId) {
		return reservationRepository
				.countOverlapping(vehicleId, start, end, RESERVATION_BLOCKING, excludeId) > 0;
	}

	private Assignment requireFetched(UUID id) {
		return assignmentRepository
				.findByIdFetched(id)
				.orElseThrow(() -> new ResourceNotFoundException("Attribution introuvable"));
	}

	private static String trimToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}

