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
package pl.baczkowicz.mqttspy.ui.messagelog;

import java.util.Collection;

import pl.baczkowicz.mqttspy.common.generated.MessageLog;
import pl.baczkowicz.mqttspy.common.generated.MessageLogEnum;
import pl.baczkowicz.mqttspy.logger.SimpleMessageLogComposer;
import pl.baczkowicz.mqttspy.storage.BasicMessageStoreWithSummary;
import pl.baczkowicz.mqttspy.storage.FormattedMqttMessage;

public class MessageLogUtils
{
	public static String getCurrentMessageAsMessageLog(final BasicMessageStoreWithSummary store, final int messageIndex)
	{
		final FormattedMqttMessage message = store.getMessages().get(messageIndex);
		return SimpleMessageLogComposer.createReceivedMessageLog(message, 
				new MessageLog(MessageLogEnum.XML_WITH_PLAIN_PAYLOAD, "", true, true, false, false, false));
	}
	
	public static String getAllMessagesAsMessageLog(final BasicMessageStoreWithSummary store)
	{
		final StringBuffer messages = new StringBuffer();
		
		for (final FormattedMqttMessage message : store.getMessages())
		{
			messages.append(SimpleMessageLogComposer.createReceivedMessageLog(message, 
					new MessageLog(MessageLogEnum.XML_WITH_PLAIN_PAYLOAD, "", true, true, false, false, false)));
			messages.append(System.lineSeparator());
		}
		
		return messages.toString();
	}
	
	public static String getAllTopicsAsString(final Collection<String> topics)
	{
		final StringBuffer messages = new StringBuffer();
		
		for (final String topic : topics)
		{
			messages.append(topic);
			messages.append(System.lineSeparator());
		}
		
		return messages.toString();
	}
}
