package com.leader.parcautomobile.gps.service;

import com.leader.parcautomobile.gps.dto.GpsVehicleStatsResponse;
import com.leader.parcautomobile.gps.entity.GpsPosition;
import com.leader.parcautomobile.gps.repository.GpsPositionRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GpsStatsService {

	private final GpsPositionRepository gpsPositionRepository;

	@Transactional(readOnly = true)
	public GpsVehicleStatsResponse computeStats(UUID vehicleId, Instant from, Instant to) {
		List<GpsPosition> pts = gpsPositionRepository.findByVehicleIdAndRecordedAtBetweenOrderByRecordedAtAsc(
				vehicleId,
				from,
				to);
		if (pts.size() < 2) {
			return new GpsVehicleStatsResponse(0D, 0D, 0D);
		}

		double distanceMeters = 0D;
		double maxSpeed = 0D;
		double speedSum = 0D;
		for (int i = 0; i < pts.size(); i++) {
			GpsPosition p = pts.get(i);
			double sp = p.getSpeed();
			maxSpeed = Math.max(maxSpeed, sp);
			speedSum += sp;
			if (i > 0) {
				GpsPosition prev = pts.get(i - 1);
				distanceMeters += haversineMeters(prev.getLatitude(), prev.getLongitude(), p.getLatitude(), p.getLongitude());
			}
		}
		double distanceKm = distanceMeters / 1000D;
		double avgSpeed = speedSum / pts.size();
		return new GpsVehicleStatsResponse(distanceKm, avgSpeed, maxSpeed);
	}

	private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
		final int R = 6371000;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
				* Math.sin(dLon / 2) * Math.sin(dLon / 2);
		return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	}
}

