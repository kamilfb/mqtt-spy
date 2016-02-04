package pl.baczkowicz.mqttspy.kura;

import java.io.IOException;

import org.eclipse.kura.KuraInvalidMessageException;
import org.eclipse.kura.core.cloud.CloudPayloadProtoBufDecoderImpl;
import org.eclipse.kura.message.KuraPayload;
import org.eclipse.kura.message.KuraPosition;

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
	
	public static String positionToString(final KuraPosition position)
	{
		return "KuraPosition [longitude=" + position.getLongitude()
				+ ", latitude=" + position.getLatitude() + ", altitude="
				+ position.getAltitude() + ", precision=" + position.getPrecision()
				+ ", heading=" + position.getHeading() + ", speed="
				+ position.getSpeed() + ", timestamp=" + position.getTimestamp()
				+ ", satellites=" + position.getSatellites() + ", status="
				+ position.getStatus() + "]";
	}

	public static String payloadToString(final KuraPayload payload)
	{
		final String body = payload.getBody() != null ? (", body=" + new String(payload.getBody())) : "";
		
		final String position = payload.getPosition() != null ? (", position=" + positionToString(payload.getPosition())) : "";
		
		return "KuraPayload [timestamp=" + payload.getTimestamp()
				+ ", metrics=" + payload.metrics() 
				+ position
				+ body
				+ "]";
	}
}
