package com.leader.parcautomobile.entity;

public enum VehicleAvailability {
	AVAILABLE,
	ASSIGNED,
	IN_REPAIR,
	/** Visite technique dépassée — contrôle requis (alerte automatique). */
	CONTROLE_REQUIS,
	OUT_OF_SERVICE
}
