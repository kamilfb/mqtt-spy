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
package pl.baczkowicz.mqttspy.connectivity.handlers;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Platform;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttContent;
import pl.baczkowicz.mqttspy.events.queuable.connectivity.MqttConnectionLostEvent;

/**
 * MQTT callback handler - one per connection.
 */
public class MqttCallbackHandler implements MqttCallback
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(MqttCallbackHandler.class);
	
	/** Stores all received messages, so that we don't block the receiving thread. */
	private final Queue<MqttContent> messageQueue = new LinkedBlockingQueue<MqttContent>();
	
	private MqttAsyncConnection connection;
	
	private long currentId = 1;

	private MqttMessageHandler messageHandler;

	public MqttCallbackHandler(final MqttAsyncConnection connection)
	{
		this.setConnection(connection);
		this.messageHandler = new MqttMessageHandler(connection, messageQueue);
		new Thread(messageHandler).start();
	}

	public void connectionLost(Throwable cause)
	{
		logger.error("Connection " + connection.getProperties().getName() + " lost", cause);
		Platform.runLater(new MqttEventHandler(new MqttConnectionLostEvent(connection, cause)));
	}

	public void messageArrived(final String topic, final MqttMessage message)
	{
		logger.debug("[{}] Received message on topic \"{}\". Payload = \"{}\"", messageQueue.size(), topic, new String(message.getPayload()));
		messageQueue.add(new MqttContent(currentId, topic, message));
		currentId++;
	}

	public void deliveryComplete(IMqttDeliveryToken token)
	{
		logger.trace("Delivery complete for " + token.getMessageId());
	}

	public MqttAsyncConnection getConnection()
	{
		return connection;
	}

	public void setConnection(MqttAsyncConnection connection)
	{
		this.connection = connection;
	}
}
