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

import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import pl.baczkowicz.mqttspy.common.generated.MqttConnectionDetails;
import pl.baczkowicz.mqttspy.common.generated.ProtocolEnum;
import pl.baczkowicz.mqttspy.common.generated.SslModeEnum;
import pl.baczkowicz.mqttspy.common.generated.SslProperty;
import pl.baczkowicz.mqttspy.exceptions.ConfigurationException;
import pl.baczkowicz.mqttspy.exceptions.MqttSpyException;
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
		this.setProtocol(details.getProtocol());
		
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
		
		this.setSSL(details.getSSL());
		final boolean sslEnabled = details.getSSL() != null && details.getSSL().getMode() != null && !details.getSSL().getMode().equals(SslModeEnum.DISABLED);
		
		ConfigurationUtils.completeServerURIs(this, sslEnabled);
		ConfigurationUtils.populateConnectionDefaults(this);
		
		try
		{
			populateMqttConnectOptions();
		}
		catch (IllegalArgumentException | MqttSpyException e)
		{
			throw new ConfigurationException("Invalid parameters", e);
		}
	}
	
	/**
	 * Populates the Paho's MqttConnectOptions based on the supplied MqttConnectionDetails.
	 * @throws MqttSpyException Thrown when SSL configuration is not valid
	 */
	private void populateMqttConnectOptions() throws MqttSpyException
	{
		// Populate MQTT options
		options = new MqttConnectOptions();
				
		if (ProtocolEnum.MQTT_3_1_1.equals(getProtocol()))
		{
			options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
		}
		else if (ProtocolEnum.MQTT_3_1.equals(getProtocol()))
		{
			options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
		}
		else
		{
			options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_DEFAULT);
		}
		
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
		
		// SSL and TLS
		if (getSSL() == null) 
		{
			// No SSL/TLS settings available
		} 
		else 
		{
			if (SslModeEnum.PROPERTIES.equals(getSSL().getMode()))			
			{
				Properties props = new Properties();
				for (final SslProperty prop : getSSL().getProperty())
				{
					props.put(prop.getName(), prop.getValue());
				}
				options.setSSLProperties(props);
			}
			else if (SslModeEnum.SERVER_AND_CLIENT.equals(getSSL().getMode()))
			{
				options.setSocketFactory(SslUtils.getSocketFactory(
						getSSL().getCertificateAuthorityFile(), 
						getSSL().getClientCertificateFile(),
						getSSL().getClientKeyFile(),
						getSSL().getClientKeyPassword(),
						getSSL().getProtocol()));
			}
			else if (SslModeEnum.SERVER_ONLY.equals(getSSL().getMode()))
			{
				options.setSocketFactory(SslUtils.getSocketFactory(
						getSSL().getCertificateAuthorityFile(), 
						getSSL().getProtocol()));
			}
			
			// TODO: set connection protocol to SSL if not done already
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
