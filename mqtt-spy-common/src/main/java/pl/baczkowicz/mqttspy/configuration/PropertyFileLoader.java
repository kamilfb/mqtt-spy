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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import pl.baczkowicz.mqttspy.exceptions.ConfigurationException;

/**
 * Utility class for loading a property file and reading property values. 
 */
public class PropertyFileLoader
{	
	/** Name of the 'version' property. */
	public static final String VERSION_PROPERTY = "application.version";
	
	/** Name of the 'build number' property. */
	public static final String BUILD_PROPERTY = "application.build";
	
	/** Name of the 'download URL' property. */
	public static final String DOWNLOAD_URL = "application.download.url";	
	
	/** Properties read from the provided file. */
	private final Properties properties;	
	
	/**
	 * Creates the loader and reads a property file into memory.
	 * 
	 * @param propertyFileLocation Class path location
	 * 
	 * @throws ConfigurationException Thrown when cannot process the file
	 */
	public PropertyFileLoader(final String propertyFileLocation) throws ConfigurationException
	{
		properties = readPropertyFile(propertyFileLocation);
	}
	
	/**
	 * Reads a property file into a Properties object.
	 * 
	 * @param propertyFileLocation Class path location
	 * @return Properties file
	 * 
	 * @throws ConfigurationException Thrown when cannot process the file
	 */
	public static Properties readPropertyFile(final String propertyFileLocation) throws ConfigurationException
	{
		final Properties fileProperties = new Properties();
	
		try
		{
			final InputStream inputStream = PropertyFileLoader.class.getResourceAsStream(propertyFileLocation);
			fileProperties.load(inputStream);
			
			if (inputStream == null)
			{
				throw new FileNotFoundException("Property file '" + propertyFileLocation + "' not found in the classpath");
			}
		}
		catch (IOException e)
		{
			throw new ConfigurationException("Cannot load the properties file", e);
		}
		return fileProperties;
	}	

	/**
	 * Retrieve a value of the specified property.
	 *  
	 * @param propertyName Name of the property to retrieve
	 * 
	 * @return Value of the property or an empty string if it doesn't exist
	 */
	public String getProperty(final String propertyName)
	{
		return properties.getProperty(propertyName, "");
	}
		
	/**
	 * Returns the build number, e.g. "16".
	 * 
	 * @return Build number property as string
	 */
	public String getBuildNumber()
	{
		return getProperty(PropertyFileLoader.BUILD_PROPERTY);
	}
	
	/**
	 * Returns the full version number as string, e.g. "0.1.0-beta-10".
	 * 
	 * @return Full version number as string
	 */
	public String getFullVersionNumber()
	{
		return getProperty(PropertyFileLoader.VERSION_PROPERTY) + "-" + getBuildNumber();
	}
	
	/**
	 * Returns the full version name as string, e.g. "0.1.0 beta (build 10)".
	 * @return
	 */
	public String getFullVersionName()
	{
		return getProperty(PropertyFileLoader.VERSION_PROPERTY).replace("-", " ") + " (build " + getBuildNumber() + ")";
	}
}
