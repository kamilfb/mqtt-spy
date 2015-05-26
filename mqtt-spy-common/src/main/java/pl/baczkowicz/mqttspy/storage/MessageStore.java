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
package pl.baczkowicz.mqttspy.storage;

import java.util.List;

/**
 * Simple interface for access to a message store.
 * 
 * TODO: needs completing.
 */
public interface MessageStore
{
	/**
	 * Gets all messages stored in the store.
	 * 
	 * @return List of MqttContent instances
	 */
	List<FormattedMqttMessage> getMessages();
	
	/**
	 * Checks if browsing filters are enabled.
	 * 
	 * @return True if filters are enabled
	 */
	boolean browsingFiltersEnabled();
	
	/**
	 * Checks if message filters are enabled.
	 * 
	 * @return True if filters are enabled
	 */
	boolean messageFiltersEnabled();
}
