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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.storage.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.utils.ThreadingUtils;

/**
 * This class is responsible for handling received messages. One thread per connection expected here.
 * 
 * @author Kamil Baczkowicz
 *
 */
public class MqttMessageHandler implements Runnable
{
	private final static Logger logger = LoggerFactory.getLogger(MqttMessageHandler.class);
	
	private final Queue<FormattedMqttMessage> queue;

	private MqttAsyncConnection connection;

	public MqttMessageHandler(final MqttAsyncConnection connection, final Queue<FormattedMqttMessage> queue)
	{
		this.queue = queue;
		this.connection = connection;
	}
	
	public void run()
	{
		logger.debug("Starting processing thread for connection " + connection.getProperties().getName());
		while (true)
		{
			if (queue.size() > 0)
			{
				final FormattedMqttMessage content = queue.remove();
				connection.messageReceived(content);
				
				// Let other threads do stuff
				ThreadingUtils.sleep(1);
			}
			else
			{
				// If no messages present, sleep a bit
				ThreadingUtils.sleep(10);
			}
		}
	}
		
	public int getMessagesToProcess()
	{
		return queue.size();
	}

}
