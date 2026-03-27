package com.leader.parcautomobile.dto.breakdown;

import java.time.Instant;

public record ResolveBreakdownRequest(
		Instant resolvedAt) {}

