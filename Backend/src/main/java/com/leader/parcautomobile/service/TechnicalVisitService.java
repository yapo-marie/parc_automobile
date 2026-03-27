package com.leader.parcautomobile.service;

import com.leader.parcautomobile.dto.technicalvisit.CreateTechnicalVisitRequest;
import com.leader.parcautomobile.dto.technicalvisit.TechnicalVisitPageResponse;
import com.leader.parcautomobile.dto.technicalvisit.TechnicalVisitResponse;
import com.leader.parcautomobile.dto.technicalvisit.UpdateTechnicalVisitRequest;
import com.leader.parcautomobile.entity.TechnicalVisit;
import com.leader.parcautomobile.entity.Vehicle;
import com.leader.parcautomobile.entity.VehicleRecordStatus;
import com.leader.parcautomobile.exception.ResourceNotFoundException;
import com.leader.parcautomobile.mapper.TechnicalVisitMapper;
import com.leader.parcautomobile.repository.TechnicalVisitRepository;
import com.leader.parcautomobile.repository.TechnicalVisitSpecifications;
import com.leader.parcautomobile.repository.VehicleRepository;
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
public class TechnicalVisitService {

	private final TechnicalVisitRepository technicalVisitRepository;
	private final VehicleRepository vehicleRepository;

	@Transactional(readOnly = true)
	public TechnicalVisitPageResponse listAll(
			UUID vehicleId,
			String type,
			String result,
			Pageable pageable) {
		Specification<TechnicalVisit> spec = Specification.allOf(
				TechnicalVisitSpecifications.vehicleId(vehicleId),
				TechnicalVisitSpecifications.type(type),
				TechnicalVisitSpecifications.result(result));
		Page<TechnicalVisit> page = technicalVisitRepository.findAll(spec, pageable);
		List<TechnicalVisitResponse> content =
				page.getContent().stream().map(TechnicalVisitMapper::toResponse).toList();
		return new TechnicalVisitPageResponse(
				content,
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages());
	}

	@Transactional(readOnly = true)
	public TechnicalVisitResponse getById(UUID id) {
		return TechnicalVisitMapper.toResponse(
				technicalVisitRepository.findByIdFetched(id)
						.orElseThrow(() -> new ResourceNotFoundException("Visite technique introuvable")));
	}

	@Transactional
	public void create(CreateTechnicalVisitRequest body) {
		Vehicle vehicle = vehicleRepository
				.findById(body.vehicleId())
				.orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable"));
		if (vehicle.getDeletedAt() != null || vehicle.getStatus() != VehicleRecordStatus.ACTIVE) {
			throw new IllegalArgumentException("Ce véhicule n'est pas actif");
		}

		var b = TechnicalVisit.builder()
				.vehicle(vehicle)
				.type(body.type().trim())
				.scheduledDate(body.scheduledDate())
				.completedDate(body.completedDate())
				.garage(body.garage())
				.cost(body.cost())
				.nextDueDate(body.nextDueDate())
				.comments(body.comments());

		// Si `result` n'est pas fourni côté client, on laisse la valeur par défaut de l'Entity.
		if (body.result() != null) {
			b.result(body.result());
		}

		technicalVisitRepository.save(b.build());
	}

	@Transactional
	public void update(UUID id, UpdateTechnicalVisitRequest body) {
		TechnicalVisit t = technicalVisitRepository
				.findByIdFetched(id)
				.orElseThrow(() -> new ResourceNotFoundException("Visite technique introuvable"));

		t.setType(body.type().trim());
		t.setScheduledDate(body.scheduledDate());
		t.setCompletedDate(body.completedDate());
		// Si `result` n'est pas fourni côté client, on conserve la valeur actuelle.
		if (body.result() != null) {
			t.setResult(body.result());
		}
		t.setGarage(body.garage());
		t.setCost(body.cost());
		t.setNextDueDate(body.nextDueDate());
		t.setComments(body.comments());

		technicalVisitRepository.save(t);
	}
}

