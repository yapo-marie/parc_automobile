package com.leader.parcautomobile.gps.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.bootstrap.ServerBootstrap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class GpsServerConfig {

	@Value("${gps.server.port:5023}")
	private int port;

	@Autowired
	private GpsServerHandler gpsServerHandler;

	@Bean
	public ServerBootstrap gpsServerBootstrap() {
		var bossGroup = new NioEventLoopGroup(1);
		var workerGroup = new NioEventLoopGroup();

		return new ServerBootstrap()
				.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new GpsServerInitializer(gpsServerHandler));
	}

	@Bean
	public ApplicationRunner startGpsServer(ServerBootstrap bootstrap) {
		return args -> {
			ChannelFuture f = bootstrap.bind(port).sync();
			log.info("Serveur GPS Netty démarré sur le port {}", port);
			// ne bloque pas : le serveur reste actif via les event loops
			f.channel().closeFuture();
		};
	}
}

