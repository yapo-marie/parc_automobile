package com.leader.parcautomobile.service;

import com.leader.parcautomobile.dto.reservation.CreateReservationRequest;
import com.leader.parcautomobile.dto.reservation.RejectReservationRequest;
import com.leader.parcautomobile.dto.reservation.ReservationPageResponse;
import com.leader.parcautomobile.dto.reservation.ReservationResponse;
import com.leader.parcautomobile.entity.Reservation;
import com.leader.parcautomobile.entity.ReservationStatus;
import com.leader.parcautomobile.entity.User;
import com.leader.parcautomobile.entity.Vehicle;
import com.leader.parcautomobile.entity.VehicleAvailability;
import com.leader.parcautomobile.entity.VehicleRecordStatus;
import com.leader.parcautomobile.exception.ResourceNotFoundException;
import com.leader.parcautomobile.mapper.ReservationMapper;
import com.leader.parcautomobile.repository.ReservationRepository;
import com.leader.parcautomobile.repository.ReservationSpecifications;
import com.leader.parcautomobile.repository.UserRepository;
import com.leader.parcautomobile.repository.VehicleRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

	private static final List<ReservationStatus> BLOCKING = List.of(
			ReservationStatus.EN_ATTENTE, ReservationStatus.CONFIRMEE);

	private final ReservationRepository reservationRepository;
	private final VehicleRepository vehicleRepository;
	private final UserRepository userRepository;
	private final EmailService emailService;

	@Transactional(readOnly = true)
	public ReservationPageResponse listAll(
			ReservationStatus status, UUID vehicleId, Pageable pageable) {
		Specification<Reservation> spec = Specification.allOf(
				ReservationSpecifications.hasStatus(status),
				ReservationSpecifications.vehicleId(vehicleId));
		Page<Reservation> page = reservationRepository.findAll(spec, pageable);
		List<ReservationResponse> content =
				page.getContent().stream().map(ReservationMapper::toResponse).toList();
		return new ReservationPageResponse(
				content,
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages());
	}

	@Transactional(readOnly = true)
	public ReservationPageResponse listMine(String email, Pageable pageable) {
		Specification<Reservation> spec = Specification.allOf(
				ReservationSpecifications.requestedByEmail(email));
		Page<Reservation> page = reservationRepository.findAll(spec, pageable);
		List<ReservationResponse> content =
				page.getContent().stream().map(ReservationMapper::toResponse).toList();
		return new ReservationPageResponse(
				content,
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages());
	}

	@Transactional(readOnly = true)
	public ReservationResponse getById(UUID id) {
		return ReservationMapper.toResponse(requireFetched(id));
	}

	@Transactional
	public ReservationResponse create(String requesterEmail, CreateReservationRequest body) {
		if (!body.endDatetime().isAfter(body.startDatetime())) {
			throw new IllegalArgumentException("La date de fin doit être après le début");
		}
		User requester = userRepository
				.findByEmailWithRoles(requesterEmail)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
		if (requester.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Utilisateur introuvable");
		}
		Vehicle vehicle = vehicleRepository
				.findById(body.vehicleId())
				.orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable"));
		if (vehicle.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Véhicule introuvable");
		}
		if (vehicle.getStatus() != VehicleRecordStatus.ACTIVE) {
			throw new IllegalArgumentException("Ce véhicule ne peut pas être réservé");
		}
		if (vehicle.getAvailability() == VehicleAvailability.OUT_OF_SERVICE) {
			throw new IllegalArgumentException("Ce véhicule est hors service");
		}
		if (vehicle.getAvailability() == VehicleAvailability.ASSIGNED) {
			throw new IllegalArgumentException(
					"Ce véhicule est déjà attribué : une réservation n'est pas possible pour ce véhicule.");
		}
		if (vehicle.getAvailability() == VehicleAvailability.IN_REPAIR) {
			throw new IllegalArgumentException(
					"Ce véhicule est en réparation : réservation impossible tant qu'il n'est pas réparé.");
		}
		if (hasOverlap(body.vehicleId(), body.startDatetime(), body.endDatetime(), null)) {
			throw new IllegalArgumentException("Ce véhicule est déjà réservé sur cette période");
		}

		Reservation r = Reservation.builder()
				.vehicle(vehicle)
				.requestedBy(requester)
				.startDatetime(body.startDatetime())
				.endDatetime(body.endDatetime())
				.reason(trimToNull(body.reason()))
				.destination(trimToNull(body.destination()))
				.estimatedKm(body.estimatedKm())
				.passengerCount(body.passengerCount())
				.status(ReservationStatus.EN_ATTENTE)
				.build();
		reservationRepository.save(r);
		return ReservationMapper.toResponse(requireFetched(r.getId()));
	}

	@Transactional
	public ReservationResponse confirm(String managerEmail, UUID id) {
		User manager = userRepository
				.findByEmailWithRoles(managerEmail)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
		Reservation r = requireFetched(id);
		if (r.getStatus() != ReservationStatus.EN_ATTENTE) {
			throw new IllegalArgumentException("Seule une demande en attente peut être confirmée");
		}
		if (hasOverlap(r.getVehicle().getId(), r.getStartDatetime(), r.getEndDatetime(), r.getId())) {
			throw new IllegalArgumentException("Ce véhicule est déjà réservé sur cette période");
		}
		r.setStatus(ReservationStatus.CONFIRMEE);
		r.setConfirmedBy(manager);
		r.setConfirmedAt(Instant.now());
		r.setRejectionReason(null);
		reservationRepository.save(r);
		ReservationResponse out = ReservationMapper.toResponse(requireFetched(id));
		sendReservationConfirmedEmail(out);
		return out;
	}

	@Transactional
	public ReservationResponse reject(String managerEmail, UUID id, RejectReservationRequest body) {
		User manager = userRepository
				.findByEmailWithRoles(managerEmail)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
		Reservation r = requireFetched(id);
		if (r.getStatus() != ReservationStatus.EN_ATTENTE) {
			throw new IllegalArgumentException("Seule une demande en attente peut être refusée");
		}
		r.setStatus(ReservationStatus.REFUSEE);
		r.setConfirmedBy(manager);
		r.setConfirmedAt(Instant.now());
		r.setRejectionReason(body.reason().trim());
		reservationRepository.save(r);
		return ReservationMapper.toResponse(requireFetched(id));
	}

	@Transactional
	public void cancel(String email, boolean canManageAll, UUID id, String cancellationReason) {
		Reservation r = requireFetched(id);
		boolean owner = r.getRequestedBy().getEmail().equalsIgnoreCase(email.trim());
		if (!canManageAll && !owner) {
			throw new AccessDeniedException("Vous ne pouvez pas annuler cette réservation");
		}
		if (r.getStatus() != ReservationStatus.EN_ATTENTE && r.getStatus() != ReservationStatus.CONFIRMEE) {
			throw new IllegalArgumentException("Cette réservation ne peut plus être annulée");
		}
		r.setStatus(ReservationStatus.ANNULEE);
		reservationRepository.save(r);
		ReservationResponse dto = ReservationMapper.toResponse(requireFetched(id));
		boolean cancelledByManager = canManageAll && !owner;
		sendReservationCancelledEmail(dto, cancellationReason, cancelledByManager);
	}

	private void sendReservationConfirmedEmail(ReservationResponse r) {
		String subject = "Réservation confirmée — " + r.vehiclePlate();
		String body =
				htmlMail(
						"Réservation confirmée",
						"<p>Bonjour "
								+ esc(r.requesterFirstname())
								+ ",</p>"
								+ "<p>Votre demande pour le véhicule <strong>"
								+ esc(r.vehicleLabel())
								+ "</strong> (<code>"
								+ esc(r.vehiclePlate())
								+ "</code>) est <strong>confirmée</strong>.</p>"
								+ reservationDetailsHtml(r));
		emailService.sendHtml(List.of(r.requesterEmail()), subject, body);
	}

	private void sendReservationCancelledEmail(
			ReservationResponse r, String cancellationReason, boolean cancelledByManager) {
		String subject = "Réservation annulée — " + r.vehiclePlate();
		String intro =
				cancelledByManager
						? "<p>Bonjour "
								+ esc(r.requesterFirstname())
								+ ",</p><p>Votre réservation a été <strong>annulée par le gestionnaire</strong>.</p>"
						: "<p>Bonjour "
								+ esc(r.requesterFirstname())
								+ ",</p><p>Votre <strong>annulation</strong> de réservation a bien été enregistrée.</p>";
		String motif =
				cancellationReason != null && !cancellationReason.isBlank()
						? "<p><strong>Motif :</strong> " + esc(cancellationReason.trim()) + "</p>"
						: "<p><em>Aucun motif n'a été précisé.</em></p>";
		String body = htmlMail("Réservation annulée", intro + motif + reservationDetailsHtml(r));
		emailService.sendHtml(List.of(r.requesterEmail()), subject, body);
	}

	private static String reservationDetailsHtml(ReservationResponse r) {
		ZoneId zone = ZoneId.of("Europe/Paris");
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(zone);
		return "<ul style=\"margin:12px 0;padding-left:20px\">"
				+ "<li>Début : "
				+ fmt.format(r.startDatetime())
				+ "</li>"
				+ "<li>Fin : "
				+ fmt.format(r.endDatetime())
				+ "</li>"
				+ "<li>Statut : "
				+ esc(r.status().name())
				+ "</li>"
				+ "</ul>";
	}

	private static String htmlMail(String title, String inner) {
		return "<html><body style=\"font-family:system-ui,sans-serif;font-size:15px;color:#0f172a\">"
				+ "<h2 style=\"margin:0 0 12px;font-size:18px\">"
				+ esc(title)
				+ "</h2>"
				+ inner
				+ "<hr style=\"border:none;border-top:1px solid #e2e8f0;margin:20px 0\"/>"
				+ "<p style=\"margin:0;font-size:12px;color:#64748b\">FleetPro — notification automatique</p>"
				+ "</body></html>";
	}

	private static String esc(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;");
	}

	private Reservation requireFetched(UUID id) {
		return reservationRepository
				.findByIdFetched(id)
				.orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable"));
	}

	private boolean hasOverlap(UUID vehicleId, Instant start, Instant end, UUID excludeReservationId) {
		return reservationRepository.countOverlapping(vehicleId, start, end, BLOCKING, excludeReservationId)
				> 0;
	}

	private static String trimToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}
