package com.leader.parcautomobile.service;

import com.leader.parcautomobile.entity.ReservationStatus;
import com.leader.parcautomobile.repository.ReservationRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationCompletionService {

	private final ReservationRepository reservationRepository;

	@Transactional
	public int closePastConfirmedReservations(Instant now) {
		int n = reservationRepository.updateStatusForEndedBefore(
				ReservationStatus.CONFIRMEE, ReservationStatus.TERMINEE, now);
		if (n > 0) {
			log.info("{} réservation(s) confirmée(s) passée(s) en TERMINEE.", n);
		}
		return n;
	}
}
