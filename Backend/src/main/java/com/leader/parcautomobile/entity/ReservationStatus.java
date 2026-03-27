package com.leader.parcautomobile.entity;

public enum ReservationStatus {
	EN_ATTENTE,
	CONFIRMEE,
	/** Période de réservation passée (clôture automatique). */
	TERMINEE,
	REFUSEE,
	ANNULEE
}
