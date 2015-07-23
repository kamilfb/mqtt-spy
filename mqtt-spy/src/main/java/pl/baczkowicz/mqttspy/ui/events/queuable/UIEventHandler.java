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
package pl.baczkowicz.mqttspy.ui.events.queuable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.storage.MessageList;
import pl.baczkowicz.mqttspy.ui.events.EventManager;
import pl.baczkowicz.mqttspy.ui.events.queuable.ui.BrowseReceivedMessageEvent;
import pl.baczkowicz.mqttspy.ui.events.queuable.ui.BrowseRemovedMessageEvent;
import pl.baczkowicz.mqttspy.ui.events.queuable.ui.MqttSpyUIEvent;
import pl.baczkowicz.mqttspy.ui.events.queuable.ui.TopicSummaryNewMessageEvent;
import pl.baczkowicz.mqttspy.ui.events.queuable.ui.TopicSummaryRemovedMessageEvent;
import pl.baczkowicz.mqttspy.utils.ThreadingUtils;
import pl.baczkowicz.mqttspy.utils.TimeUtils;

/**
 * This class is responsible for handling queued events. This is done in batches
 * for improved performance. So rather than flooding JavaFX with hundreds or
 * thousands of requests to do runLater, we buffer those events, and then
 * process them in batches.
 */
public class UIEventHandler implements Runnable
{
	final static Logger logger = LoggerFactory.getLogger(UIEventHandler.class);
	
	private final EventQueueManager uiEventQueue;
	
	private final EventManager eventManager;

	public UIEventHandler(final EventQueueManager uiEventQueue, final EventManager eventManager)
	{
		this.uiEventQueue = uiEventQueue;
		this.eventManager = eventManager;
	}

	@Override
	public void run()
	{
		ThreadingUtils.logThreadStarting("UI event handler");
		
		while (true)
		{
			if (uiEventQueue.getEventCount() > 0)
			{
				showUpdates();
			}
			
			// Sleep so that we don't run all the time - updating the UI 10 times a second should be more than enough
			if (ThreadingUtils.sleep(100))			
			{
				break;
			}
		}		
		
		ThreadingUtils.logThreadEnding();
	}

	private void showUpdates()
	{
		final long start = TimeUtils.getMonotonicTime();
		long processed = 0;
		while (uiEventQueue.getEventCount() > 0)
		{
			final Map<String, List<MqttSpyUIEvent>> events = uiEventQueue.getEvents();
			{
				for (final String type : events.keySet())
				{
					// Remove the event queue from the manager
					final List<MqttSpyUIEvent> eventQueue = uiEventQueue.getAndRemoveEvents(type);
					
					if (eventQueue.isEmpty())
					{
						continue;
					}
					
					processed = processed + eventQueue.size();
					processEventType(eventQueue);					
				}
			}								
		}	
		final long end = TimeUtils.getMonotonicTime();
		if (logger.isTraceEnabled())
		{
			logger.trace("UI event handling of {} items took {} ms", processed, (end - start));
		}
	}
	
	private void processEventType(final List<MqttSpyUIEvent> eventQueue)
	{
		// Split by parent
		final Map<MessageList, List<MqttSpyUIEvent>> parentToEvent = new HashMap<>();		
		for (final MqttSpyUIEvent event : eventQueue)
		{
			List<MqttSpyUIEvent> parentQueue = parentToEvent.get(event.getList());
			
			if (parentQueue == null)
			{
				parentQueue = new ArrayList<>();
				parentToEvent.put(event.getList(), parentQueue);
			}
			
			parentToEvent.get(event.getList()).add(event);
		}

		// Process in batches
		for (final MessageList parent : parentToEvent.keySet())
		{
			Platform.runLater(new Runnable()
			{				
				@Override
				public void run()
				{					
					handleEvents(parentToEvent.get(parent));
				}
			});			
		}		
	}

	@SuppressWarnings("unchecked")
	private void handleEvents(final List<MqttSpyUIEvent> eventQueue)
	{
		final MqttSpyUIEvent event = eventQueue.get(0);
		
		if (event instanceof BrowseReceivedMessageEvent)
		{
			eventManager.notifyMessageAdded(
					(List<BrowseReceivedMessageEvent>)(Object)eventQueue, 
					((BrowseReceivedMessageEvent) event).getList());
		}
		else if (event instanceof BrowseRemovedMessageEvent)
		{
			eventManager.notifyMessageRemoved(
					(List<BrowseRemovedMessageEvent>)(Object)eventQueue, 
					event.getList());
		}
		else if (event instanceof TopicSummaryNewMessageEvent)
		{
			for (final MqttSpyUIEvent item : eventQueue)
			{
				handleTopicSummaryNewMessageEvent((TopicSummaryNewMessageEvent) item);
			}
		}
		else if (event instanceof TopicSummaryRemovedMessageEvent)
		{
			for (final MqttSpyUIEvent item : eventQueue)
			{
				handleTopicSummaryRemovedMessageEvent((TopicSummaryRemovedMessageEvent) item);
			}			
		}
	}
	
	private void handleTopicSummaryNewMessageEvent(final TopicSummaryNewMessageEvent updateEvent)
	{
		// Calculate the overall message count per topic
		updateEvent.getList().getTopicSummary().addMessage(updateEvent.getAdded());
		
		// Update the 'show' property if required
		if (updateEvent.isShowTopic())
		{			
			updateEvent.getList().getTopicSummary().setShowValue(updateEvent.getAdded().getTopic(), true);											
		}
	}
	
	private void handleTopicSummaryRemovedMessageEvent(final TopicSummaryRemovedMessageEvent removeEvent)
	{
		// Remove old message from stats
		if (removeEvent.getRemoved() != null)
		{
			// TODO: does this actually work?
			removeEvent.getList().getTopicSummary().removeMessage(removeEvent.getRemoved());
		}
	}
}
