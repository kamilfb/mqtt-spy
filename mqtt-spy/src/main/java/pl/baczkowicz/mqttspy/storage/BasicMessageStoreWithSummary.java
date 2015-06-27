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

import pl.baczkowicz.mqttspy.common.generated.ConversionMethod;
import pl.baczkowicz.mqttspy.common.generated.FormatterDetails;
import pl.baczkowicz.mqttspy.utils.FormattingUtils;

/**
 * Basic message store, keeping all messages in a list.
 */
public class BasicMessageStoreWithSummary extends BasicMessageStore
{
	protected final MessageListWithObservableTopicSummary messagesWithSummary;
		
	/** The message format used for this message store. */
	protected FormatterDetails messageFormat = FormattingUtils.createBasicFormatter("default", "Plain", ConversionMethod.PLAIN);

	public BasicMessageStoreWithSummary(final String name, final int preferredSize, final int maxSize, final int maxPayloadLength)
	{
		super(null/*, name, preferredSize, maxSize, maxPayloadLength*/);
		this.messagesWithSummary = new MessageListWithObservableTopicSummary(preferredSize, maxSize, name, messageFormat, maxPayloadLength);
		this.messages = messagesWithSummary;
	}
	
	public MessageListWithObservableTopicSummary getMessageList()
	{
		return messagesWithSummary;
	}

	@Override
	public void clear()
	{
		messagesWithSummary.clear();
		messagesWithSummary.getTopicSummary().clear();
	}	
	
	@Override
	public void setFormatter(final FormatterDetails messageFormat)
	{
		this.messageFormat = messageFormat;		
		messagesWithSummary.getTopicSummary().setFormatter(messageFormat);
	}	
}
