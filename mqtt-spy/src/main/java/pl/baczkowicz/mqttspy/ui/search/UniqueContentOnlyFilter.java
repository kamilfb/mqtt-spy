/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
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
package pl.baczkowicz.mqttspy.ui.search;

import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.storage.BasicMessageStore;
import pl.baczkowicz.mqttspy.storage.MessageList;
import pl.baczkowicz.mqttspy.ui.events.queuable.EventQueueManager;
import pl.baczkowicz.mqttspy.ui.events.queuable.ui.BrowseRemovedMessageEvent;

public class UniqueContentOnlyFilter implements MessageFilter
{
	private boolean uniqueContentOnly;
	
	private int deleted = 0;
	
	/** Stores events for the UI to be updated. */
	protected final EventQueueManager uiEventQueue;

	private BasicMessageStore store;
	
	public UniqueContentOnlyFilter(final BasicMessageStore store, final EventQueueManager uiEventQueue)
	{
		this.uiEventQueue = uiEventQueue;
		this.store = store;
	}
	
	@Override
	public boolean filter(final FormattedMqttMessage message, final MessageList messageList, final boolean updateUi)
	{
		if (!uniqueContentOnly || messageList.getMessages().size() == 0)
		{
			return false;			
		}
		
		final FormattedMqttMessage lastMessage = messageList.getMessages().get(0);
		
		if (message.getFormattedPayload().equals(lastMessage.getFormattedPayload()) && message.getTopic().equals(lastMessage.getTopic()))
		{
			final FormattedMqttMessage deletedMessage = messageList.getMessages().remove(0);
			
			if (updateUi)
			{
				uiEventQueue.add(store, new BrowseRemovedMessageEvent(messageList, deletedMessage, 0));
			}
			deleted++;
		}
		
		return false;
	}

	/**
	 * Sets the flag.
	 * 
	 * @return the uniqueContentOnly
	 */
	public boolean isUniqueContentOnly()
	{
		return uniqueContentOnly;
	}

	/**
	 * Gets the flag.
	 * 
	 * @param uniqueContentOnly the uniqueContentOnly to set
	 */
	public void setUniqueContentOnly(boolean uniqueContentOnly)
	{
		this.uniqueContentOnly = uniqueContentOnly;
		
		if (!uniqueContentOnly)
		{
			reset();
		}
	}

	@Override
	public void reset()
	{
		deleted = 0;		
	}

	@Override
	public boolean isActive()
	{
		return deleted > 0;
	}

}
