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
package pl.baczkowicz.mqttspy.ui.messagelog;

import java.io.File;
import java.util.List;

import javafx.application.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.LoggedMqttMessage;
import pl.baczkowicz.mqttspy.logger.MessageLogParserUtils;
import pl.baczkowicz.mqttspy.messages.ReceivedMqttMessage;
import pl.baczkowicz.mqttspy.ui.MainController;
import pl.baczkowicz.mqttspy.ui.connections.ConnectionManager;
import pl.baczkowicz.mqttspy.utils.ThreadingUtils;

/**
 * Tasks responsible for reading the message log.
 */
public class LogReaderTask extends TaskWithProgressUpdater<List<ReceivedMqttMessage>>
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(LogReaderTask.class);
	
	/** The file to read from. */
	private File selectedFile;
	
	/** Connection manager - used for loading the message log tab. */
	protected ConnectionManager connectionManager;
	
	/** Main controller. */
	protected MainController controller;
	
	/**
	 * Creates a LogReaderTask with the supplied parameters.
	 * 
	 * @param selectedFile The file to read from
	 * @param connectionManager The connection manager
	 * @param mainController The main controller
	 */
	public LogReaderTask(final File selectedFile, final ConnectionManager connectionManager, final MainController mainController)
	{
		this.selectedFile = selectedFile;
		this.connectionManager = connectionManager;
		this.controller = mainController;
	}

	@Override
	protected List<ReceivedMqttMessage> call() throws Exception
	{
		try
		{
			// Read the message log
			updateMessage("Please wait - reading message log [1/4]");
			updateProgress(0, 4);
			final List<String> fileContent = MessageLogParserUtils.readMessageLog(selectedFile);					
			final long totalItems = fileContent.size();
			updateProgress(totalItems, totalItems * 4);
			
			// Parser the message log (string -> LoggedMqttMessage)
			updateMessage("Please wait - parsing " + fileContent.size() + " messages [2/4]");					
			final List<LoggedMqttMessage> loggedMessages = MessageLogParserUtils.parseMessageLog(fileContent, this, totalItems, totalItems * 4);
			updateProgress(totalItems * 2, totalItems * 4);
								
			// Process the message log (LoggedMqttMessage -> ReceivedMqttMessage)
			updateMessage("Please wait - processing " + loggedMessages.size() + " messages [3/4]");					
			final List<ReceivedMqttMessage> processedMessages = MessageLogParserUtils.processMessageLog(loggedMessages, this, totalItems * 2, totalItems * 4);
			updateProgress(totalItems * 3, totalItems * 4);
			
			// Display message log
			updateMessage("Please wait - displaying " + loggedMessages.size() + " messages [4/4]");	
			Platform.runLater(new Runnable()
			{							
				@Override
				public void run()
				{
					connectionManager.loadMessageLogTab(controller, controller, selectedFile.getName(), processedMessages);								
				}
			});	
			
			// Done!
			updateMessage("Finished!");
			updateProgress(4, 4);
			
			// Make the last message visible for some time
			ThreadingUtils.sleep(500);
			
			return processedMessages;
		}
		catch (Exception e)
		{
			logger.error("Cannot process the message log - {}", selectedFile.getName(), e);
		}
		
		return null;
	}
}
