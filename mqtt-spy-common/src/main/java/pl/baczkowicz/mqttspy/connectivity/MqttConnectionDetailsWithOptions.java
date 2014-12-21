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

import org.apache.commons.codec.binary.Base64;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import pl.baczkowicz.mqttspy.common.generated.MqttConnectionDetails;
import pl.baczkowicz.mqttspy.exceptions.ConfigurationException;
import pl.baczkowicz.mqttspy.utils.ConfigurationUtils;
import pl.baczkowicz.mqttspy.utils.ConversionUtils;

/**
 * Extends JAXB-generated class for storing MQTT connection details, by adding the Paho's MqttConnectOptions.
 */
public class MqttConnectionDetailsWithOptions extends MqttConnectionDetails
{
	/** Unique ID for this connection - populated when loading configuration. */
	private final int id;
	
	/** Paho's MQTT connection options. */
	private MqttConnectOptions options;

	/**
	 * Instantiates the MqttConnectionDetailsWithOptions.
	 * 
	 * @param details The configured connection details
	 * 
	 * @throws ConfigurationException Thrown when errors detected
	 */
	public MqttConnectionDetailsWithOptions(final int id, final MqttConnectionDetails details) throws ConfigurationException
	{
		this.id = id;
		
		// Copy all parameters
		this.setName(details.getName());
		this.setClientID(details.getClientID());
		this.getServerURI().addAll(details.getServerURI());
		
		this.setConnectionTimeout(details.getConnectionTimeout());
		this.setKeepAliveInterval(details.getKeepAliveInterval());
		this.setCleanSession(details.isCleanSession());
		
		this.setLastWillAndTestament(details.getLastWillAndTestament());
		this.setUserCredentials(details.getUserCredentials());
		this.setReconnectionSettings(details.getReconnectionSettings());
		
		ConfigurationUtils.completeServerURIs(this);
		ConfigurationUtils.populateConnectionDefaults(this);
		
		try
		{
			populateMqttConnectOptions();
		}
		catch (IllegalArgumentException e)
		{
			throw new ConfigurationException("Invalid parameters", e);
		}
	}
	
	/**
	 * Populates the Paho's MqttConnectOptions based on the supplied MqttConnectionDetails.
	 */
	private void populateMqttConnectOptions()
	{
		// Populate MQTT options
		options = new MqttConnectOptions();
				
		if (getServerURI().size() > 1)
		{
			options.setServerURIs(getServerURI().toArray(new String[getServerURI().size()]));
		}
		
		options.setCleanSession(isCleanSession());
		options.setConnectionTimeout(getConnectionTimeout());
		options.setKeepAliveInterval(getKeepAliveInterval());
		
		if (getUserCredentials() != null)
		{
			options.setUserName(getUserCredentials().getUsername());
			options.setPassword(ConversionUtils.base64ToString(getUserCredentials().getPassword()).toCharArray());
		}
		
		if (getLastWillAndTestament() != null)
		{
			options.setWill(getLastWillAndTestament().getTopic(), 
					Base64.decodeBase64(getLastWillAndTestament().getValue()),
					getLastWillAndTestament().getQos(),
					getLastWillAndTestament().isRetained());
		}
	}
	
	/**
	 * Gets the MqttConnectOptions.
	 * 
	 * @return MqttConnectOptions
	 */
	public MqttConnectOptions getOptions()
	{
		return options;
	}

	public int getId()
	{
		return id;
	}
}
