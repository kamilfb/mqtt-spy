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
package pl.baczkowicz.mqttspy.ui.events.observers;

import java.util.List;

import pl.baczkowicz.mqttspy.storage.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.ui.events.queuable.ui.BrowseRemovedMessageEvent;

public interface MessageRemovedObserver
{
	// void onMessageRemoved(final FormattedMqttMessage message, final int messageIndex);
	void onMessageRemoved(final List<BrowseRemovedMessageEvent> events);	
}