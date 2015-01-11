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
import java.util.Queue;

import pl.baczkowicz.mqttspy.configuration.generated.ConversionMethod;
import pl.baczkowicz.mqttspy.configuration.generated.FormatterDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttContent;
import pl.baczkowicz.mqttspy.events.EventManager;
import pl.baczkowicz.mqttspy.events.queuable.ui.MqttSpyUIEvent;
import pl.baczkowicz.mqttspy.ui.utils.FormattingUtils;

/**
 * Basic message store, keeping all messages in a list.
 */
public class BasicMessageStore implements MessageStore
{
	protected final MessageListWithObservableTopicSummary messages;
		
	/** The message format used for this message store. */
	protected FormatterDetails messageFormat = FormattingUtils.createBasicFormatter("default", "Plain", ConversionMethod.PLAIN);

	// TODO: move to ManagedMessageStore...
	/** Stores events for the UI to be updated. */
	protected final Queue<MqttSpyUIEvent> uiEventQueue;

	// TODO: move to ManagedMessageStore...
	protected final EventManager eventManager;

	public BasicMessageStore(final String name, final int preferredSize, final int maxSize, final Queue<MqttSpyUIEvent> uiEventQueue, final EventManager eventManager)
	{
		this.messages = new MessageListWithObservableTopicSummary(preferredSize, maxSize, name, messageFormat);
		this.eventManager = eventManager;
		this.uiEventQueue = uiEventQueue;
	}
	
	public MqttContent storeMessage(final MqttContent message)
	{
		if (message != null)
		{
			return messages.add(message);
		}	
		
		return null;
	}

	public List<MqttContent> getMessages()
	{
		return messages.getMessages();
	}
	
	public MessageListWithObservableTopicSummary getMessageList()
	{
		return messages;
	}

	public void clear()
	{
		messages.clear();
		messages.getTopicSummary().clear();
	}	
	

	public void setFormatter(final FormatterDetails messageFormat)
	{
		this.messageFormat = messageFormat;		
		messages.getTopicSummary().setFormatter(messageFormat);
	}
	
	public FormatterDetails getFormatter()
	{
		return messageFormat;
	}
	
	public boolean browsingFiltersEnabled()
	{
		return false;
	}
	
	public boolean messageFiltersEnabled()
	{
		return false;
	}

	public String getName()
	{
		return messages.getName();
	}	
}
