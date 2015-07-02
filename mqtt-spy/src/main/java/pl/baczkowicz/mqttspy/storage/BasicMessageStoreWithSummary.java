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

import pl.baczkowicz.mqttspy.common.generated.FormatterDetails;
import pl.baczkowicz.mqttspy.scripts.FormattingManager;

/**
 * Basic message store, keeping all messages in a list.
 */
public class BasicMessageStoreWithSummary extends BasicMessageStore
{
	protected FormattingManager formattingManager;
	
	private MessageListWithObservableTopicSummary messageListWithTopicSummary;
	
	public BasicMessageStoreWithSummary(final String name, final int preferredSize, final int maxSize, final int maxPayloadLength, 
			final FormattingManager formattingManager)
	{
		super(null);
		this.formattingManager = formattingManager;
		messageListWithTopicSummary = new MessageListWithObservableTopicSummary(preferredSize, maxSize, name, messageFormat, maxPayloadLength); 
		setMessageList(messageListWithTopicSummary);
	}
	
	@Override
	public MessageListWithObservableTopicSummary getMessageList()
	{
		return messageListWithTopicSummary;
	}

	@Override
	public void clear()
	{
		messageListWithTopicSummary.clear();
		messageListWithTopicSummary.getTopicSummary().clear();
	}	
	
	@Override
	public void setFormatter(final FormatterDetails messageFormat)
	{
		this.messageFormat = messageFormat;	
		messageListWithTopicSummary.getTopicSummary().setFormatter(messageFormat, formattingManager);
	}	
}
