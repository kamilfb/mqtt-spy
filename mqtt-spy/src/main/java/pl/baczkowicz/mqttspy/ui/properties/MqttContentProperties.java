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
import pl.baczkowicz.mqttspy.configuration.generated.FormatterDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttContent;
import pl.baczkowicz.mqttspy.connectivity.MqttSubscription;
import pl.baczkowicz.mqttspy.utils.TimeUtils;

/**
 * Base property class for representing a message.
 */
public class MqttContentProperties extends BaseTopicProperty
{
	/** The timestamp of when it was received. */
	private StringProperty lastReceivedTimestamp;
	
	/** Last payload .*/
	private StringProperty lastReceivedPayload;

	/** Last message. */
	private MqttContent mqttContent;

	/**
	 * Creates MqttContentProperties with the supplied parameters.
	 * 
	 * @param message The last received message object
	 * @param format The formatting settings to be used for the payload
	 */
	public MqttContentProperties(final MqttContent message, final FormatterDetails format)
	{
		super(message.getTopic());
		this.lastReceivedTimestamp = new SimpleStringProperty();
		this.lastReceivedPayload = new SimpleStringProperty();
		setMessage(message, format);
	}
	
	/**
	 * Changes the format of the last received payload.
	 * 
	 * @param format The new format to be used
	 */
	public void changeFormat(final FormatterDetails format)
	{
		this.lastReceivedPayload.set(mqttContent.getFormattedPayload(format));
	}

	/**
	 * Gets the subscription object for this message.
	 * 
	 * @return The subscription object as MqttSubscription
	 */
	public MqttSubscription getSubscription()
	{
		return mqttContent.getSubscription();
	}

	/**
	 * Sets a newly received message object.
	 * 
	 * @param message The last received message
	 * @param format The format to use
	 */
	public void setMessage(final MqttContent message, final FormatterDetails format)
	{
		this.mqttContent = message;

		this.lastReceivedTimestamp.set(TimeUtils.DATE_WITH_MILLISECONDS_SDF.format(mqttContent.getDate()));
		this.lastReceivedPayload.set(mqttContent.getFormattedPayload(format));
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
}
