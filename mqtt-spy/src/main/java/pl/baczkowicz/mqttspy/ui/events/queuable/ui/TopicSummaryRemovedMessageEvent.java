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
package pl.baczkowicz.mqttspy.ui.events.queuable.ui;

import pl.baczkowicz.mqttspy.storage.MessageListWithObservableTopicSummary;
import pl.baczkowicz.mqttspy.storage.FormattedMqttMessage;

public class TopicSummaryRemovedMessageEvent implements MqttSpyUIEvent
{
	private final FormattedMqttMessage removed;

	private final MessageListWithObservableTopicSummary list;

	public TopicSummaryRemovedMessageEvent(final MessageListWithObservableTopicSummary list, final FormattedMqttMessage removed)
	{
		this.list = list;
		this.removed = removed;
	}

	public FormattedMqttMessage getRemoved()
	{
		return removed;
	}

	@Override
	public MessageListWithObservableTopicSummary getList()
	{
		return list;
	}
}
