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
package pl.baczkowicz.mqttspy.storage.summary;

import java.util.HashMap;
import java.util.Map;

import pl.baczkowicz.mqttspy.connectivity.MqttContent;

/**
 * This class contains message count for each topic. These values are not directly displayed on the UI.
 */
public class TopicMessageCount
{
	private Map<String, Integer> messageCountPerTopic = new HashMap<>();

	protected final String name;

	public TopicMessageCount(final String name)
	{
		this.name = name;
	}
	
	public void clear()
	{
		synchronized (messageCountPerTopic)
		{
			messageCountPerTopic.clear();
		}
	}	

	public int getCountForTopic(final String topic)
	{
		synchronized (messageCountPerTopic)
		{
			if (messageCountPerTopic.get(topic) != null)
			{
				return messageCountPerTopic.get(topic);
			}
		}
		
		return 0;
	}
	
	public void increaseCount(final MqttContent message)
	{
		synchronized (messageCountPerTopic)
		{
			Integer value = messageCountPerTopic.get(message.getTopic());
			
			if (value == null)
			{
				messageCountPerTopic.put(message.getTopic(), 1);
			}
			else
			{
				messageCountPerTopic.put(message.getTopic(), value + 1);
			}
		}
	}
	
	public void decreaseCount(final MqttContent message)
	{
		Integer value = messageCountPerTopic.get(message.getTopic());
		
		if (value == null)
		{
			messageCountPerTopic.put(message.getTopic(), 0);
		}
		else
		{
			messageCountPerTopic.put(message.getTopic(), value - 1);
		}
	}		
}
