package com.leader.parcautomobile.gps.server;

import com.leader.parcautomobile.gps.service.GpsPositionService;
import com.leader.parcautomobile.gps.service.MotorCutoffService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import java.nio.ByteBuffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GpsServerHandler extends SimpleChannelInboundHandler<Gt06DecodedMessage> {

	private static final AttributeKey<String> IMEI_KEY = AttributeKey.valueOf("gt06-imei");

	private final GpsPositionService gpsPositionService;
	private final MotorCutoffService motorCutoffService;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Gt06DecodedMessage msg) {
		String imei = msg.imei();
		if (imei == null) {
			imei = ctx.channel().attr(IMEI_KEY).get();
		} else {
			ctx.channel().attr(IMEI_KEY).set(imei);
			motorCutoffService.registerChannel(imei, ctx.channel());
		}

		Gt06DecodedMessage effective = (imei != null && msg.imei() == null)
				? new Gt06DecodedMessage(
						imei,
						msg.timestamp(),
						msg.latitude(),
						msg.longitude(),
						msg.speed(),
						msg.heading(),
						msg.altitude(),
						msg.satellites(),
						msg.accuracy(),
						msg.ignitionOn(),
						msg.fuelLevel(),
						msg.alarmType(),
						msg.messageTypeCode())
				: msg;

		gpsPositionService.processMessage(effective);

		// Toujours acquitter (règle du prompt).
		try {
			byte[] ack = Gt06AckBuilder.buildAck(effective.messageTypeCode());
			ctx.writeAndFlush(io.netty.buffer.Unpooled.wrappedBuffer(ByteBuffer.wrap(ack)));
		} catch (Exception e) {
			log.debug("GT06 ack send failed: {}", e.getMessage());
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		String imei = ctx.channel().attr(IMEI_KEY).get();
		if (imei != null) {
			motorCutoffService.unregisterChannel(imei);
		}
		super.channelInactive(ctx);
	}
}

