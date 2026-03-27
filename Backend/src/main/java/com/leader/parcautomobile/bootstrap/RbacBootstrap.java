package com.leader.parcautomobile.bootstrap;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RbacBootstrap {

	private final RbacSeedService rbacSeedService;

	@Bean
	CommandLineRunner seedRbacRunner() {
		return args -> rbacSeedService.seedIfNeeded();
	}
}
