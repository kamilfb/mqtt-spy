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
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import pl.baczkowicz.mqttspy.ui.events.queuable.ui.MqttSpyUIEvent;

@SuppressWarnings("unchecked")
public class EventQueue
{
	private Map<Class<MqttSpyUIEvent>, Queue<MqttSpyUIEvent>> events = new HashMap<>();
	
	public void add(final MqttSpyUIEvent event)
	{
		if (!events.containsKey(event.getClass()))
		{
			events.put((Class<MqttSpyUIEvent>) event.getClass(), new LinkedBlockingQueue<>());
		}
		
		events.get(event.getClass()).add(event);
	}
	
	// TODO: reading
	
	// TODO: processing in batches
}
