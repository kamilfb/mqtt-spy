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
package pl.baczkowicz.mqttspy.messages;

import pl.baczkowicz.mqttspy.common.generated.BaseMqttMessage;

/**
 * Wrapper around the base MQTT message, exposing standard access methods.
 */
public class BaseMqttMessageWrapper implements IMqttMessage
{
	/** Wrapped MQTT message. */
	private final BaseMqttMessage message;

	/**
	 * Creates a BaseMqttMessageWrapper from the provided message.
	 * 
	 * @param message The message to be wrapped
	 */
	public BaseMqttMessageWrapper(final BaseMqttMessage message)
	{
		this.message = message;
	}

	@Override
	public String getTopic()
	{
		return message.getTopic();
	}

	@Override
	public String getPayload()
	{
		return message.getValue();
	}

	@Override
	public int getQoS()
	{
		return message.getQos();
	}

	@Override
	public boolean isRetained()
	{
		return message.isRetained();
	}

	@Override
	public void setPayload(String payload)
	{
		message.setValue(payload);		
	}
}
