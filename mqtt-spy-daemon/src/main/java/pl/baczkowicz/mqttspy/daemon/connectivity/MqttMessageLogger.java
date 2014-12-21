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

import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.daemon.configuration.generated.DaemonMqttConnectionDetails;
import pl.baczkowicz.mqttspy.logger.SimpleMessageLogComposer;
import pl.baczkowicz.mqttspy.messages.ReceivedMqttMessageWithSubscriptions;
import pl.baczkowicz.mqttspy.utils.ThreadingUtils;

/**
 * This class is responsible for handling logging messages.
 */
public class MqttMessageLogger implements Runnable
{
	/** Message log logger. */
	private final static Logger logger = LoggerFactory.getLogger(MqttMessageLogger.class);
	
	/** Received messages that are to be logged. */
	private final Queue<ReceivedMqttMessageWithSubscriptions> queue;

	/** Connection details. */
	private final DaemonMqttConnectionDetails connectionSettings;
	
	/** Flag indicating whether the logger is/should be running. */
	private boolean running;

	/**
	 * Creates a MqttMessageLogger.
	 * 
	 * @param queue The message queue to be used
	 * @param connectionSettings The connection details
	 */
	public MqttMessageLogger(final Queue<ReceivedMqttMessageWithSubscriptions> queue, final DaemonMqttConnectionDetails connectionSettings)
	{
		this.queue = queue;
		this.connectionSettings = connectionSettings;
	}
	
	public void run()
	{
		Thread.currentThread().setName("Message Logger");
		ThreadingUtils.logStarting();
		running = true;
		
		while (running)
		{
			try
			{
				if (queue.size() > 0)
				{
					logger.info(SimpleMessageLogComposer.createReceivedMessageLog(queue.remove(), connectionSettings.getMessageLog()));					
				}
				else
				{
					// If no messages present, sleep a bit
					Thread.sleep(10);
				}
			}
			catch (InterruptedException e)
			{				
				// Not expected
			}
		}
		
		ThreadingUtils.logEnding();
	}
		
	/**
	 * Returns the number of message awaiting processing.
	 * 
	 * @return The number of messages waiting to be logged
	 */
	public int getMessagesToProcess()
	{
		return queue.size();
	}
	
	/**
	 * Stops the logger.
	 */
	public void stop()
	{
		running = false;
	}
}
