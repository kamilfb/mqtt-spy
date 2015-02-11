/***********************************************************************************
 * 
 * Copyright (c) 2014 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */
package pl.baczkowicz.mqttspy.connectivity;

import java.util.Date;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import pl.baczkowicz.mqttspy.configuration.generated.FormatterDetails;
import pl.baczkowicz.mqttspy.messages.ReceivedMqttMessage;
import pl.baczkowicz.mqttspy.ui.utils.FormattingUtils;
import pl.baczkowicz.mqttspy.utils.ConversionUtils;

public class MqttContent extends ReceivedMqttMessage
{
	private MqttSubscription subscription;
	
	private FormatterDetails lastUsedFormatter;
	
	private String formattedPayload;
	
	public MqttContent(final MqttContent message)
	{
		super(message.getId(), message.getTopic(), copyMqttMessage(message.getRawMessage()));
		this.formattedPayload = message.getFormattedPayload();
		this.lastUsedFormatter = message.getLastUsedFormatter();
		this.subscription = message.getSubscription();
	}

	public MqttContent(final long id, final String topic, final MqttMessage message)
	{
		super(id, topic, message);
		this.formattedPayload = ConversionUtils.arrayToString(message.getPayload());
	}
	
	public MqttContent(final long id, final String topic, final MqttMessage message, final Date date)
	{
		super(id, topic, message, date);
		this.formattedPayload = ConversionUtils.arrayToString(message.getPayload());
	}
	
	public MqttContent(final ReceivedMqttMessage message)
	{
		super(message.getId(), message.getTopic(), message.getRawMessage(), message.getDate());
		this.formattedPayload = message.getPayload();
	}	
	
	public FormatterDetails getLastUsedFormatter()
	{
		return lastUsedFormatter;
	}

	public MqttSubscription getSubscription()
	{
		return subscription;
	}

	public void setSubscription(MqttSubscription subscription)
	{
		this.subscription = subscription;
	}

	public void format(final FormatterDetails formatter)
	{
		if (formatter == null)
		{
			formattedPayload = getPayload();
		}		
		else if (!formatter.equals(lastUsedFormatter))
		{
			lastUsedFormatter = formatter;
			// Use the raw payload to make sure any formatting/encoding that is applied is correct
			formattedPayload = FormattingUtils.checkAndFormatText(formatter, getRawMessage().getPayload());
		}
	}
	
	public String getFormattedPayload(final FormatterDetails formatter)
	{
		format(formatter);
		
		return formattedPayload;
	}
	
	public String getFormattedPayload()
	{
		return formattedPayload;
	}
}
