package com.leader.parcautomobile.gps.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Gt06ProtocolDecoder extends ByteToMessageDecoder {

	private static final short START_BITS = 0x7878;
	private static final byte TAIL_1 = 0x0D;
	private static final byte TAIL_2 = 0x0A;

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		while (true) {
			int readable = in.readableBytes();
			if (readable < 6) return;

			int startIdx = indexOfHeader(in);
			if (startIdx < 0) {
				// Pas de header => on jette tout
				in.skipBytes(readable);
				return;
			}
			if (startIdx > in.readerIndex()) {
				in.skipBytes(startIdx - in.readerIndex());
			}

			if (in.readableBytes() < 3) return;
			int lengthByte = in.getUnsignedByte(in.readerIndex() + 2);
			int totalLen = lengthByte + 5; // règle basée sur le parseur JS (gt06.js)

			if (in.readableBytes() < totalLen) return; // en attente d’octets complets

			// Frame = [0..totalLen-1]
			byte[] frame = new byte[totalLen];
			in.readBytes(frame);

			// Validation basique queue
			if (frame[frame.length - 2] != TAIL_1 || frame[frame.length - 1] != TAIL_2) {
				continue;
			}

			int messageTypeCode = frame[3] & 0xFF;

			try {
				Gt06DecodedMessage decoded = decodeFrame(frame, messageTypeCode);
				if (decoded != null) {
					// messageTypeCode est toujours renseigné pour l’ACK
					out.add(decoded);
				}
			}
			catch (Exception e) {
				log.debug("GT06 decode failed: {}", e.getMessage());
			}
		}
	}

	private static int indexOfHeader(ByteBuf in) {
		for (int i = in.readerIndex(); i <= in.writerIndex() - 2; i++) {
			if (in.getByte(i) == 0x78 && in.getByte(i + 1) == 0x78) {
				return i;
			}
		}
		return -1;
	}

	private static Gt06DecodedMessage decodeFrame(byte[] frame, int messageTypeCode) {
		// On décode selon les offsets du parseur open-source (vondraussen/gt06 => gt06.js)
		// Note: la CRC n'est pas vérifiée ici (on vise surtout un décodage robuste + ACK).
		switch (messageTypeCode) {
			case 0x01: // login
				return decodeLogin(frame);
			case 0x12: // position
			case 0x19: // position étendue (on traite pareil que 0x12)
				return decodeLocation(frame, messageTypeCode);
			case 0x13: // status
				return decodeStatus(frame);
			case 0x16: // alarm
				return decodeAlarm(frame);
			default:
				return new Gt06DecodedMessage(null, Instant.now(), null, null, null, null, null, null, null, null, null, messageTypeCode);
		}
	}

	private static String decodeImeiBcd(byte[] frame) {
		// JS parseLogin: data.slice(4,12) => 8 octets
		StringBuilder sb = new StringBuilder();
		for (int i = 4; i < 12; i++) {
			int b = frame[i] & 0xFF;
			int high = (b >> 4) & 0x0F;
			int low = b & 0x0F;
			if (high != 0x0F) sb.append(high);
			if (low != 0x0F) sb.append(low);
		}
		// GT06 IMEI = 15 chiffres : si nécessaire on tronque
		if (sb.length() > 15) return sb.substring(0, 15);
		return sb.toString();
	}

	private static Gt06DecodedMessage decodeLogin(byte[] frame) {
		String imei = decodeImeiBcd(frame);
		return new Gt06DecodedMessage(
				imei,
				Instant.now(),
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				0x01);
	}

	private static Gt06DecodedMessage decodeLocation(byte[] frame, int messageTypeCode) {
		// JS parseLocation offsets :
		// fixTime = data.slice(4,10)  (6 bytes)
		// quantity = data.readUInt8(10)
		// lat = data.readUInt32BE(11)
		// lon = data.readUInt32BE(15)
		// speed = data.readUInt8(19)
		// course = data.readUInt16BE(20)

		Instant ts = parseDatetimeUtc(frame[4], frame[5], frame[6], frame[7], frame[8], frame[9]);
		int quantity = frame[10] & 0xFF;
		double latRaw = readUInt32BE(frame, 11);
		double lonRaw = readUInt32BE(frame, 15);
		double speed = frame[19] & 0xFF;
		int course = readUInt16BE(frame, 20);

		double lat = decodeGt06Lat(latRaw, course);
		double lon = decodeGt06Lon(lonRaw, course);
		int heading = course & 0x3FF;
		int satellites = (quantity & 0xF0) >> 4;

		return new Gt06DecodedMessage(
				null,
				ts,
				lat,
				lon,
				speed,
				heading,
				0D,
				satellites,
				0D,
				null,
				null,
				messageTypeCode);
	}

	private static Gt06DecodedMessage decodeStatus(byte[] frame) {
		// JS parseStatus: statusInfo = data.slice(4,9) => terminalInfo = statusInfo[0] => frame[4]
		int terminalInfo = frame[4] & 0xFF;
		boolean ignitionOn = (terminalInfo & 0x02) != 0;

		int alarm = (terminalInfo & 0x38) >> 3;
		com.leader.parcautomobile.gps.entity.AlertType alarmType = null;
		switch (alarm) {
			case 1 -> alarmType = com.leader.parcautomobile.gps.entity.AlertType.COLLISION;
			case 2 -> alarmType = com.leader.parcautomobile.gps.entity.AlertType.POWER_CUT;
			case 3 -> alarmType = com.leader.parcautomobile.gps.entity.AlertType.LOW_FUEL;
			case 4 -> alarmType = com.leader.parcautomobile.gps.entity.AlertType.SOS;
			default -> alarmType = null;
		}

		return new Gt06DecodedMessage(
				null,
				Instant.now(),
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				ignitionOn,
				alarmType,
				0x13);
	}

	private static Gt06DecodedMessage decodeAlarm(byte[] frame) {
		// JS parseAlarm offsets :
		// fixTime = data.slice(4,10) (6 bytes)
		// quantity = data.readUInt8(10)
		// lat = data.readUInt32BE(11)
		// lon = data.readUInt32BE(15)
		// speed = data.readUInt8(19)
		// course = data.readUInt16BE(20)
		// terminalInfo = data.readUInt8(31)

		Instant ts = parseDatetimeUtc(frame[4], frame[5], frame[6], frame[7], frame[8], frame[9]);
		int quantity = frame[10] & 0xFF;
		double latRaw = readUInt32BE(frame, 11);
		double lonRaw = readUInt32BE(frame, 15);
		double speed = frame[19] & 0xFF;
		int course = readUInt16BE(frame, 20);
		double lat = decodeGt06Lat(latRaw, course);
		double lon = decodeGt06Lon(lonRaw, course);
		int heading = course & 0x3FF;
		int satellites = (quantity & 0xF0) >> 4;

		int terminalInfo = frame[31] & 0xFF;
		boolean ignitionOn = (terminalInfo & 0x02) != 0;
		int alarm = (terminalInfo & 0x38) >> 3;
		com.leader.parcautomobile.gps.entity.AlertType alarmType;
		switch (alarm) {
			case 1 -> alarmType = com.leader.parcautomobile.gps.entity.AlertType.COLLISION;
			case 2 -> alarmType = com.leader.parcautomobile.gps.entity.AlertType.POWER_CUT;
			case 3 -> alarmType = com.leader.parcautomobile.gps.entity.AlertType.LOW_FUEL;
			case 4 -> alarmType = com.leader.parcautomobile.gps.entity.AlertType.SOS;
			default -> alarmType = null;
		}

		return new Gt06DecodedMessage(
				null,
				ts,
				lat,
				lon,
				speed,
				heading,
				0D,
				satellites,
				0D,
				ignitionOn,
				alarmType,
				0x16);
	}

	private static Instant parseDatetimeUtc(byte year0, byte month1, byte day, byte hour, byte minute, byte second) {
		int year = (year0 & 0xFF) + 2000;
		int month = (month1 & 0xFF) - 1;
		int d = (day & 0xFF);
		int h = (hour & 0xFF);
		int m = (minute & 0xFF);
		int s = (second & 0xFF);
		java.time.ZonedDateTime zdt = java.time.ZonedDateTime.of(year, month + 1, d, h, m, s, 0, java.time.ZoneOffset.UTC);
		return zdt.toInstant();
	}

	private static double decodeGt06Lat(double lat, int course) {
		// JS: lat / 60 / 30000 ; if (!(course & 0x0400)) => negative
		double latitude = lat / 60.0 / 30000.0;
		boolean northLatitude = (course & 0x0400) != 0;
		if (!northLatitude) latitude = -latitude;
		return Math.round(latitude * 1_000_000d) / 1_000_000d;
	}

	private static double decodeGt06Lon(double lon, int course) {
		// JS: lon / 60 / 30000 ; if (course & 0x0800) => negative
		double longitude = lon / 60.0 / 30000.0;
		if ((course & 0x0800) != 0) longitude = -longitude;
		return Math.round(longitude * 1_000_000d) / 1_000_000d;
	}

	private static int readUInt16BE(byte[] frame, int offset) {
		return ((frame[offset] & 0xFF) << 8) | (frame[offset + 1] & 0xFF);
	}

	private static long readUInt32BE(byte[] frame, int offset) {
		return ((long) (frame[offset] & 0xFF) << 24)
				| ((long) (frame[offset + 1] & 0xFF) << 16)
				| ((long) (frame[offset + 2] & 0xFF) << 8)
				| ((long) (frame[offset + 3] & 0xFF));
	}

}

