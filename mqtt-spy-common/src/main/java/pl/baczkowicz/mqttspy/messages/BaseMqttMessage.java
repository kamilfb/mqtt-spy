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
package pl.baczkowicz.mqttspy.messages;

import java.util.Date;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import pl.baczkowicz.spy.utils.ConversionUtils;

/**
 * Represents a message received on a topic (wraps the Paho's MqttMessage).
 */
public class BaseMqttMessage implements IBaseMessage
{
	/** Topic on which the message was received. */
	private final String topic;
	
	/** The received message. */
	private final MqttMessage rawMessage;

	/** When the message was received. */
	private Date date;

	/** A unique message ID - guaranteed to be unique at runtime. */
	private final long id;
	
	/**
	 * Creates a BaseMqttMessage from the given parameters.
	 * 
	 * @param id Message ID
	 * @param topic Topic on which it was received
	 * @param message The received message
	 */
	public BaseMqttMessage(final long id, final String topic, final MqttMessage message)
	{
		this.id = id;
		this.topic = topic;
		this.rawMessage = message;
		this.date = new Date();
	}
	
	/**
	 * Creates a BaseMqttMessage from the given parameters.
	 * 
	 * @param id Message ID
	 * @param topic Topic on which it was received
	 * @param message The received message
	 * @param date When the message was received
	 */
	public BaseMqttMessage(final long id, final String topic, final MqttMessage message, final Date date)
	{
		this.id = id;
		this.topic = topic;
		this.rawMessage = message;
		this.date = date;
	}
	
	/**
	 * Makes a copy of the MqttMessage object.
	 *  
	 * @param message The object to be copied.
	 * 
	 * @return A copy of the given object
	 */
	public static MqttMessage copyMqttMessage(final MqttMessage message)
	{
		final MqttMessage copy = new MqttMessage();
		
		copy.setPayload(message.getPayload());
		copy.setQos(message.getQos());
		copy.setRetained(message.isRetained());
		
		return copy;
	}

	/**
	 * Gets the MqttMessage.
	 * 
	 * @return MqttMessage
	 */
	public MqttMessage getRawMessage()
	{
		return rawMessage;
	}

	/**
	 * Gets the date.
	 * 
	 * @return The received date
	 */
	public Date getDate()
	{
		return date;
	}

	/**
	 * Gets the message ID.
	 * 
	 * @return Message ID
	 */
	public long getId()
	{
		return id;
	}
	
	// Convenience methods for accessing the message object	

	@Override
	public String getTopic()
	{
		return topic;
	}
	
	@Override
	public String getPayload()
	{
		return ConversionUtils.arrayToString(this.rawMessage.getPayload());
	}
	
	@Override
	public void setPayload(final String payload)
	{
		this.rawMessage.setPayload(ConversionUtils.stringToArray(payload));
	}
	
	@Override
	public int getQoS()
	{
		return this.rawMessage.getQos();
	}
	
	@Override
	public boolean isRetained()
	{
		return this.rawMessage.isRetained();
	}
}
