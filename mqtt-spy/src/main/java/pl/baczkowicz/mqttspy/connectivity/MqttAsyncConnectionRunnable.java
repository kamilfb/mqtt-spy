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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import pl.baczkowicz.mqttspy.connectivity.handlers.MqttConnectionResultHandler;
import pl.baczkowicz.mqttspy.connectivity.handlers.MqttEventHandler;
import pl.baczkowicz.mqttspy.exceptions.MqttSpyException;
import pl.baczkowicz.mqttspy.ui.events.queuable.connectivity.MqttConnectionAttemptFailureEvent;
import pl.baczkowicz.mqttspy.utils.ThreadingUtils;

public class MqttAsyncConnectionRunnable implements Runnable
{
	public final static int CONNECT_DELAY = 500;

	private final static Logger logger = LoggerFactory.getLogger(MqttAsyncConnectionRunnable.class);
	
	private MqttAsyncConnection connection;

	public MqttAsyncConnectionRunnable(final MqttAsyncConnection connection)
	{
		this.connection = connection;
	}
	
	@Override
	public void run()
	{
		connection.setConnectionStatus(MqttConnectionStatus.CONNECTING);
		
		try
		{
			// TODO: move this away
			if (ThreadingUtils.sleep(CONNECT_DELAY))							
			{
				return;
			}

			// Asynch connect
			logger.info("Connecting client ID [{}] to server [{}]; options = {}",
					connection.getProperties().getClientID(), 
					connection.getProperties().getServerURI(), 
					connection.getProperties().getOptions().toString());
			
			connection.connect(connection.getProperties().getOptions(), connection, new MqttConnectionResultHandler());
			
			// TODO: resubscribe when connection regained?
		}
		catch (MqttSpyException e)
		{
			Platform.runLater(new MqttEventHandler(new MqttConnectionAttemptFailureEvent(connection, e)));
			logger.error("Cannot connect to " + connection.getProperties().getServerURI(), e);
		}				
	}
}
