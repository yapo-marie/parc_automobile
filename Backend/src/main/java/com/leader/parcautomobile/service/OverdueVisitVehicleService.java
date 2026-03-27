package com.leader.parcautomobile.service;

import com.leader.parcautomobile.entity.TechnicalVisit;
import com.leader.parcautomobile.entity.Vehicle;
import com.leader.parcautomobile.entity.VehicleAvailability;
import com.leader.parcautomobile.repository.TechnicalVisitRepository;
import com.leader.parcautomobile.repository.VehicleRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OverdueVisitVehicleService {

	private final TechnicalVisitRepository technicalVisitRepository;
	private final VehicleRepository vehicleRepository;

	@Transactional
	public void markVehiclesControleRequisForOverdueVisits(LocalDate today) {
		List<TechnicalVisit> overdue = technicalVisitRepository.findPendingOverdueBefore(today);
		for (TechnicalVisit t : overdue) {
			Vehicle v = t.getVehicle();
			if (v.getDeletedAt() != null) {
				continue;
			}
			if (v.getAvailability() == VehicleAvailability.IN_REPAIR
					|| v.getAvailability() == VehicleAvailability.OUT_OF_SERVICE) {
				continue;
			}
			v.setAvailability(VehicleAvailability.CONTROLE_REQUIS);
			vehicleRepository.save(v);
		}
	}
}
