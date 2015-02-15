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
import java.util.List;

import pl.baczkowicz.mqttspy.common.generated.MessageLog;
import pl.baczkowicz.mqttspy.common.generated.MessageLogEnum;
import pl.baczkowicz.mqttspy.connectivity.BaseMqttConnection;
import pl.baczkowicz.mqttspy.logger.SimpleMessageLogComposer;
import pl.baczkowicz.mqttspy.messages.BaseMqttMessage;
import pl.baczkowicz.mqttspy.messages.BaseMqttMessageWithSubscriptions;
import pl.baczkowicz.mqttspy.storage.BasicMessageStore;
import pl.baczkowicz.mqttspy.storage.UiMqttMessage;

public class MessageLogUtils
{
	public static BaseMqttMessageWithSubscriptions convert(
			final BaseMqttMessage message, final BaseMqttConnection connection, final List<String> subscriptions)
	{
		final BaseMqttMessageWithSubscriptions messageWithSubs = new BaseMqttMessageWithSubscriptions(
				message.getId(), message.getTopic(), message.getRawMessage(), message.getDate(), connection);
		
		messageWithSubs.setMatchingSubscriptionTopics(subscriptions);
		
		return messageWithSubs;
	}
		
	public static String getCurrentMessageAsMessageLog(final BasicMessageStore store, final int messageIndex)
	{
		final UiMqttMessage message = store.getMessages().get(messageIndex);
		return SimpleMessageLogComposer.createReceivedMessageLog(MessageLogUtils.convert(message, null, null), 
				new MessageLog(MessageLogEnum.XML_WITH_PLAIN_PAYLOAD, "", true, true, false, false, false));
	}
	
	public static String getAllMessagesAsMessageLog(final BasicMessageStore store)
	{
		final StringBuffer messages = new StringBuffer();
		
		for (final UiMqttMessage message : store.getMessages())
		{
			messages.append(SimpleMessageLogComposer.createReceivedMessageLog(MessageLogUtils.convert(message, null, null), 
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
