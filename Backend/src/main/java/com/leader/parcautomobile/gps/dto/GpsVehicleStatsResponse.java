package com.leader.parcautomobile.gps.dto;

public record GpsVehicleStatsResponse(
		double distanceKm,
		double avgSpeedKmh,
		double maxSpeedKmh) {}

