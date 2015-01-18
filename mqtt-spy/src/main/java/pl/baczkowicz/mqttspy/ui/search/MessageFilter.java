/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
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
package pl.baczkowicz.mqttspy.ui.search;

import pl.baczkowicz.mqttspy.connectivity.MqttContent;
import pl.baczkowicz.mqttspy.storage.MessageList;

public interface MessageFilter
{
	/**
	 * Checks if the given message should be filtered out.
	 * 
	 * @param message The message to be checked
	 * @param store The message store (could be modified)
	 * @param updateUi Whether to update the UI
	 * 
	 * @return True if to filter the message out.
	 */
	boolean filter(final MqttContent message, final MessageList messageList, final boolean updateUi);
	
	void reset();
	
	boolean isActive();
}
