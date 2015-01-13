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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.generated.FormatterDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttContent;
import pl.baczkowicz.mqttspy.ui.search.MessageFilter;

/**
 * Message store with filtering.
 * 
 * TODO: could it extend the BasicMessageStore?
 */
public class FilteredMessageStore extends BasicMessageStore
{
	final static Logger logger = LoggerFactory.getLogger(FilteredMessageStore.class);
	
	/** This is the same as 'show' flag on topic summary. */
	private final Set<String> shownTopics = new HashSet<String>();
	
	//private final MessageListWithObservableTopicSummary filteredMessages;

	private final MessageListWithObservableTopicSummary allMessages;
	
	private final Set<MessageFilter> messageFilters = new HashSet<>();
	
	public FilteredMessageStore(final MessageListWithObservableTopicSummary allMessages, 
			final int preferredSize, final int maxSize, final String name, final FormatterDetails messageFormat)
	{
		super("filtered-" + name, preferredSize, maxSize, null, null);
		setFormatter(messageFormat);
		//this.filteredMessages = new MessageListWithObservableTopicSummary(preferredSize, maxSize, "filtered-" + name, messageFormat);
		this.allMessages = allMessages;
	}
	
	public void addMessageFilter(final MessageFilter messageFilter)
	{
		messageFilters.add(messageFilter);
	}
	
	public void removeMessageFilter(final MessageFilter messageFilter)
	{
		messageFilters.remove(messageFilter);
		// TODO: rebuild the store?
	}
	
	public void runFilter(final MessageFilter messageFilter)
	{
		reinitialiseFilteredStore();
	}
	
	@Override
	public boolean messageFiltersEnabled()
	{
		for (final MessageFilter filter : messageFilters)
		{
			if (filter.isActive())
			{
				return true;
			}
		}	
		
		return false;
	}
	
	private void reinitialiseFilteredStore()
	{
		messages.clear();
		
		synchronized (allMessages.getMessages())
		{			
			final int size = allMessages.getMessages().size();
			for (int i = size - 1; i >= 0; i--)
			// TODO: bug - iterates from 0 to end, but always adds to 0 position
			// for (final MqttContent message : allMessages.getMessages())
			{
				final MqttContent message = allMessages.getMessages().get(i);
				
				if (shownTopics.contains(message.getTopic()) && !filterMessage(message))
				{
					messages.add(message);								
				}
			}
		}
	}	
	
	public boolean filterMessage(final MqttContent message)
	{
		for (final MessageFilter filter : messageFilters)
		{
			if (filter.filter(message, messages))
			{
				return true;
			}
		}	
		
		return false;
	}
	
	public boolean updateTopicFilter(final String topic, final boolean show)
	{
		boolean updated = false;
		if (show)
		{
			updated = applyTopicFilter(topic, true);
		}
		else
		{
			updated = removeTopicFilter(topic);
		}
		
		return updated;
	}	
	
	public void addAllTopicFilters()
	{
		removeAllTopicFilters();
		
		synchronized (allMessages.getMessages())
		{
			for (MqttContent message : allMessages.getMessages())
			{
				shownTopics.add(message.getTopic());
				
				if (!filterMessage(message))
				{
					messages.add(message);
				}
			}
		}
	}
	
	public void removeAllTopicFilters()
	{
		synchronized (shownTopics)
		{
			shownTopics.clear();
			messages.clear();
		}
	}
	
	public boolean applyTopicFilters(final Collection<String> topics, final boolean recreateStore)
	{
		synchronized (shownTopics)
		{
			boolean updated = false;
			
			for (final String topic : topics)
			{
				if (!shownTopics.contains(topic))
				{
					logger.debug("Adding {} to active filters for {}; recreate = {}", topic, allMessages.getName(), recreateStore);
					shownTopics.add(topic);														
					updated = true;
				}
			}
			
			// TODO: optimise
			if (updated && recreateStore)
			{
				logger.warn("Recreating store for topics in {}", allMessages.getName());
				reinitialiseFilteredStore();
			}
			
			return updated;
		}
	}
	
	public boolean applyTopicFilter(final String topic, final boolean recreateStore)
	{
		return applyTopicFilters(Arrays.asList(topic), recreateStore);
	}
	
	public boolean removeTopicFilters(final Collection<String> topics)
	{
		synchronized (shownTopics)
		{
			boolean updated = false;
			
			for (final String topic : topics)
			{
				if (shownTopics.contains(topic))
				{
					logger.debug("Removing {} from active filters for {}", topic, allMessages.getName());
					shownTopics.remove(topic);		
					updated = true;
				}
			}
			
			if (updated)
			{
				reinitialiseFilteredStore();
			}
			
			return updated;
		}
	}

	private boolean removeTopicFilter(final String topic)
	{
		return removeTopicFilters(Arrays.asList(topic));
	}
	
	public MessageListWithObservableTopicSummary getFilteredMessages()
	{
		return messages;
	}

	public Set<String> getShownTopics()
	{
		return Collections.unmodifiableSet(shownTopics);
	}
}
