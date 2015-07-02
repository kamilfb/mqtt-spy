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
package pl.baczkowicz.mqttspy.ui.properties;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.storage.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.utils.TimeUtils;

/**
 * Base property class for representing a message.
 */
public class MqttContentProperties extends BaseTopicProperty
{
	final static Logger logger = LoggerFactory.getLogger(MqttContentProperties.class);
	
	/** The timestamp of when it was received. */
	private StringProperty lastReceivedTimestamp;
	
	/** Last payload .*/
	private StringProperty lastReceivedPayload;
	
	/** Last payload - short.*/
	private StringProperty lastReceivedPayloadShort;

	/** Last message. */
	private FormattedMqttMessage mqttContent;

	private int maxPayloadLength;

	/**
	 * Creates MqttContentProperties with the supplied parameters.
	 * 
	 * @param message The last received message object
	 * @param format The formatting settings to be used for the payload
	 * @param maxPayloadLength Maximum payload length - to make sure UI remains responsive for large messages
	 */
	public MqttContentProperties(final FormattedMqttMessage message, /*final FormatterDetails format, */final int maxPayloadLength)
	{
		super(message.getTopic());
		this.lastReceivedTimestamp = new SimpleStringProperty();
		this.lastReceivedPayload = new SimpleStringProperty();
		this.lastReceivedPayloadShort = new SimpleStringProperty();
		this.maxPayloadLength = maxPayloadLength;
		setMessage(message/*, format*/);
	}
	
//	/**
//	 * Changes the format of the last received payload.
//	 * 
//	 * @param format The new format to be used
//	 */
//	public void changeFormat(final FormatterDetails format)
//	{
//		formattingManager.formatMessage(mqttContent, format);
//		updateReceivedPayload(mqttContent.getFormattedPayload());
//	}
	
	public void updateReceivedPayload(final String formattedText)
	{
		if (maxPayloadLength > 0)
		{			
			final int lengthToSet = Math.min(formattedText.length(), maxPayloadLength); 
			this.lastReceivedPayloadShort.set(formattedText.substring(0, lengthToSet));			
		}
		else
		{		
			this.lastReceivedPayloadShort.set(formattedText);
		}
		
		this.lastReceivedPayload.set(formattedText);
	}

	/**
	 * @return the mqttContent
	 */
	public FormattedMqttMessage getMqttContent()
	{
		return mqttContent;
	}

	/**
	 * @param mqttContent the mqttContent to set
	 */
	public void setMqttContent(FormattedMqttMessage mqttContent)
	{
		this.mqttContent = mqttContent;
	}

	/**
	 * Gets the subscription object for this message.
	 * 
	 * @return The subscription object as MqttSubscription
	 */
	public String getSubscription()
	{
		return mqttContent.getSubscription();
	}

	/**
	 * Sets a newly received message object.
	 * 
	 * @param message The last received message
	 * @param format The format to use
	 */
	public void setMessage(final FormattedMqttMessage message/*, final FormatterDetails format*/)
	{
		this.mqttContent = message;

		this.lastReceivedTimestamp.set(TimeUtils.DATE_WITH_MILLISECONDS_SDF.format(mqttContent.getDate()));
				
		updateReceivedPayload(mqttContent.getFormattedPayload(/*format*/));
	}

	/**
	 * Gets the message's ID.
	 * 
	 * @return Message ID
	 */
	public long getId()
	{
		return mqttContent.getId();
	}

	/**
	 * Gets the last received timestamp property.
	 * 
	 * @return Last received timestamp as StringProperty
	 */
	public StringProperty lastReceivedTimestampProperty()
	{
		return lastReceivedTimestamp;
	}

	/**
	 * Gets the last received payload property.
	 * 
	 * @return Formatted, last received payload, returned as StringProperty
	 */
	public StringProperty lastReceivedPayloadProperty()
	{
		return lastReceivedPayload;
	}
	
	/**
	 * Gets the last received payload property - shorter version.
	 * 
	 * @return Formatted, last received payload, returned as StringProperty
	 */
	public StringProperty lastReceivedPayloadShortProperty()
	{
		return lastReceivedPayloadShort;
	}
}
