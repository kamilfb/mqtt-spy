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

/**
 * Basic interface for an MQTT message (e.g. used by scripts accessing received or to be published messages.
 */
public interface IMqttMessage
{
	/**
	 * Gets the message's topic.
	 * 
	 * @return The topic string
	 */
	String getTopic();
	
	/**
	 * Gets the message's payload.
	 * 
	 * @return The payload string
	 */
	String getPayload();
	
	/**
	 * Sets the payload on the message.
	 * 
	 * @param payload The new payload
	 */
	void setPayload(final String payload);
	
	/**
	 * Gets the quality of service.
	 * 
	 * @return The quality of service (0, 1 or 2)
	 */
	int getQoS();
	
	/**
	 * Gets the retained flag for the message.
	 * 
	 * @return True if the message is marked as retained
	 */
	boolean isRetained();
}
