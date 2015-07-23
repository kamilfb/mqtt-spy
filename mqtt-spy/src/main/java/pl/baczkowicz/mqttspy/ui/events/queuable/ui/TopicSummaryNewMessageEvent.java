/***********************************************************************************
 * 
 * Copyright (c) 2014 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 * The Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */
package pl.baczkowicz.mqttspy.ui.events.queuable.ui;

import pl.baczkowicz.mqttspy.storage.MessageListWithObservableTopicSummary;
import pl.baczkowicz.mqttspy.storage.FormattedMqttMessage;

public class TopicSummaryNewMessageEvent implements MqttSpyUIEvent
{
	private final FormattedMqttMessage added;
	
	private final boolean showTopic;

	private final MessageListWithObservableTopicSummary list;

	public TopicSummaryNewMessageEvent(final MessageListWithObservableTopicSummary list, final FormattedMqttMessage added, final boolean showTopic)
	{
		this.list = list;
		this.added = added;
		this.showTopic = showTopic;
	}
	
	public FormattedMqttMessage getAdded()
	{
		return added;
	}

	public boolean isShowTopic()
	{
		return showTopic;
	}

	@Override
	public MessageListWithObservableTopicSummary getList()
	{
		return list;
	}
}
