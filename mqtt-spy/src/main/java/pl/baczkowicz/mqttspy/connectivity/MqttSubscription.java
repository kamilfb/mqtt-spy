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

import java.util.Queue;

import javafx.scene.paint.Color;
import pl.baczkowicz.mqttspy.common.generated.SubscriptionDetails;
import pl.baczkowicz.mqttspy.events.EventManager;
import pl.baczkowicz.mqttspy.events.queuable.ui.MqttSpyUIEvent;
import pl.baczkowicz.mqttspy.storage.ManagedMessageStoreWithFiltering;
import pl.baczkowicz.mqttspy.ui.SubscriptionController;

// TODO: split the logic from UI
public class MqttSubscription extends ManagedMessageStoreWithFiltering
{
	private int id;
	
	private String topic;

	private Integer qos;

	private Color color;

	private boolean subscribing;
	
	private boolean subscriptionRequested;

	private boolean active;
	
	private SubscriptionController subscriptionController;

	private MqttAsyncConnection connection;
	
	private SubscriptionDetails details;

	public MqttSubscription(final String topic, final Integer qos, final Color color, 
			final int minMessagesPerTopic, final int preferredStoreSize, final Queue<MqttSpyUIEvent> uiEventQueue, final EventManager eventManager)
	{
		// Max size is double the preferred size
		super(topic, minMessagesPerTopic, preferredStoreSize, preferredStoreSize * 2, uiEventQueue, eventManager);
		
		this.topic = topic;
		this.qos = qos;
		this.color = color;
		this.active = false;
		this.subscriptionRequested = false;
	}

	public String getTopic()
	{
		return topic;
	}

	public void setTopic(String topic)
	{
		this.topic = topic;
	}

	public Integer getQos()
	{
		return qos;
	}

	public void setQos(Integer qos)
	{
		this.qos = qos;
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(final boolean active)
	{
		this.active = active;
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

	public void setConnection(MqttAsyncConnection connection)
	{
		this.connection = connection;		
	}
	
	public MqttAsyncConnection getConnection()	
	{
		return connection;
	}

	public int getId()
	{
		return id;
	}

	public void setId(final int id)
	{
		this.id = id;		
	}

	public boolean isSubscribing()
	{
		return subscribing;
	}
	
	public void setSubscribing(final boolean value)
	{
		subscribing = value;
	}

	public boolean getSubscriptionRequested()
	{
		return subscriptionRequested;
	}

	public void setSubscriptionRequested(final boolean subscriptionRequested)
	{
		this.subscriptionRequested = subscriptionRequested;
	}

	public SubscriptionDetails getDetails()
	{
		return details;
	}

	public void setDetails(SubscriptionDetails details)
	{
		this.details = details;
	}
}
