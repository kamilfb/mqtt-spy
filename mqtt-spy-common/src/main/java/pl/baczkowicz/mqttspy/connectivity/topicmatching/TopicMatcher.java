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
package pl.baczkowicz.mqttspy.connectivity.topicmatching;

import java.util.ArrayList;
import java.util.List;

import org.dna.mqtt.moquette.messaging.spi.impl.subscriptions.Subscription;
import org.dna.mqtt.moquette.messaging.spi.impl.subscriptions.SubscriptionsStore;
import org.dna.mqtt.moquette.proto.messages.AbstractMessage.QOSType;

/**
 * This class is responsible for matching topics against subscriptions, and
 * figure out which subscription the message has been received for. It uses
 * moquette's SubscriptionStore to achieve that.
 */
public class TopicMatcher
{
	/** This dummy client ID is used for storing subscriptions. */
	private static final String DUMMY_CLIENT_ID = "mqttspy";  
	
	/** Subscription store - used to matching topics against subscriptions - from moquette. */
	private SubscriptionsStore subscriptionsStore;
	
	/**
	 * Creates the topic matcher.
	 */
	public TopicMatcher()
	{
		// Manage subscriptions, based on moquette
		subscriptionsStore = new SubscriptionsStore();
		subscriptionsStore.init(new MapBasedSubscriptionStore());
	}
	
	/**
	 * Returns matching subscriptions for the given topic.
	 * 
	 * @param topic The topic to get active subscriptions for
	 * 
	 * @return List of subscription topics matching the given topic
	 */
	public List<String> getMatchingSubscriptions(final String topic)
	{		
		// Check matching subscription, based on moquette
		final List<Subscription> matchingSubscriptions = subscriptionsStore.matches(topic);
		
		final List<String> matchingSubscriptionTopics = new ArrayList<String>();
		
		// For all found subscriptions
		for (final Subscription matchingSubscription : matchingSubscriptions)
		{						
			matchingSubscriptionTopics.add(matchingSubscription.getTopic());
		}		

		return matchingSubscriptionTopics;
	}

	/**
	 * Adds the given topic to the subscription store - used for topic to subscription matching.
	 *  
	 * @param topic Topic to add
	 */
	public void addSubscriptionToStore(final String topic)
	{
		// Store the subscription topic for further matching
		subscriptionsStore.add(new Subscription(DUMMY_CLIENT_ID, topic, QOSType.MOST_ONE, true));
	}
	
	/**
	 * Removes the given topic from the subscription store - used for topic to subscription matching.
	 *  
	 * @param topic Topic to remove
	 */
	public void removeSubscriptionFromStore(final String topic)
	{
		subscriptionsStore.removeSubscription(topic, DUMMY_CLIENT_ID);
	}
}
