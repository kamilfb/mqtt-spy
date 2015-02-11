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

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.MqttConnectionDetails;
import pl.baczkowicz.mqttspy.connectivity.reconnection.ReconnectionManager;
import pl.baczkowicz.mqttspy.exceptions.ConfigurationException;
import pl.baczkowicz.mqttspy.exceptions.MqttSpyException;
import pl.baczkowicz.mqttspy.utils.ConversionUtils;

/**
 * Simple synchronous MQTT connection.
 */
public class SimpleMqttConnection extends MqttConnectionWithReconnection
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(SimpleMqttConnection.class);
	
	/**
	 * Creates a SimpleMqttConnection.
	 * 
	 * @param id ID of the connection
	 * @param connectionDetails Configured connection details
	 * @param reconnectionManager The reconnection manager to use
	 *  
	 * @throws ConfigurationException Thrown in case of an error
	 */
	public SimpleMqttConnection(final ReconnectionManager reconnectionManager, final int id, final MqttConnectionDetails connectionDetails) 
		throws ConfigurationException
	{
		super(reconnectionManager, new MqttConnectionDetailsWithOptions(id, connectionDetails));	
	}
	
	/**
	 * Attempts a synchronous connection.
	 * 
	 * @return True if successfully connected
	 */
	public boolean connect()
	{
		setConnectionStatus(MqttConnectionStatus.CONNECTING);				
		try
		{
			connectAndWait(connectionDetails.getOptions());
			logger.info("Successfully connected to {}", connectionDetails.getName());
			setConnectionStatus(MqttConnectionStatus.CONNECTED);
			return true;
		}
		catch (MqttSpyException e)
		{
			logger.error("Connection attempt failed", e);
			setConnectionStatus(MqttConnectionStatus.NOT_CONNECTED);
		}
		return false;
	}
	
	/**
	 * Attempts a subscription to the given topic and quality of service.
	 */
	public boolean subscribe(final String topic, final int qos)
	{
		try
		{
			super.subscribeToTopic(topic, qos);
			logger.info("Successfully subscribed to " + topic);
			return true;
		}
		catch (MqttSpyException e)
		{
			logger.error("Subscription attempt failed for topic {}", topic, e);
		}
		
		return false;
	}
	
	/**
	 * Tries to publish a message to the given topic, with the provided payload, quality of service and retained flag.
	 * 
	 * @param publicationTopic Topic to which to publish the message
	 * @param payload Message payload
	 * @param qos Requested quality of service
	 * @param retained Whether the message should be retained
	 */
	public boolean publish(final String publicationTopic, final String payload, final int qos, final boolean retained)
	{
		if (canPublish())
		{
			try
			{
				logger.info("Publishing message on topic \"" + publicationTopic + "\". Payload = \"" + payload + "\"");
				client.publish(publicationTopic, ConversionUtils.stringToArray(payload), qos, retained);
				
				logger.trace("Published message on topic \"" + publicationTopic + "\". Payload = \"" + payload + "\"");
				
				return true;
			}
			catch (MqttException e)
			{
				logger.error("Cannot publish message on " + publicationTopic, e);
			}
		}
		else
		{
			logger.warn("Publication attempt failure - no connection available...");
		}
		
		return false;
	}

	@Override
	public boolean unsubscribeAll(boolean manualOverride)
	{
		throw new UnsupportedOperationException("This operation is now available");
	}
}
