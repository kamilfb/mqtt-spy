/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
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
package pl.baczkowicz.spy.eventbus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KBus implements IKBus
{
	final static Logger logger = LoggerFactory.getLogger(KBus.class);
	
	private final Map<Object, Set<Consumer<? extends KBusEvent>>> subscribers = new HashMap<>();
	
	private final Map<Consumer<? extends KBusEvent>, Object> consumerFilters = new HashMap<>();

	private final Map<Consumer<? extends KBusEvent>, Class<? extends KBusEvent>> consumerTypes = new HashMap<>();
	
	private final Map<Class<? extends KBusEvent>, Collection<Consumer<? extends KBusEvent>>> typeConsumers = new HashMap<>();
	
	private Collection<Consumer<? extends KBusEvent>> getConsumersForType(final KBusEvent event)
	{
		Collection<Consumer<? extends KBusEvent>> matchedConsumers;
		
		// Try to get previously matched consumers
		final Collection<Consumer<? extends KBusEvent>> previouslyMatchedConsumers = typeConsumers.get(event.getClass());
		
		// If none available, try to match
		if (previouslyMatchedConsumers == null)
		{		
			matchedConsumers = matchConsumersForType(event.getClass());
			
			typeConsumers.put(event.getClass(), matchedConsumers);
		}
		else
		{
			matchedConsumers = previouslyMatchedConsumers;
		}
		
		return matchedConsumers;
	}
	
	private Collection<Consumer<? extends KBusEvent>> matchConsumersForType(final Class<? extends KBusEvent> eventType)
	{
		final Collection<Consumer<? extends KBusEvent>> matchedConsumers = new ArrayList<>();
	
		logger.debug("Matching consumers for type {}", eventType);
		
		synchronized (consumerTypes)
		{
			for (final Consumer<? extends KBusEvent> consumer : consumerTypes.keySet())
			{
				final Class<? extends KBusEvent> consumerType = consumerTypes.get(consumer);
				
				// Compares two Classes with each other (because of that couldn't use instanceof or isInstance)
				if (consumerType.isAssignableFrom(eventType))
				{
					matchedConsumers.add(consumer);
				}
			}
		}
		
		logger.debug("Matched {} consumers for type {}: {}", matchedConsumers.size(), eventType, matchedConsumers);
		
		return matchedConsumers;
	}

	@SuppressWarnings("unchecked")
	@Override
	/**
	 * Publishes an event in a synchronous way.
	 */
	public void publish(final KBusEvent event)
	{
		final Collection<Consumer<? extends KBusEvent>> matchedConsumers = getConsumersForType(event);
		
		for (final Consumer<? extends KBusEvent> observer : matchedConsumers)
		{
			// No need to synchronise here, read-only
			final Object filter = consumerFilters.get(observer);
				
			if (filter == null || filter.equals(event.getFilter()))
			{	
				((Consumer<KBusEvent>) observer).accept(event);
			}
		}
	}
	
	private void recalculateExistingMappings()
	{
		// Recalculate all existing eventType to consumer mappings
		for (final Class<? extends KBusEvent> type : typeConsumers.keySet())
		{	
			typeConsumers.put(type, matchConsumersForType(type));
		}
	}

	@Override
	public void subscribe (final Object subscriber, final Consumer<? extends KBusEvent> consumer, 
			final Class<? extends KBusEvent> eventType)
	{
		subscribe(subscriber, consumer, eventType, null);
	}

	@Override
	public void subscribe(final Object subscriber, final Consumer<? extends KBusEvent> consumer, 
			final Class<? extends KBusEvent> eventType, final Object filter)
	{
		synchronized (consumerTypes)
		{
			Set<Consumer<? extends KBusEvent>> consumers = subscribers.get(subscriber);
			
			if (consumers == null)
			{
				consumers = new HashSet<>();				
				subscribers.put(subscriber, consumers);
			}
			consumers.add(consumer);	
			
			consumerFilters.put(consumer, filter);		
			consumerTypes.put(consumer, eventType);		
			
			recalculateExistingMappings();
		}
	}
	

	@Override
	public void unsubscribe(final Object subscriber)
	{
		synchronized (consumerTypes)
		{
			logger.debug("Trying to remove {} from subscribers", subscriber);
			
			final Set<Consumer<? extends KBusEvent>> removed = subscribers.remove(subscriber);
						
			if (removed != null)
			{
				for (final Consumer<? extends KBusEvent> consumer : removed)
				{
					consumerFilters.remove(consumer);		
					consumerTypes.remove(consumer);				
				}
				logger.debug("Removed consumers: {}", removed.size());
			}
			else
			{
				logger.debug("Removed consumers: 0");	
			}
					
			recalculateExistingMappings();
		}
	}
	
	@Override
	public void unsubscribeConsumer(final Object subscriber, final Consumer<? extends KBusEvent> consumer)
	{
		synchronized (consumerTypes)
		{
			logger.debug("Trying to remove {} owned by {}", consumer, subscriber);
			
			// Remove from subscriber's list of consumers
			final Set<Consumer<? extends KBusEvent>> consumers = subscribers.get(subscriber);
			if (consumers != null)
			{
				consumers.remove(consumer);
			}
			
			logger.debug("Removing {} from filters; contains: {}", consumer, consumerFilters.containsKey(consumer));
			consumerFilters.remove(consumer);
						
			logger.debug("Removing {} from types; contains: {}", consumer, consumerTypes.containsKey(consumer));
			consumerTypes.remove(consumer);
			
			recalculateExistingMappings();
		}
	}

	@Override
	public void unsubscribeConsumer(final Object subscriber, final Class<? extends KBusEvent> eventType)
	{
		synchronized (consumerTypes)
		{
			logger.debug("Trying to remove consumer of type {} from {}", eventType, subscriber);
						
			final Collection<Consumer<? extends KBusEvent>> consumers = subscribers.get(subscriber);
			
			Consumer<? extends KBusEvent> foundConsumer = null;
			
			// Find the consumer based on its type
			for (final Consumer<? extends KBusEvent> consumer : consumerTypes.keySet())
			{
				if (consumerTypes.get(consumer).equals(eventType) && consumers.contains(consumer))
				{
					foundConsumer = consumer;
					break;
				}
			}
			
			if (foundConsumer != null)
			{
				unsubscribeConsumer(subscriber, foundConsumer);
			}
		}
	}
}
