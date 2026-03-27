package com.leader.parcautomobile.gps.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GpsServerInitializer extends ChannelInitializer<SocketChannel> {

	private final GpsServerHandler gpsServerHandler;

	@Override
	protected void initChannel(SocketChannel ch) {
		var pipeline = ch.pipeline();
		pipeline.addLast(new Gt06ProtocolDecoder());
		pipeline.addLast(gpsServerHandler);
	}
}

