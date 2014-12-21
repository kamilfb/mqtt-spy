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
package pl.baczkowicz.mqttspy.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.Main;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.storage.MessageList;

public class ConfigurationUtils
{
	private final static Logger logger = LoggerFactory.getLogger(ConfigurationUtils.class);
	
	public final static int DEFAULT_RECONNECTION_INTERVAL = 5000;
		
	public static void populateConnectionDefaults(final UserInterfaceMqttConnectionDetails connection)
	{
		pl.baczkowicz.mqttspy.utils.ConfigurationUtils.populateConnectionDefaults(connection);
				
		if (connection.getMaxMessagesStored() == null)
		{
			connection.setMaxMessagesStored(MessageList.DEFAULT_MAX_SIZE);
		}
		
		if (connection.getMinMessagesStoredPerTopic() == null)
		{
			connection.setMinMessagesStoredPerTopic(MessageList.DEFAULT_MIN_MESSAGES_PER_TOPIC);
		}
		
		if (connection.isAutoOpen() == null)
		{
			connection.setAutoOpen(false);
		}
		
		if (connection.isAutoConnect() == null)
		{
			connection.setAutoConnect(true);
		}
	}
		
	public static void streamToFile (final InputStream input, final File output) throws IOException 
	{            
	    try (FileOutputStream out = new FileOutputStream(output)) 
	    {
	        IOUtils.copy(input, out);
	    }         
	}
	
	public static boolean createDefaultConfigFromFile(final File orig)
	{
		try
		{ 
			final File dest = ConfigurationManager.getDefaultConfigurationFile();
		
			dest.mkdirs();
			Files.copy(orig.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);			
			
			return true;
			
		}
		catch (IOException e)
		{
			// TODO: show warning dialog for invalid
			logger.error("Cannot copy configuration file", e);
		}
		
		return false;
	}

	
	private static boolean createDefaultConfigFromFile(final InputStream orig) throws IOException
	{
		ConfigurationManager.getDefaultConfigurationFileDirectory().mkdirs();
		final File dest = ConfigurationManager.getDefaultConfigurationFile();

		ConfigurationUtils.streamToFile(orig, dest);

		return true;	
	}
	
	public static boolean createDefaultConfigFromClassPath(final String name)
	{
		final String origin = "/samples/" + name + "-mqtt-spy-configuration.xml";
		try
		{			
			return createDefaultConfigFromFile(Main.class.getResourceAsStream(origin));
		}
		catch (IllegalArgumentException | IOException e)
		{
			// TODO: show warning dialog for invalid
			logger.error("Cannot copy configuration file from {}", origin, e);
		}
		
		return false;
	}
}
