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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.FormatterDetails;
import pl.baczkowicz.mqttspy.storage.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.ui.properties.SubscriptionTopicSummaryProperties;

/**
 * This class is responsible for managing counts of messages for each topic. It
 * is responsible for adding new topic entries and storing the formatting
 * settings.
 */
public class TopicSummary extends TopicMessageCount
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(TopicSummary.class);
	
	protected Map<String, SubscriptionTopicSummaryProperties> topicToSummaryMapping = new HashMap<>();
	
	protected FormatterDetails messageFormat;

	private int maxPayloadLength;

	public TopicSummary(final String name, final int maxPayloadLength)
	{
		super(name);
		this.maxPayloadLength = maxPayloadLength;
	}
	
	public void clear()
	{
		synchronized (topicToSummaryMapping)
		{
			super.clear();
			topicToSummaryMapping.clear();
		}
	}
	
	public void removeMessage(final FormattedMqttMessage message)
	{
		synchronized (topicToSummaryMapping)
		{
			final SubscriptionTopicSummaryProperties value = topicToSummaryMapping.get(message.getTopic());
	
			// There should be something in
			if (value != null)
			{
				value.setCount(value.countProperty().intValue() - 1);
			}
			else
			{
				logger.error("[{}] Found empty value for topic {}", name, message.getTopic());
			}
		}
	}
	
	public SubscriptionTopicSummaryProperties addMessage(final FormattedMqttMessage message)
	{
		SubscriptionTopicSummaryProperties newElement = null;
		
		synchronized (topicToSummaryMapping)
		{
			SubscriptionTopicSummaryProperties value = topicToSummaryMapping.get(message.getTopic());
	
			if (value == null)
			{
				value = new SubscriptionTopicSummaryProperties(false, 1, message, /*messageFormat, */maxPayloadLength);
				topicToSummaryMapping.put(message.getTopic(), value);
				newElement = value;
			}
			else
			{
				value.setCount(value.countProperty().intValue() + 1);	
				value.setMessage(message/*, messageFormat*/);				
			}
			
			logger.trace("[{}] has {} messages", name, value.countProperty().intValue());
		}		
		
		return newElement;
	}

	public void setFormatter(final FormatterDetails messageFormat)
	{
		this.messageFormat = messageFormat;		
	}
}
