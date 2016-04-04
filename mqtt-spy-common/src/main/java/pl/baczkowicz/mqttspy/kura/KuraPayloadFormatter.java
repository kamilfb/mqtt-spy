package pl.baczkowicz.mqttspy.kura;

import java.io.IOException;

import org.eclipse.kura.KuraInvalidMessageException;
import org.eclipse.kura.core.cloud.CloudPayloadProtoBufDecoderImpl;
import org.eclipse.kura.message.KuraPayload;
import org.json.JSONObject;

public class KuraPayloadFormatter
{
	public static String format(byte[] data)
	{
		final CloudPayloadProtoBufDecoderImpl decoder = new CloudPayloadProtoBufDecoderImpl(data);
		
		try
		{
			final KuraPayload kuraPayload = decoder.buildFromByteArray();
			return KuraPayloadFormatter.payloadToString(kuraPayload);
		}
		catch (KuraInvalidMessageException | IOException e)
		{
			return e.getLocalizedMessage();
		} 
	}
	
//	public static String positionToString(final KuraPosition position)
//	{
//		return "position: {longitude: " + position.getLongitude()
//				+ ", latitude: " + position.getLatitude() + ", altitude: "
//				+ position.getAltitude() + ", precision: " + position.getPrecision()
//				+ ", heading: " + position.getHeading() + ", speed: "
//				+ position.getSpeed() + ", timestamp=" + position.getTimestamp()
//				+ ", satellites: " + position.getSatellites() + ", status: "
//				+ position.getStatus() + "}";
//	}

	public static String payloadToString(final KuraPayload payload)
	{
		// final String body = payload.getBody() != null ? (", body: " + new String(payload.getBody())) : "";
		final String body = payload.getBody() != null ? (", body: " + new JSONObject(payload.getBody()).toString()) : "";
		
		// final String position = payload.getPosition() != null ? (", position: " + positionToString(payload.getPosition())) : "";
		final String position = payload.getPosition() != null ? (", position: " + new JSONObject(payload.getPosition()).toString()) : "";
		
		return "{kuraPayload: {timestamp: " + payload.getTimestamp()
				+ ", metrics: " + new JSONObject(payload.metrics()).toString() 
				+ position
				+ body
				+ "}}";
	}
}
