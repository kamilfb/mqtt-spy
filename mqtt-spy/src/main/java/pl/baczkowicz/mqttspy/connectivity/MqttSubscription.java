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
package pl.baczkowicz.mqttspy.connectivity;

import javafx.scene.paint.Color;
import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.UiProperties;
import pl.baczkowicz.mqttspy.scripts.FormattingManager;
import pl.baczkowicz.mqttspy.storage.ManagedMessageStoreWithFiltering;
import pl.baczkowicz.mqttspy.ui.SubscriptionController;
import pl.baczkowicz.mqttspy.ui.events.EventManager;
import pl.baczkowicz.mqttspy.ui.events.queuable.EventQueueManager;

public class MqttSubscription extends BaseMqttSubscription
{
	private Color color;
	
	private SubscriptionController subscriptionController;

	private MqttAsyncConnection connection;
	
	private final ManagedMessageStoreWithFiltering store;

	private EventManager eventManager;

	public MqttSubscription(final String topic, final Integer qos, final Color color, 
			final int minMessagesPerTopic, final int preferredStoreSize, final EventQueueManager uiEventQueue, final EventManager eventManager, 
			final ConfigurationManager configurationManager, final FormattingManager formattingManager)
	{
		super(topic, qos, minMessagesPerTopic, preferredStoreSize);
		
		// Max size is double the preferred size
		store = new ManagedMessageStoreWithFiltering(topic, minMessagesPerTopic, 
				preferredStoreSize, preferredStoreSize * 2, 
				uiEventQueue, eventManager, formattingManager,
				UiProperties.getSummaryMaxPayloadLength(configurationManager));
		
		this.color = color;
		this.eventManager = eventManager;
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	@Override
	public void setActive(final boolean active)
	{
		super.setActive(active);
		subscriptionStatusChanged();
	}

	public void subscriptionStatusChanged()
	{
		eventManager.notifySubscriptionStatusChanged(this);
	}

	public void setSubscriptionController(final SubscriptionController subscriptionController)
	{
		this.subscriptionController = subscriptionController;		
	}
	
	public SubscriptionController getSubscriptionController()
	{
		return subscriptionController;
	}

	public void setConnection(final MqttAsyncConnection connection)
	{
		this.connection = connection;		
	}
	
	public MqttAsyncConnection getConnection()	
	{
		return connection;
	}

	@Override
	public ManagedMessageStoreWithFiltering getStore()
	{
		return store;
	}
}
