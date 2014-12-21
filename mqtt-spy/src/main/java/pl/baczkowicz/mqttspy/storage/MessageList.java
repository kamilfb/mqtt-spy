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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.MqttContent;

/**
 * Class for storing received messages.
 */
public class MessageList
{
	final static Logger logger = LoggerFactory.getLogger(MessageList.class);

	public static final int DEFAULT_MAX_SIZE = 5000;
	
	public static final int DEFAULT_MIN_MESSAGES_PER_TOPIC = 10;

	private final List<MqttContent> messages;

	private final int maxSize;

	private final String name;
	
	private final int preferredSize;
	
	public MessageList(final int preferredSize, final int maxSize, final String name)
	{
		this.name = name;
		this.preferredSize = preferredSize;
		this.maxSize = maxSize;
		this.messages = Collections.synchronizedList(new ArrayList<MqttContent>());
	}
	
	public void clear()
	{
		synchronized (messages)
		{
			messages.clear();
		}
	}

	public MqttContent add(final MqttContent message)
	{
		MqttContent removed = null;
		
		synchronized (messages)
		{		
			if (isMaxSize())
			{
				// logger.trace("[{}] Buffer full = {}. Removing message on {}; payload = {}", name, maxSize, message.getTopic(), message.getFormattedPayload());
				removed = messages.remove(messages.size() - 1);
				// logger.trace("[{}] Store update = {}/{}/{}", name, messages.size(), preferredSize, maxSize);
			}
	
			// Store the message
			messages.add(0, message);
			// logger.trace("[{}] Store update = {}/{}/{}", name, messages.size(), preferredSize, maxSize);
			return removed;
		}
	}
	
	public MqttContent remove(final int index)
	{
		MqttContent removed = null;
		
		synchronized (messages)
		{
			removed = messages.remove(index);
			// logger.trace("[{}] Store update = {}/{}/{}", name, messages.size(), preferredSize, maxSize); 
		}
		
		return removed;
	}
	
	public boolean isMaxSize()
	{
		synchronized (messages)
		{
			return messages.size() >= maxSize;
		}
	}
	
	public boolean exceedingPreferredSize()	
	{
		synchronized (messages)
		{
			return messages.size() > preferredSize;
		}
	}

	public List<MqttContent> getMessages()
	{
		return messages;
	}

	public int getPreferredSize()
	{
		return preferredSize;
	}
	
	public int getMaxSize()
	{
		return maxSize;
	}
		
	public String getName()
	{
		return name;
	}
}
