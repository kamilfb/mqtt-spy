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
package pl.baczkowicz.mqttspy.versions;

import java.io.IOException;
import java.net.URL;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.PropertyFileLoader;
import pl.baczkowicz.mqttspy.exceptions.XMLException;
import pl.baczkowicz.mqttspy.ui.controlpanel.ItemStatus;
import pl.baczkowicz.mqttspy.versions.generated.MqttSpyVersions;
import pl.baczkowicz.mqttspy.versions.generated.ReleaseStatus;
import pl.baczkowicz.mqttspy.xml.XMLParser;

/**
 * Manages loading of the version information.
 */
public class VersionManager extends XMLParser
{
	/** Packages where the JAXB-generated classes are store. */
	private static final String PACKAGE = "pl.baczkowicz.mqttspy.versions.generated";
	
	/** Schema location. */
	private static final String SCHEMA = "/mqtt-spy-versions.xsd";

	/** Used for reading the version URL property. */
	private PropertyFileLoader propertyLoader;
	
	/** The version information retrieved from the URL. */
	private MqttSpyVersions versions;

	/**
	 * Creates the VersionManager.
	 * 
	 * @param propertyLoader Used for reading the version URL property
	 * 
	 * @throws XMLException Thrown when cannot create the VersionManager
	 */
	public VersionManager(final PropertyFileLoader propertyLoader) throws XMLException
	{
		super(PACKAGE, SCHEMA);
		this.propertyLoader = propertyLoader;
					
		this.versions = new MqttSpyVersions();
	}
	
	/**
	 * Loads version information from the URL.
	 * 
	 * @return Object representing the version information
	 * 
	 * @throws XMLException Thrown when cannot load the version information
	 */
	public MqttSpyVersions loadVersions() throws XMLException
	{
		try
		{
			final URL url = new URL(propertyLoader.getProperty(ConfigurationManager.VERSION_INFO_URL));

			versions = (MqttSpyVersions) loadFromInputStream(url.openStream());			
		}
		catch (IOException e)
		{
			throw new XMLException("Cannot read version info from " + propertyLoader.getProperty(ConfigurationManager.VERSION_INFO_URL), e);
		}
				
		return versions;
	}
	
	/**
	 * Gets the version information object.
	 * 
	 * @return Object representing the version information
	 */
	public MqttSpyVersions getVersions()
	{
		return versions;
	}

	/**
	 * Checks whether the current release is within the range of the given release to check.
	 * 
	 * @param currentRelease The current release to check
	 * @param release The release to check against
	 * 
	 * @return True if the current release is within the range of the given release.
	 * 
	 */
	public static boolean isInRange(final String currentRelease, final ReleaseStatus release)
	{
		if ((new DefaultArtifactVersion(currentRelease).compareTo(new DefaultArtifactVersion(release.getFromVersion())) >= 0)
			&& (new DefaultArtifactVersion(currentRelease).compareTo(new DefaultArtifactVersion(release.getToVersion())) <= 0))
		{
			return true;		
		}
		
		return false;
	}
	
	/**
	 * Gets item status enum for the given release.
	 * 
	 * @param release The release to check
	 * 
	 * @return ItemStatus enum based on the UpdateStatus field of the release
	 */
	public static ItemStatus convertVersionStatus(final ReleaseStatus release)
	{
		switch (release.getUpdateStatus())
		{
			case CRITICAL:
				return ItemStatus.ERROR;
			case UPDATE_RECOMMENDED:
				return ItemStatus.WARN;
			case NEW_AVAILABLE:
				return ItemStatus.INFO;
			case ON_LATEST:
				return ItemStatus.OK;
			default:
				return ItemStatus.ERROR;		
		}
	}
}
