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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.MqttContent;
import pl.baczkowicz.mqttspy.events.EventManager;
import pl.baczkowicz.mqttspy.events.queuable.ui.BrowseReceivedMessageEvent;
import pl.baczkowicz.mqttspy.events.queuable.ui.MqttSpyUIEvent;
import pl.baczkowicz.mqttspy.events.queuable.ui.TopicSummaryNewMessageEvent;
import pl.baczkowicz.mqttspy.events.queuable.ui.TopicSummaryRemovedMessageEvent;

/**
 * The top level message store, handling received messages.
 * 
 * TODO: need to rationalise the BasicMessageStore, FilteredMessageStore and
 * MessageStore interface. There are 3 message lists, two in FilteredStore and
 * one in BasicMessageStore - probably only need two.
 */
public class ManagedMessageStoreWithFiltering extends BasicMessageStore
{
	final static Logger logger = LoggerFactory.getLogger(ManagedMessageStoreWithFiltering.class);
	
	/** All topics this store knows about. */
	private final Set<String> allTopics = new HashSet<String>();
	
	private FilteredMessageStore filteredStore;
	
	public ManagedMessageStoreWithFiltering(final String name, final int minMessagesPerTopic, final int preferredSize, final int maxSize, 
			final Queue<MqttSpyUIEvent> uiEventQueue, final EventManager eventManager)
	{
		super(name, preferredSize, maxSize, uiEventQueue, eventManager);
		
		filteredStore = new FilteredMessageStore(messages, preferredSize, maxSize, name, messageFormat);		
		
		new Thread(new MessageStoreGarbageCollector(this, messages, uiEventQueue, minMessagesPerTopic, true, false)).start();
		new Thread(new MessageStoreGarbageCollector(this, filteredStore.getFilteredMessages(), uiEventQueue, minMessagesPerTopic, false, true)).start();
	}
	
	/**
	 * Stores the received message and triggers UI updates. The following
	 * updates are queued as UI events so that the JavaFX thread is not swamped
	 * with hundreds or thousands of requests to do Platform.runLater().
	 * 
	 * @param message Received message
	 */
	public void messageReceived(final MqttContent message)
	{	
		// Record the current state of topics
		final boolean allTopicsShown = !browsingFiltersEnabled();		
		final boolean topicAlreadyExists = allTopics.contains(message.getTopic());
		
		// Start processing the received message...
		
		// 1. Store the topic for the received message
		allTopics.add(message.getTopic());
		
		// 2. Add the message to 'all messages' store - oldest could be removed if the store has reached its max size 
		final MqttContent removed = storeMessage(message);
		
		// 3. Add it to the filtered store if:
		// - message is not filtered out
		// - all messages are shown or the topic is already on the list
		if (!filteredStore.filterMessage(message, true) && (allTopicsShown || filteredStore.getShownTopics().contains(message.getTopic())))
		{
			filteredStore.getFilteredMessages().add(message);
			
			// Message browsing update
			// TODO: even if message is added, others can be removed so the index might not be right for > 1
			uiEventQueue.add(new BrowseReceivedMessageEvent(filteredStore.getFilteredMessages(), message));
		}

		// 4. If the topic doesn't exist yet, add it (e.g. all shown but this is the first message for this topic)
		if (allTopicsShown && !topicAlreadyExists)
		{
			// This doesn't need to trigger 'show first' or sth because the following two UI events should refresh the screen
			filteredStore.applyTopicFilter(message.getTopic(), false);	 
		}

		// 5. Formats the message with the currently selected formatter
		message.format(getFormatter());			
		
		// 6. Summary table update - required are: removed message, new message, and whether to show the topic
		if (removed != null)
		{
			uiEventQueue.add(new TopicSummaryRemovedMessageEvent(messages, removed));
		}
		uiEventQueue.add(new TopicSummaryNewMessageEvent(messages, message, allTopicsShown && !topicAlreadyExists));
	}	
	
	@Override
	public List<MqttContent> getMessages()
	{		
		return filteredStore.getFilteredMessages().getMessages();
	}
	
	@Override
	public MessageListWithObservableTopicSummary getMessageList()
	{
		return filteredStore.getFilteredMessages();
	}
	
	public MessageListWithObservableTopicSummary getNonFilteredMessageList()
	{
		return messages;
	}
	
	public FilteredMessageStore getFilteredMessageStore()
	{
		return filteredStore;
	}	
	
	@Override
	public boolean browsingFiltersEnabled()
	{
		return filteredStore.getShownTopics().size() != allTopics.size();
	}
	
	@Override
	public boolean messageFiltersEnabled()
	{
		return filteredStore.messageFiltersEnabled();
	}
	
	public Collection<String> getAllTopics()
	{
		return Collections.unmodifiableCollection(allTopics);
	}

	@Override
	public void clear()
	{
		super.clear();
		allTopics.clear();
		filteredStore.removeAllTopicFilters();
	}

	public void setAllShowValues(final boolean show)
	{
		if (show)
		{
			filteredStore.addAllTopicFilters();
		}
		else
		{
			filteredStore.removeAllTopicFilters();
		}
		
		messages.getTopicSummary().setAllShowValues(show);
	}
	
	public void setShowValues(final boolean show, final Collection<String> topics)
	{		
		synchronized (topics)
		{
			if (show)
			{
				filteredStore.applyTopicFilters(topics, true);
			}
			else
			{
				filteredStore.removeTopicFilters(topics);
			}
			
			messages.getTopicSummary().setShowValues(topics, show);
		}
	}
	
	public void toggleAllShowValues()
	{
		toggleShowValues(allTopics);
	}

	public void toggleShowValues(final Collection<String> topics)
	{
		final Set<String> topicsToAdd = new HashSet<>();
		final Set<String> topicsToRemove = new HashSet<>();
		
		synchronized (topics)
		{
			for (final String topic : topics)
			{		
				if (filteredStore.getShownTopics().contains(topic))				
				{
					topicsToRemove.add(topic);				
				}
				else
				{
					topicsToAdd.add(topic);
				}
			}
			
			filteredStore.removeTopicFilters(topicsToRemove);
			filteredStore.applyTopicFilters(topicsToAdd, true);
			
			messages.getTopicSummary().toggleShowValues(topics);
		}
	}

	public void setShowValue(final String topic, final boolean show)
	{
		filteredStore.updateTopicFilter(topic, show);
		messages.getTopicSummary().setShowValue(topic, show);
	}
}
