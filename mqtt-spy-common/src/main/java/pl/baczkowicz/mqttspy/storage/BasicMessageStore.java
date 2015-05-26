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

import java.util.List;

import pl.baczkowicz.mqttspy.common.generated.ConversionMethod;
import pl.baczkowicz.mqttspy.common.generated.FormatterDetails;
import pl.baczkowicz.mqttspy.utils.FormattingUtils;

/**
 * Basic message store, keeping all messages in a list.
 */
public class BasicMessageStore implements MessageStore
{
	protected final MessageList messages;
		
	/** The message format used for this message store. */
	protected FormatterDetails messageFormat = FormattingUtils.createBasicFormatter("default", "Plain", ConversionMethod.PLAIN);

	public BasicMessageStore(final String name, final int preferredSize, final int maxSize, final int maxPayloadLength)
	{
		// this.messages = new MessageListWithObservableTopicSummary(preferredSize, maxSize, name, messageFormat, maxPayloadLength);
		this.messages = new MessageList(preferredSize, maxSize, name);
	}
	
	public FormattedMqttMessage storeMessage(final FormattedMqttMessage message)
	{
		if (message != null)
		{
			return messages.add(message);
		}	
		
		return null;
	}

	public List<FormattedMqttMessage> getMessages()
	{
		return messages.getMessages();
	}
	
	public MessageList getMessageList()
	{
		return messages;
	}

	public void clear()
	{
		messages.clear();
	}	
	
	public void setFormatter(final FormatterDetails messageFormat)
	{
		this.messageFormat = messageFormat;		
	}
	
	public FormatterDetails getFormatter()
	{
		return messageFormat;
	}
	
	public boolean browsingFiltersEnabled()
	{
		return false;
	}
	
	public boolean messageFiltersEnabled()
	{
		return false;
	}

	public String getName()
	{
		return messages.getName();
	}	
}
