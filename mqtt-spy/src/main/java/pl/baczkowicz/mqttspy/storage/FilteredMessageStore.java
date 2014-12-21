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

/**
 * Message store with filtering.
 * 
 * TODO: could it extend the BasicMessageStore?
 */
public class FilteredMessageStore
{
	final static Logger logger = LoggerFactory.getLogger(FilteredMessageStore.class);
	
	/** This is the same as 'show' flag on topic summary. */
	private final Set<String> shownTopics = new HashSet<String>();
	
	private final MessageListWithObservableTopicSummary filteredMessages;

	private final MessageListWithObservableTopicSummary allMessages;
	
	public FilteredMessageStore(MessageListWithObservableTopicSummary messages, int preferredSize, int maxSize, String name, FormatterDetails messageFormat)
	{
		this.filteredMessages = new MessageListWithObservableTopicSummary(preferredSize, maxSize, "filtered-" + name, messageFormat);
		this.allMessages = messages;
	}
	
	private void reinitialiseFilteredStore()
	{
		filteredMessages.clear();
		
		synchronized (allMessages.getMessages())
		{
			for (MqttContent message : allMessages.getMessages())
			{
				if (shownTopics.contains(message.getTopic()))
				{
					filteredMessages.add(message);
				}
			}
		}
	}	
	
	public boolean updateFilter(final String topic, final boolean show)
	{
		boolean updated = false;
		if (show)
		{
			updated = applyFilter(topic, true);
		}
		else
		{
			updated = removeFilter(topic);
		}
		
		return updated;
	}	
	
	public void addAllFilters()
	{
		removeAllFilters();
		
		synchronized (allMessages.getMessages())
		{
			for (MqttContent message : allMessages.getMessages())
			{
				shownTopics.add(message.getTopic());
				filteredMessages.add(message);				
			}
		}
	}
	
	public void removeAllFilters()
	{
		synchronized (shownTopics)
		{
			shownTopics.clear();
			filteredMessages.clear();
		}
	}
	
	public boolean applyFilters(final Collection<String> topics, final boolean recreateStore)
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
	
	public boolean applyFilter(final String topic, final boolean recreateStore)
	{
		return applyFilters(Arrays.asList(topic), recreateStore);
	}
	
	public boolean removeFilters(final Collection<String> topics)
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

	private boolean removeFilter(final String topic)
	{
		return removeFilters(Arrays.asList(topic));
	}
	
	public MessageListWithObservableTopicSummary getFilteredMessages()
	{
		return filteredMessages;
	}

	public Set<String> getShownTopics()
	{
		return Collections.unmodifiableSet(shownTopics);
	}
}
