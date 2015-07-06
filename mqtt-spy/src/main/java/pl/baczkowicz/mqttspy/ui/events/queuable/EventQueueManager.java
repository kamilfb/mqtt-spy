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
package pl.baczkowicz.mqttspy.ui.events.queuable;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import pl.baczkowicz.mqttspy.storage.BasicMessageStore;
import pl.baczkowicz.mqttspy.ui.events.queuable.ui.MqttSpyUIEvent;

public class EventQueueManager
{
	private Map<BasicMessageStore, Map<String, Queue<MqttSpyUIEvent>>> events = new ConcurrentHashMap<>();
	
	private AtomicLong eventCount = new AtomicLong();
	
	public void add(final BasicMessageStore parent, final MqttSpyUIEvent event)
	{
		final String eventType = event.getClass().toString();
		Map<String, Queue<MqttSpyUIEvent>> storeEvents = events.get(parent);
		
		// Check if the parent map exists
		if (storeEvents == null)
		{
			storeEvents = new ConcurrentHashMap<>();
			events.put(parent, storeEvents);				
		}			
		
		Queue<MqttSpyUIEvent> eventQueue = storeEvents.get(eventType);
		
		// Check if the event queue exists
		if (eventQueue == null)
		{
			eventQueue = new ConcurrentLinkedQueue<>();
			storeEvents.put(eventType, eventQueue);		
		}		
		
		// Add the event
		eventQueue.add(event);
		eventCount.incrementAndGet();			
	}
	
	public void resetStoreEvents(final BasicMessageStore parent, final String eventType)
	{
		final Map<String, Queue<MqttSpyUIEvent>> storeEvents = events.get(parent);
		final Queue<MqttSpyUIEvent> eventQueue = storeEvents.get(eventType);
		
		reduceCount(eventQueue.size());		
		storeEvents.put(eventType, new ConcurrentLinkedQueue<>());		
	}
	
	public Map<BasicMessageStore, Map<String, Queue<MqttSpyUIEvent>>> getEvents()
	{
		return events;
	}
	
	public long getEventCount()
	{
		return eventCount.get();
	}
	
	private void reduceCount(final long reduceBy)
	{
		eventCount.addAndGet(-reduceBy);
	}
	
	// TODO: reading
	
	// TODO: processing in batches
}
