package com.leader.parcautomobile.service;

import com.leader.parcautomobile.dto.fuelrecord.CreateFuelRecordRequest;
import com.leader.parcautomobile.dto.fuelrecord.FuelMonthlySeriesPoint;
import com.leader.parcautomobile.dto.fuelrecord.FuelRecordPageResponse;
import com.leader.parcautomobile.dto.fuelrecord.FuelRecordResponse;
import com.leader.parcautomobile.dto.fuelrecord.FuelRecordStatsResponse;
import com.leader.parcautomobile.entity.FuelRecord;
import com.leader.parcautomobile.entity.User;
import com.leader.parcautomobile.entity.Vehicle;
import com.leader.parcautomobile.entity.VehicleRecordStatus;
import com.leader.parcautomobile.exception.ResourceNotFoundException;
import com.leader.parcautomobile.mapper.FuelRecordMapper;
import com.leader.parcautomobile.repository.FuelRecordRepository;
import com.leader.parcautomobile.repository.FuelRecordSpecifications;
import com.leader.parcautomobile.repository.UserRepository;
import com.leader.parcautomobile.repository.VehicleRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
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
public class FuelRecordService {

	private final FuelRecordRepository fuelRecordRepository;
	private final VehicleRepository vehicleRepository;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public FuelRecordStatsResponse stats(UUID vehicleId) {
		if (vehicleId == null) {
			throw new IllegalArgumentException("Le paramètre vehicleId est requis");
		}
		var vehicle = vehicleRepository
				.findById(vehicleId)
				.orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable"));
		if (vehicle.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Véhicule introuvable");
		}

		List<FuelRecord> records = fuelRecordRepository.findAllByVehicleIdForStats(vehicleId);

		List<BigDecimal> globalSegments = consumptionSegments(records, null);
		BigDecimal avgL100 = average(globalSegments);

		YearMonth currentYm = YearMonth.now();
		BigDecimal monthLiters = BigDecimal.ZERO;
		BigDecimal monthCost = BigDecimal.ZERO;
		long monthCount = 0;
		for (FuelRecord r : records) {
			if (YearMonth.from(r.getFillDate()).equals(currentYm)) {
				monthLiters = monthLiters.add(r.getLiters());
				if (r.getTotalCost() != null) {
					monthCost = monthCost.add(r.getTotalCost());
				}
				monthCount++;
			}
		}

		List<FuelMonthlySeriesPoint> series = new ArrayList<>();
		for (int i = 5; i >= 0; i--) {
			YearMonth m = YearMonth.now().minusMonths(i);
			List<BigDecimal> monthSegs = consumptionSegments(records, m);
			series.add(
					new FuelMonthlySeriesPoint(m.toString(), average(monthSegs), sumLitersInMonth(records, m)));
		}

		return new FuelRecordStatsResponse(
				avgL100,
				monthLiters.setScale(2, RoundingMode.HALF_UP),
				monthCost.setScale(2, RoundingMode.HALF_UP),
				monthCount,
				records.size(),
				series);
	}

	/** Segments consommation (L/100 km) ; {@code monthFilter} null = tous les pleins. */
	private static List<BigDecimal> consumptionSegments(List<FuelRecord> sorted, YearMonth monthFilter) {
		List<BigDecimal> out = new ArrayList<>();
		for (int i = 1; i < sorted.size(); i++) {
			FuelRecord prev = sorted.get(i - 1);
			FuelRecord curr = sorted.get(i);
			if (monthFilter != null && !YearMonth.from(curr.getFillDate()).equals(monthFilter)) {
				continue;
			}
			if (prev.getMileage() == null || curr.getMileage() == null) {
				continue;
			}
			long deltaKm = curr.getMileage() - prev.getMileage();
			if (deltaKm <= 0) {
				continue;
			}
			BigDecimal l100 = curr.getLiters()
					.divide(BigDecimal.valueOf(deltaKm), 6, RoundingMode.HALF_UP)
					.multiply(BigDecimal.valueOf(100))
					.setScale(2, RoundingMode.HALF_UP);
			out.add(l100);
		}
		return out;
	}

	private static BigDecimal sumLitersInMonth(List<FuelRecord> records, YearMonth m) {
		return records.stream()
				.filter(r -> YearMonth.from(r.getFillDate()).equals(m))
				.map(FuelRecord::getLiters)
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.setScale(2, RoundingMode.HALF_UP);
	}

	private static BigDecimal average(List<BigDecimal> values) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
	}

	@Transactional(readOnly = true)
	public FuelRecordPageResponse list(UUID vehicleId, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
		Specification<FuelRecord> spec = Specification.allOf(
				FuelRecordSpecifications.vehicleId(vehicleId),
				FuelRecordSpecifications.fromDate(fromDate),
				FuelRecordSpecifications.toDate(toDate));
		Page<FuelRecord> page = fuelRecordRepository.findAll(spec, pageable);
		List<FuelRecordResponse> content =
				page.getContent().stream().map(FuelRecordMapper::toResponse).toList();
		return new FuelRecordPageResponse(
				content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
	}

	@Transactional
	public FuelRecordResponse create(String actorEmail, CreateFuelRecordRequest body) {
		User user = userRepository
				.findByEmailWithRoles(actorEmail)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
		Vehicle vehicle = vehicleRepository
				.findById(body.vehicleId())
				.orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable"));
		if (vehicle.getDeletedAt() != null || vehicle.getStatus() != VehicleRecordStatus.ACTIVE) {
			throw new IllegalArgumentException("Ce véhicule n'est pas actif");
		}

		BigDecimal totalCost = body.totalCost();
		if (totalCost == null && body.unitPrice() != null) {
			totalCost = body.unitPrice().multiply(body.liters()).setScale(2, RoundingMode.HALF_UP);
		}

		FuelRecord record = FuelRecord.builder()
				.vehicle(vehicle)
				.filledBy(user)
				.fillDate(body.fillDate())
				.liters(body.liters().setScale(2, RoundingMode.HALF_UP))
				.unitPrice(body.unitPrice() != null ? body.unitPrice().setScale(2, RoundingMode.HALF_UP) : null)
				.totalCost(totalCost != null ? totalCost.setScale(2, RoundingMode.HALF_UP) : null)
				.mileage(body.mileage())
				.station(trimToNull(body.station()))
				.build();

		return FuelRecordMapper.toResponse(fuelRecordRepository.save(record));
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}

