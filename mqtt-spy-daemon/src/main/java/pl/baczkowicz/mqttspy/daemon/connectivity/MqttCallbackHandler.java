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
package pl.baczkowicz.mqttspy.daemon.connectivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.MessageLogEnum;
import pl.baczkowicz.mqttspy.common.generated.SubscriptionDetails;
import pl.baczkowicz.mqttspy.connectivity.BaseMqttConnection;
import pl.baczkowicz.mqttspy.daemon.configuration.generated.DaemonMqttConnectionDetails;
import pl.baczkowicz.mqttspy.messages.ReceivedMqttMessageWithSubscriptions;
import pl.baczkowicz.mqttspy.scripts.ScriptManager;

/**
 * Callback handler for the MQTT connection.
 */
public class MqttCallbackHandler implements MqttCallback
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(MqttCallbackHandler.class);
	
	/** Stores all received messages, so that we don't block the receiving thread. */
	private final Queue<ReceivedMqttMessageWithSubscriptions> messageQueue = new LinkedBlockingQueue<ReceivedMqttMessageWithSubscriptions>();
	
	/** Logs all received messages (if configured). */
	private final MqttMessageLogger messageLogger;
	
	/** The connection. */
	private final BaseMqttConnection connection;

	/** Connection details. */
	private final DaemonMqttConnectionDetails connectionSettings;
	
	/** Subscription details (as configured). */
	private final Map<String, SubscriptionDetails> subscriptions = new HashMap<String, SubscriptionDetails>();

	/** Script manager - for running subscription scripts. */
	private final ScriptManager scriptManager;
	
	/** Message ID. */
	private long currentId = 1;

	/**
	 * Creates a MqttCallbackHandler.
	 * 
	 * @param connection The connection to be used
	 * @param connectionSettings Connection's details
	 * @param scriptManager Script manager - for running subscription scripts
	 */
	public MqttCallbackHandler(final BaseMqttConnection connection, final DaemonMqttConnectionDetails connectionSettings, final ScriptManager scriptManager)
	{
		this.connection = connection;
		this.connectionSettings = connectionSettings;
		this.scriptManager = scriptManager;
		this.messageLogger = new MqttMessageLogger(messageQueue, connectionSettings);
		
		for (final SubscriptionDetails subscriptionDetails : connectionSettings.getSubscription())
		{
			this.subscriptions.put(subscriptionDetails.getTopic(), subscriptionDetails);
		}
		
		new Thread(messageLogger).start();			
	}

	/** 
	 * Handles connection loss.
	 * 
	 * @param cause Reason of the connection loss
	 */
	public void connectionLost(final Throwable cause)
	{
		logger.error("Connection lost", cause);
		connection.connectionLost(cause);
	}

	/**
	 * Handles received messages.
	 * 
	 * @param topic Topic on which the message has been received
	 * @param message The received message
	 */
	public void messageArrived(final String topic, final MqttMessage message)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("[{}] Received message on topic \"{}\". Payload = \"{}\"", messageQueue.size(), topic, new String(message.getPayload()));
		}
		
		final ReceivedMqttMessageWithSubscriptions receivedMessage = new ReceivedMqttMessageWithSubscriptions(currentId, topic, message, connection);
		
		// Check matching subscriptions
		final List<String> matchingSubscriptions = connection.getTopicMatcher().getMatchingSubscriptions(receivedMessage.getTopic());
		receivedMessage.setSubscriptions(matchingSubscriptions);
		
		// Before scripts
		if (connectionSettings.getMessageLog().isLogBeforeScripts())
		{
			// Log a copy, so that it cannot be modified
			logMessage(new ReceivedMqttMessageWithSubscriptions(receivedMessage));
		}
		
		// If configured, run scripts for the matching subscriptions
		for (final String matchingSubscription : matchingSubscriptions)
		{
			final SubscriptionDetails subscriptionDetails = subscriptions.get(matchingSubscription);
			
			if (subscriptionDetails.getScriptFile() != null)
			{
				scriptManager.runScriptFileWithReceivedMessage(subscriptionDetails.getScriptFile(), receivedMessage);
			}
		}
		
		// After scripts
		if (!connectionSettings.getMessageLog().isLogBeforeScripts())
		{
			logMessage(receivedMessage);
		}
		
		currentId++;
	}
	
	/**
	 * Adds the message to the 'to be logged' queue.
	 *  
	 * @param receivedMessage The received message
	 */
	public void logMessage(final ReceivedMqttMessageWithSubscriptions receivedMessage)
	{
		// Add the received message to queue for logging
		if (!MessageLogEnum.DISABLED.equals(connectionSettings.getMessageLog().getValue()))
		{
			messageQueue.add(receivedMessage);
		}
	}

	/**
	 * Handles completion of message delivery.
	 * 
	 * @param token Delivery token
	 */
	public void deliveryComplete(final IMqttDeliveryToken token)
	{
		if (logger.isTraceEnabled())
		{
			logger.trace("Delivery complete for " + token.getMessageId());
		}
	}
	
	/**
	 * Stops the message logger.
	 */
	public void stop()
	{
		messageLogger.stop();
	}
}
