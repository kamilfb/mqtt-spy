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
package pl.baczkowicz.mqttspy.events.queuable;

import java.util.Queue;

import javafx.application.Platform;
import pl.baczkowicz.mqttspy.events.EventManager;
import pl.baczkowicz.mqttspy.events.queuable.ui.BrowseReceivedMessageEvent;
import pl.baczkowicz.mqttspy.events.queuable.ui.BrowseRemovedMessageEvent;
import pl.baczkowicz.mqttspy.events.queuable.ui.MqttSpyUIEvent;
import pl.baczkowicz.mqttspy.events.queuable.ui.TopicSummaryNewMessageEvent;
import pl.baczkowicz.mqttspy.events.queuable.ui.TopicSummaryRemovedMessageEvent;
import pl.baczkowicz.mqttspy.utils.ThreadingUtils;

/**
 * This class is responsible for handling queued events. This is done in batches
 * for improved performance. So rather than flooding JavaFX with hundreds or
 * thousands of requests to do runLater, we buffer those events, and then
 * process when in a single runLater.
 */
public class UIEventHandler implements Runnable
{
	private final Queue<MqttSpyUIEvent> uiEventQueue;
	
	private final EventManager eventManager;

	public UIEventHandler(final Queue<MqttSpyUIEvent> uiEventQueue, final EventManager eventManager)
	{
		this.uiEventQueue = uiEventQueue;
		this.eventManager = eventManager;
	}

	@Override
	public void run()
	{
		while (true)
		{
			if (uiEventQueue.size() > 0)
			{
				Platform.runLater(new Runnable()
				{				
					@Override
					public void run()
					{					
						showUpdates();					
					}
				});	
			}
			
			// Sleep so that we don't run all the time - updating the UI 20 times a second should be more than enough
			if (ThreadingUtils.sleep(50))			
			{
				break;
			}
		}		
	}

	private void showUpdates()
	{
		while (uiEventQueue.size() > 0)
		{
			final MqttSpyUIEvent event = uiEventQueue.remove();
			
			if (event instanceof BrowseReceivedMessageEvent)
			{
				eventManager.notifyMessageAdded((BrowseReceivedMessageEvent) event);				
			}
			else if (event instanceof BrowseRemovedMessageEvent)
			{
				eventManager.notifyMessageRemoved((BrowseRemovedMessageEvent) event);
			}			
			else if (event instanceof TopicSummaryNewMessageEvent)
			{
				final TopicSummaryNewMessageEvent updateEvent = (TopicSummaryNewMessageEvent) event;
				
				// Calculate the overall message count per topic
				updateEvent.getList().getTopicSummary().addMessage(updateEvent.getAdded());
				
				// Update the 'show' property if required
				if (updateEvent.isShowTopic())
				{			
					updateEvent.getList().getTopicSummary().setShowValue(updateEvent.getAdded().getTopic(), true);											
				}
			}
			else if (event instanceof TopicSummaryRemovedMessageEvent)
			{
				final TopicSummaryRemovedMessageEvent removeEvent = (TopicSummaryRemovedMessageEvent) event;
				
				// Remove old message from stats
				if (removeEvent.getRemoved() != null)
				{
					removeEvent.getList().getTopicSummary().removeMessage(removeEvent.getRemoved());
				}
			}
		}		
	}
}
