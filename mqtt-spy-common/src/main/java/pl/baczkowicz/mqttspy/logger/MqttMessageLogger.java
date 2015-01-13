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
package pl.baczkowicz.mqttspy.logger;

import java.util.Queue;

import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;

import pl.baczkowicz.mqttspy.common.generated.MessageLog;
import pl.baczkowicz.mqttspy.messages.ReceivedMqttMessageWithSubscriptions;
import pl.baczkowicz.mqttspy.utils.ThreadingUtils;

/**
 * This class is responsible for handling logging messages.
 */
public class MqttMessageLogger implements Runnable
{
	/** Message log logger. */
	private final static Logger logger = Logger.getLogger(MqttMessageLogger.class);
	
	/** Message log logger. */
	private Logger localLogger;
	
	/** Received messages that are to be logged. */
	private final Queue<ReceivedMqttMessageWithSubscriptions> queue;

	/** Message log settings. */
	private final MessageLog messageLogSettings;
	
	/** Flag indicating whether the logger is/should be running. */
	private boolean running;

	/**
	 * Creates a MqttMessageLogger.
	 * 
	 * @param queue The message queue to be used
	 * @param connectionSettings The connection details
	 */
	public MqttMessageLogger(final Queue<ReceivedMqttMessageWithSubscriptions> queue, final MessageLog messageLogSettings, final boolean useAsTemplate)
	{
		this.queue = queue;
		this.messageLogSettings = messageLogSettings;
		
		final String file = messageLogSettings.getLogFile();
		if (file != null)
		{			
			//final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getRootLogger();
			RollingFileAppender appender;
			
			if (useAsTemplate)
			{
				final RollingFileAppender templateAppender = (RollingFileAppender) logger.getAppender("messagelog");
				
				appender = new RollingFileAppender();
				appender.setThreshold(templateAppender.getThreshold());
				appender.setMaximumFileSize(templateAppender.getMaximumFileSize());
				appender.setMaxBackupIndex(templateAppender.getMaxBackupIndex());
				appender.setLayout(templateAppender.getLayout());
				appender.setFile(file);
				appender.activateOptions();
				
				localLogger = Logger.getLogger("pl.baczkowicz.mqttspy.logger.ConnectionSpecificLogger");
				localLogger.addAppender(appender);
				// localLogger.addAppender(logger.getAppender("stdout"));
			}
			else
			{
				appender = (RollingFileAppender) logger.getAppender("messagelog");
				appender.setFile(file);
			}			
		}
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
					if (localLogger != null)
					{
						localLogger.info(SimpleMessageLogComposer.createReceivedMessageLog(queue.remove(), messageLogSettings));
					}
					else
					{
						logger.info(SimpleMessageLogComposer.createReceivedMessageLog(queue.remove(), messageLogSettings));
					}
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
