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
package pl.baczkowicz.mqttspy.utils;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.MqttConnectionDetails;
import pl.baczkowicz.mqttspy.utils.ConfigurationUtils;
import pl.baczkowicz.mqttspy.utils.ConnectionUtils;
import pl.baczkowicz.mqttspy.utils.MqttUtils;

/**
 * Configuration utilities.
 */
public class ConfigurationUtils
{
	/** Commons schema. */
	public static final String COMMON_SCHEMA = "/mqtt-spy-common.xsd";

	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(ConfigurationUtils.class);
	
	/**
	 * Goes through all server URIs and completes them with the TCP prefix if necessary.
	 * 
	 * @param connection Connection details
	 */
	public static void completeServerURIs(final MqttConnectionDetails connection)
	{
		for (int i = 0; i < connection.getServerURI().size(); i++)
		{
			final String serverURI = connection.getServerURI().get(i);			
			final String completeServerURI = MqttUtils.getCompleteServerURI(serverURI);
			
			// Replace the existing value if it is not complete
			if (!completeServerURI.equals(serverURI))
			{
				logger.info("Auto-complete for server URI ({} -> {})", serverURI, completeServerURI);
				connection.getServerURI().set(i, completeServerURI);
			}
		}	
	}
	
	/**
	 * Populates the connection details with missing parameters, e.g. name, clean session, etc.
	 * 
	 * @param connection The connection details to complete
	 */
	public static void populateConnectionDefaults(final MqttConnectionDetails connection)
	{	
		if (connection.getName() == null || connection.getName().isEmpty())
		{
			connection.setName(ConnectionUtils.composeConnectionName(connection.getClientID(), connection.getServerURI()));
		}
		
		if (connection.isCleanSession() == null)
		{
			connection.setCleanSession(MqttConnectOptions.CLEAN_SESSION_DEFAULT);
		}
		
		if (connection.getConnectionTimeout() == null)
		{
			connection.setConnectionTimeout(MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT);
		}		

		if (connection.getKeepAliveInterval() == null)
		{
			connection.setKeepAliveInterval(MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT);
		}		
	}
}
