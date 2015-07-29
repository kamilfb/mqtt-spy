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
package pl.baczkowicz.mqttspy.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttTopic;

import pl.baczkowicz.mqttspy.common.generated.ProtocolEnum;
import pl.baczkowicz.mqttspy.exceptions.MqttSpyException;
import pl.baczkowicz.mqttspy.utils.ConversionUtils;

/**
 * MQTT utilities.
 */
public class MqttUtils
{
	/** TCP prefix. */
	public final static String TCP_PREFIX = "tcp://";
	
	/** SSL prefix. */
	public final static String SSL_PREFIX = "ssl://";
	
	/** Multi-level MQTT topic wildcard. */
	public static final String MULTI_LEVEL_WILDCARD = "#";
	
	/** Single-level MQTT topic wildcard. */
	public static final String SINGLE_LEVEL_WILDCARD = "+";
	
	/** MQTT topic level delimiter. */
	public static final String TOPIC_DELIMITER = "/";
	
	/** Max client length for MQTT 3.1. */
	public static final int MAX_CLIENT_LENGTH_FOR_3_1 = 23;

	/** Client ID timestamp delimiter. */
	private static final String CLIENT_ID_TIMESTAMP_DELIMITER = "_";

	/** Client ID timestamp format. */
	private static final String CLIENT_ID_TIMESTAMP_FORMAT = "HHmmssSSS";
	
	/** Client ID timestamp SDF. */
	private static final SimpleDateFormat CLIENT_ID_SDF = new SimpleDateFormat(CLIENT_ID_TIMESTAMP_FORMAT);
	
	/**
	 * Removes last topic delimiter if present.
	 * 
	 * @param topic Topic to remove the delimiter from
	 * 
	 * @return Topic with removed delimiter
	 */
	public static String removeLastDelimiter(String topic)
	{
		if (topic.endsWith(TOPIC_DELIMITER))
		{
			topic = topic.substring(0, topic.length() - TOPIC_DELIMITER.length());
		}
		
		return topic;
	}		

	/**
	 * Generate client ID with timestamp.
	 * 
	 * @param clientId The client ID to use as source
	 * 
	 * @return The generated client ID
	 */
	public static String generateClientIdWithTimestamp(final String clientId, final ProtocolEnum protocol)
	{
		final int addedLength = CLIENT_ID_TIMESTAMP_FORMAT.length() + CLIENT_ID_TIMESTAMP_DELIMITER.length();
		final int index = clientId.lastIndexOf(CLIENT_ID_TIMESTAMP_DELIMITER);
		String newClientId = clientId;
		
		// for e.g. k-01; index = 1; added length = 3; length == 4
		if (index >= 0 && (index + addedLength == newClientId.length()))
		{
			newClientId = newClientId.substring(0, index); 
		}		
	
		if (limitClientId(protocol) && newClientId.length() + addedLength > MAX_CLIENT_LENGTH_FOR_3_1)
		{
			newClientId = newClientId.substring(0, MAX_CLIENT_LENGTH_FOR_3_1 - addedLength);
		}

		newClientId = newClientId + CLIENT_ID_TIMESTAMP_DELIMITER + CLIENT_ID_SDF.format(new Date());

		return newClientId;
	}
	
	public static boolean limitClientId(final ProtocolEnum protocol)
	{
		if (ProtocolEnum.MQTT_3_1.equals(protocol) || ProtocolEnum.MQTT_DEFAULT.equals(protocol))
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Completes the server URI with the TCP prefix if not present.
	 *  
	 * @param brokerAddress The broker URL to complete
	 * 
	 * @return Complete URL
	 */
	public static String getCompleteServerURI(final String brokerAddress, final boolean sslEnabled)
	{
		String serverURI = brokerAddress.replaceAll(TCP_PREFIX, "").replaceAll(SSL_PREFIX, "");
		
		if (sslEnabled)
		{
			serverURI = SSL_PREFIX + serverURI;			
		}
		else
		{			
			serverURI = TCP_PREFIX + serverURI;
		}		

		return serverURI;
	}
	
	/**
	 * Encodes the given password to Base 64.
	 * 
	 * @param value The password to encode
	 * 
	 * @return The encoded password
	 */
	public static String encodePassword(final String value)
	{
		return ConversionUtils.stringToBase64(value);
	}
	
	/**
	 * Decodes the given password from Base 64.
	 * 
	 * @param value The password to decode
	 * 
	 * @return The decoded password
	 */
	public static String decodePassword(final String value)
	{
		return ConversionUtils.base64ToString(value);
	}
	
	public static boolean recordTopic(final String newTopic, final List<String> topics)
	{
		final boolean contains = topics.contains(newTopic);
		
		if (!contains)
		{
			topics.add(newTopic);
			return true;
		}
		
		return false;		
	}
	
	public static void validateTopic(final String topic) throws MqttSpyException
	{
		try
		{
			MqttTopic.validate(topic, true);
		}
		catch (Exception e)
		{
			throw new MqttSpyException(e.getMessage(), e);
		}
	}
}
