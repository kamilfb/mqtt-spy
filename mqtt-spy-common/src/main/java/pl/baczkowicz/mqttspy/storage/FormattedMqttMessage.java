/***********************************************************************************
 * 
 * Copyright (c) 2014 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 * The Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */
package pl.baczkowicz.mqttspy.storage;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import pl.baczkowicz.mqttspy.common.generated.FormatterDetails;
import pl.baczkowicz.mqttspy.connectivity.BaseMqttConnection;
import pl.baczkowicz.mqttspy.messages.BaseMqttMessage;
import pl.baczkowicz.mqttspy.messages.BaseMqttMessageWithSubscriptions;
import pl.baczkowicz.mqttspy.utils.ConversionUtils;

public class FormattedMqttMessage extends BaseMqttMessageWithSubscriptions
{
	/** The first matching subscription. */ 
	private String subscription;
	
	private FormatterDetails lastUsedFormatter;
	
	private String formattedPayload;
	
	public FormattedMqttMessage(final FormattedMqttMessage message)
	{
		super(message.getId(), message.getTopic(), copyMqttMessage(message.getRawMessage()), message.getConnection());
		this.formattedPayload = message.getFormattedPayload();
		this.lastUsedFormatter = message.getLastUsedFormatter();
		this.subscription = message.getSubscription();
	}

	public FormattedMqttMessage(final long id, final String topic, final MqttMessage message, final BaseMqttConnection connection)
	{
		super(id, topic, message, connection);
		this.formattedPayload = ConversionUtils.arrayToString(message.getPayload());
	}
	/*
	public FormattedMqttMessage(final long id, final String topic, final MqttMessage message, final Date date, final BaseMqttConnection connection)
	{
		super(id, topic, message, date, connection);
		this.formattedPayload = ConversionUtils.arrayToString(message.getPayload());
	}
	*/
	public FormattedMqttMessage(final BaseMqttMessage message, final BaseMqttConnection connection)
	{
		super(message.getId(), message.getTopic(), message.getRawMessage(), message.getDate(), connection);
		this.formattedPayload = message.getPayload();
	}	
	
	public FormatterDetails getLastUsedFormatter()
	{
		return lastUsedFormatter;
	}

	public String getSubscription()
	{
		return subscription;
	}

	public void setSubscription(final String subscription)
	{
		this.subscription = subscription;
	}
	
	public String getFormattedPayload()
	{
		return formattedPayload;
	}

	public void setFormattedPayload(final String formattedPayload)
	{
		this.formattedPayload = formattedPayload;
	}

	public void setLastUsedFormatter(final FormatterDetails formatter)
	{
		this.lastUsedFormatter = formatter;		
	}
}
