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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import pl.baczkowicz.mqttspy.storage.BasicMessageStore;
import pl.baczkowicz.mqttspy.ui.events.queuable.ui.MqttSpyUIEvent;

public class EventQueueManager
{
	private Map<BasicMessageStore, Map<String, List<MqttSpyUIEvent>>> events = new HashMap<>();
	
	private AtomicLong eventCount = new AtomicLong();
	
	public void add(final BasicMessageStore parent, final MqttSpyUIEvent event)
	{
		if (!events.containsKey(parent))
		{
			synchronized (events)
			{
				events.put(parent, new HashMap<>());
			}
		}
		
		final Map<String, List<MqttSpyUIEvent>> storeEvents = events.get(parent);
		
		final String eventType = event.getClass().toString();
		if (storeEvents.get(eventType) == null)
		{
			synchronized (storeEvents)
			{
				storeEvents.put(eventType, new ArrayList<>());
			}
		}
		
		final List<MqttSpyUIEvent> eventQueue = storeEvents.get(eventType);
		
		synchronized(eventQueue)
		{
			eventQueue.add(event);
			eventCount.incrementAndGet();
		}		
	}
	
	public Map<BasicMessageStore, Map<String, List<MqttSpyUIEvent>>> getEvents()
	{
		return events;
	}
	
	public long getEventCount()
	{
		return eventCount.get();
	}
	
	public void reduceCount(final long reduceBy)
	{
		eventCount.addAndGet(-reduceBy);
	}
	
	// TODO: reading
	
	// TODO: processing in batches
}
