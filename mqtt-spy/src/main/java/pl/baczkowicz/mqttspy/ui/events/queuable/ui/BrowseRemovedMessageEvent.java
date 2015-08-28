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
package pl.baczkowicz.mqttspy.ui.events.queuable.ui;

import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.storage.MessageList;

public class BrowseRemovedMessageEvent implements MqttSpyUIEvent
{
	private final FormattedMqttMessage message;
	
	private final int messageIndex;

	private final MessageList store;

	public BrowseRemovedMessageEvent(final MessageList store, final FormattedMqttMessage message, final int messageIndex)
	{
		this.store = store;
		this.message = message;
		this.messageIndex = messageIndex;
	}

	public FormattedMqttMessage getMessage()
	{
		return message;
	}

	public int getMessageIndex()
	{
		return messageIndex;
	}

	@Override
	public MessageList getList()
	{
		return store;
	}
}
