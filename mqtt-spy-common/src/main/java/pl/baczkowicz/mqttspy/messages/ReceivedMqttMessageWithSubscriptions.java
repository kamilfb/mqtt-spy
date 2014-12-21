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
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import pl.baczkowicz.mqttspy.connectivity.BaseMqttConnection;

/**
 * Extension of the ReceivedMqttMessage for storing the matching subscriptions and connection.
 */
public class ReceivedMqttMessageWithSubscriptions extends ReceivedMqttMessage
{
	/** Subscriptions matching the message's topic. */
	private List<String> subscriptions;
	
	/** The connection on which the message was received. */
	private final BaseMqttConnection connection;
	
	/**
	 * Creates a ReceivedMqttMessageWithSubscriptions from the given message.
	 * 
	 * @param message Message to copy from
	 */
	public ReceivedMqttMessageWithSubscriptions(final ReceivedMqttMessageWithSubscriptions message)
	{
		this(
				message.getId(), message.getTopic(), 
				// TODO: not sure why we need a copy here
				copyMqttMessage(message.getMessage()), 
				message.getDate(), message.getConnection());
		
		this.setSubscriptions(message.getSubscriptions());
	}
	
	/**
	 * Creates a ReceivedMqttMessageWithSubscriptions from the given parameters.
	 * 
	 * @param id Message ID
	 * @param topic Topic on which the message was received
	 * @param message The received MqttMessage object
	 * @param connection The connection on which it was received
	 */
	public ReceivedMqttMessageWithSubscriptions(final long id, final String topic, final MqttMessage message, final BaseMqttConnection connection)
	{
		super(id, topic, message);
		this.connection = connection;
	}
	
	/**
	 * Creates a ReceivedMqttMessageWithSubscriptions from the given parameters.
	 * 
	 * @param id Message ID
	 * @param topic Topic on which the message was received
	 * @param message The received MqttMessage object
	 * @param date When the message was received
	 * @param connection The connection on which it was received
	 */
	public ReceivedMqttMessageWithSubscriptions(final long id, final String topic, final MqttMessage message, final Date date, final BaseMqttConnection connection)
	{
		super(id, topic, message, date);
		this.connection = connection;
	}

	/**
	 * Gets the list of matching subscriptions.
	 * 
	 * @return List of matching subscriptions
	 */
	public List<String> getSubscriptions()
	{
		return subscriptions;
	}

	/**
	 * Sets the list of matching subscriptions.
	 * 
	 * @param subscriptions The matching subscriptions to set
	 */
	public void setSubscriptions(final List<String> subscriptions)
	{
		this.subscriptions = subscriptions;
	}

	/**
	 * Gets the connection.
	 * 
	 * @return The connection on which the message was received
	 */
	public BaseMqttConnection getConnection()
	{
		return connection;
	}
}
