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
import pl.baczkowicz.mqttspy.messages.BaseMqttMessageWithSubscriptions;
import pl.baczkowicz.mqttspy.utils.ThreadingUtils;
import pl.baczkowicz.mqttspy.utils.Utils;

/**
 * This class is responsible for handling logging messages.
 */
public class MqttMessageLogger implements Runnable
{
	/** Message log logger. */
	private final static Logger logger = Logger.getLogger(MqttMessageLogger.class);
	
	/** If X messages logged without a break, log this. */
	private final static int LOG_INTERVAL = 10000;
	
	/** Message log logger. */
	private Logger localLogger;
	
	/** Received messages that are to be logged. */
	private final Queue<BaseMqttMessageWithSubscriptions> queue;

	/** Message log settings. */
	private final MessageLog messageLogSettings;
	
	/** Flag indicating whether the logger is/should be running. */
	private boolean running;

	private int sleepWhenNoMessages;

	/**
	 * Creates a MqttMessageLogger.
	 * 
	 * @param connectionId Connection ID 
	 * @param queue The message queue to be used
	 * @param connectionSettings The connection details
	 */
	public MqttMessageLogger(
			final int connectionId, final Queue<BaseMqttMessageWithSubscriptions> queue, 
			final MessageLog messageLogSettings, 
			final boolean useAsTemplate, final int sleepWhenNoMessages)
	{
		this.queue = queue;
		this.messageLogSettings = messageLogSettings;
		this.sleepWhenNoMessages = sleepWhenNoMessages;
		
		final String file = messageLogSettings.getLogFile();
		if (file != null)
		{			
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
				
				localLogger = Logger.getLogger("pl.baczkowicz.mqttspy.logger.ConnectionSpecificLogger" + connectionId);
				localLogger.addAppender(appender);
				localLogger.setAdditivity(false);
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
				int mesagesProcessed = 0;
				while (queue.size() > 0)
				{
					mesagesProcessed++;
					if (localLogger != null)
					{
						localLogger.info(SimpleMessageLogComposer.createReceivedMessageLog(queue.remove(), messageLogSettings));
					}
					else
					{
						logger.info(SimpleMessageLogComposer.createReceivedMessageLog(queue.remove(), messageLogSettings));
					}
					
					if (mesagesProcessed > LOG_INTERVAL)
					{
						Utils.logger.warn("Logged " + LOG_INTERVAL + " messages; logger not keeping up; queue size = " + queue.size());						
						mesagesProcessed = 0;
					}
				}
			
				// When no messages present, sleep a bit
				Thread.sleep(sleepWhenNoMessages);
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
	 * The running state.
	 * 
	 * @return True if the logger is running
	 */
	public boolean isRunning()
	{
		return running;
	}
	
	/**
	 * Stops the logger.
	 */
	public void stop()
	{
		running = false;
	}
	
	public Queue<BaseMqttMessageWithSubscriptions> getQueue()
	{
		return queue;
	}
}
