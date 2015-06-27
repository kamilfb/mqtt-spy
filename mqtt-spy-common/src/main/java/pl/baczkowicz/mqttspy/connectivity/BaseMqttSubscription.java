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

import pl.baczkowicz.mqttspy.common.generated.SubscriptionDetails;
import pl.baczkowicz.mqttspy.storage.BasicMessageStore;
import pl.baczkowicz.mqttspy.storage.MessageList;

public class BaseMqttSubscription
{
	private int id;
	
	private String topic;

	private Integer qos;

	private boolean subscribing;
	
	private boolean subscriptionRequested;

	private boolean active;
	
	private BaseMqttConnection connection;
	
	private SubscriptionDetails details;
	
	private final BasicMessageStore store;

	public BaseMqttSubscription(final String topic, final Integer qos, 
			final int minMessagesPerTopic, final int preferredStoreSize)
	{
		// Max size is double the preferred size
		//this.store = new BasicMessageStore(topic, minMessagesPerTopic, preferredStoreSize, preferredStoreSize * 2);
		this.store = new BasicMessageStore(new MessageList(minMessagesPerTopic, preferredStoreSize, topic));
		
		this.topic = topic;
		this.qos = qos;
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

	public boolean isActive()
	{
		return active;
	}

	public void setActive(final boolean active)
	{
		this.active = active;
	}

	public void setConnection(final BaseMqttConnection connection)
	{
		this.connection = connection;		
	}
	
	public BaseMqttConnection getConnection()	
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

	public BasicMessageStore getStore()
	{
		return store;
	}
}
