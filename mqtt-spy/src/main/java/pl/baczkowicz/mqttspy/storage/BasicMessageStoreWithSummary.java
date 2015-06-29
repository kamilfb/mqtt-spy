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

/**
 * Basic message store, keeping all messages in a list.
 */
public class BasicMessageStoreWithSummary extends BasicMessageStore
{
	/** The message format used for this message store. */
	// protected FormatterDetails messageFormat = FormattingUtils.createBasicFormatter("default", "Plain", ConversionMethod.PLAIN);

	public BasicMessageStoreWithSummary(final String name, final int preferredSize, final int maxSize, final int maxPayloadLength)
	{
		super(null);
		setMessageList(new MessageListWithObservableTopicSummary(preferredSize, maxSize, name, messageFormat, maxPayloadLength));
	}
	
	@Override
	public MessageListWithObservableTopicSummary getMessageList()
	{
		return (MessageListWithObservableTopicSummary) super.getMessageList();
	}

	@Override
	public void clear()
	{
		getMessageList().clear();
		getMessageList().getTopicSummary().clear();
	}	
	
	@Override
	public void setFormatter(final FormatterDetails messageFormat)
	{
		this.messageFormat = messageFormat;		
		getMessageList().getTopicSummary().setFormatter(messageFormat);
	}	
}
