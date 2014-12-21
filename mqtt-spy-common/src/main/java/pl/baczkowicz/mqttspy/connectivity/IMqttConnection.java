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

/** 
 * Basic interface for interacting with an MQTT connection.
 * 
 * TODO: might need adding more methods from BaseMqttConnection
 */
public interface IMqttConnection
{
	/** 
	 * Checks if a message can be published (e.g. client is connected).
	 * 
	 * @return True if publication is possible
	 */
	boolean canPublish();
	
	/**
	 * Publishes a message with the given parameters.
	 * 
	 * @param publicationTopic Publication topic
	 * @param payload Payload
	 * @param qos Quality of Service
	 * @param retained Retained flag
	 * 
	 * @return True if publication was successful
	 */
	boolean publish(final String publicationTopic, final String payload, final int qos, final boolean retained);
}
