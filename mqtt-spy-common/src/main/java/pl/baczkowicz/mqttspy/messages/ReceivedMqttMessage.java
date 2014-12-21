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
package pl.baczkowicz.mqttspy.messages;

import java.util.Date;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Represents a message received on a topic (wraps the Paho's MqttMessage).
 */
public class ReceivedMqttMessage implements IMqttMessage
{
	/** Topic on which the message was received. */
	private final String topic;
	
	/** The received message. */
	private final MqttMessage message;

	/** When the message was received. */
	private Date date;

	/** A unique message ID - guaranteed to be unique at runtime. */
	private final long id;
	
	/**
	 * Creates a ReceivedMqttMessage from the given parameters.
	 * 
	 * @param id Message ID
	 * @param topic Topic on which it was received
	 * @param message The received message
	 */
	public ReceivedMqttMessage(final long id, final String topic, final MqttMessage message)
	{
		this.id = id;
		this.topic = topic;
		this.message = message;
		this.date = new Date();
	}
	
	/**
	 * Creates a ReceivedMqttMessage from the given parameters.
	 * 
	 * @param id Message ID
	 * @param topic Topic on which it was received
	 * @param message The received message
	 * @param date When the message was received
	 */
	public ReceivedMqttMessage(final long id, final String topic, final MqttMessage message, final Date date)
	{
		this.id = id;
		this.topic = topic;
		this.message = message;
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
	public MqttMessage getMessage()
	{
		return message;
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
		return new String(this.message.getPayload());
	}
	
	@Override
	public void setPayload(final String payload)
	{
		this.message.setPayload(payload.getBytes());
	}
	
	@Override
	public int getQoS()
	{
		return this.message.getQos();
	}
	
	@Override
	public boolean isRetained()
	{
		return this.message.isRetained();
	}
}
