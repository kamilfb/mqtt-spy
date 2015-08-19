/***********************************************************************************
 * 
 * Copyright (c) 2014 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 * The Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */
package pl.baczkowicz.mqttspy.daemon.configuration;

import java.io.File;
import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.ScriptDetails;
import pl.baczkowicz.mqttspy.configuration.PropertyFileLoader;
import pl.baczkowicz.mqttspy.daemon.configuration.generated.MqttSpyDaemonConfiguration;
import pl.baczkowicz.mqttspy.utils.ConfigurationUtils;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.xml.XMLParser;

/**
 * Helper class for loading the daemon's configuration.
 */
public class ConfigurationLoader extends PropertyFileLoader
{
	/** Configuration package. */
	public static final String PACKAGE = "pl.baczkowicz.mqttspy.daemon.configuration.generated";
	
	/** Configuration schema. */
	public static final String SCHEMA = "/mqtt-spy-daemon-configuration.xsd";
	
	/** Location of the properties file. */
	public static final String DEFAULT_PROPERTIES_FILE_NAME = "/mqtt-spy-daemon.properties";

	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);
	
	/** XML config parser. */
	private final XMLParser parser;

	/** Daemon's configuration (once parsed). */
	private MqttSpyDaemonConfiguration configuration;
	
	/**
	 * Creates the loader. 
	 * 
	 * @throws XMLException Thrown if cannot read the properties file or instantiate the config parser
	 */
	public ConfigurationLoader() throws XMLException
	{
		super();
		readFromClassPath(DEFAULT_PROPERTIES_FILE_NAME);
		
		this.parser = new XMLParser(PACKAGE, new String[] {ConfigurationUtils.COMMON_SCHEMA, SCHEMA});					
	}
	
	/**
	 * Loads configuration from the given file.
	 * 
	 * @param file The file to load from
	 * 
	 * @return True if all OK
	 */
	public boolean loadConfiguration(final File file)
	{
		try
		{
			configuration = (MqttSpyDaemonConfiguration) parser.loadFromFile(file);	
			populateDefaults();
			return true;
		}
		catch (XMLException e)
		{							
			logger.error("Cannot process the configuration file at " + file.getAbsolutePath(), e);
		}
		catch (FileNotFoundException e)
		{
			logger.error("Cannot read the configuration file from " + file.getAbsolutePath(), e);
		}
		
		return false;
	}

	/**
	 * Populates the connection configuration with default values.
	 */
	private void populateDefaults()
	{
		for (final ScriptDetails scriptDetails : configuration.getConnection().getBackgroundScript())
		{
			if (scriptDetails.isRepeat() == null)
			{
				scriptDetails.setRepeat(false);
			}
		}
		
		ConfigurationUtils.populateMessageLogDefaults(configuration.getConnection().getMessageLog());
	}

	/**
	 * Gets the configuration value.
	 * 
	 * @return The daemon's configuration
	 */
	public MqttSpyDaemonConfiguration getConfiguration()
	{
		return configuration;
	}
}
