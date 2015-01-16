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

import java.util.List;

import pl.baczkowicz.mqttspy.common.generated.MessageLog;
import pl.baczkowicz.mqttspy.common.generated.MessageLogEnum;
import pl.baczkowicz.mqttspy.connectivity.BaseMqttConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttContent;
import pl.baczkowicz.mqttspy.logger.SimpleMessageLogComposer;
import pl.baczkowicz.mqttspy.messages.ReceivedMqttMessage;
import pl.baczkowicz.mqttspy.messages.ReceivedMqttMessageWithSubscriptions;
import pl.baczkowicz.mqttspy.storage.BasicMessageStore;

public class MessageLogUtils
{
	public static ReceivedMqttMessageWithSubscriptions convert(
			final ReceivedMqttMessage message, final BaseMqttConnection connection, final List<String> subscriptions)
	{
		final ReceivedMqttMessageWithSubscriptions messageWithSubs = new ReceivedMqttMessageWithSubscriptions(
				message.getId(), message.getTopic(), message.getMessage(), message.getDate(), connection);
		
		messageWithSubs.setSubscriptions(subscriptions);
		
		return messageWithSubs;
	}
		
	public static String getCurrentMessageAsMessageLog(final BasicMessageStore store, final int messageIndex)
	{
		final MqttContent message = store.getMessages().get(messageIndex);
		return SimpleMessageLogComposer.createReceivedMessageLog(MessageLogUtils.convert(message, null, null), 
				new MessageLog(MessageLogEnum.XML_WITH_PLAIN_PAYLOAD, "", true, true, false, false, false));
	}
	
	public static String getAllMessagesAsMessageLog(final BasicMessageStore store)
	{
		final StringBuffer messages = new StringBuffer();
		
		for (final MqttContent message : store.getMessages())
		{
			messages.append(SimpleMessageLogComposer.createReceivedMessageLog(MessageLogUtils.convert(message, null, null), 
					new MessageLog(MessageLogEnum.XML_WITH_PLAIN_PAYLOAD, "", true, true, false, false, false)));
			messages.append(System.lineSeparator());
		}
		
		return messages.toString();
	}
}
