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
package pl.baczkowicz.mqttspy.ui.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttSubscription;
import pl.baczkowicz.mqttspy.scripts.IScriptEventManager;
import pl.baczkowicz.mqttspy.scripts.ScriptRunningState;
import pl.baczkowicz.mqttspy.storage.ManagedMessageStoreWithFiltering;
import pl.baczkowicz.mqttspy.storage.MessageList;
import pl.baczkowicz.mqttspy.storage.MessageListWithObservableTopicSummary;
import pl.baczkowicz.mqttspy.storage.MessageStore;
import pl.baczkowicz.mqttspy.ui.events.observers.ClearTabObserver;
import pl.baczkowicz.mqttspy.ui.events.observers.ConnectionStatusChangeObserver;
import pl.baczkowicz.mqttspy.ui.events.observers.MessageAddedObserver;
import pl.baczkowicz.mqttspy.ui.events.observers.MessageFormatChangeObserver;
import pl.baczkowicz.mqttspy.ui.events.observers.MessageIndexChangeObserver;
import pl.baczkowicz.mqttspy.ui.events.observers.MessageIndexIncrementObserver;
import pl.baczkowicz.mqttspy.ui.events.observers.MessageIndexToFirstObserver;
import pl.baczkowicz.mqttspy.ui.events.observers.MessageListChangedObserver;
import pl.baczkowicz.mqttspy.ui.events.observers.MessageRemovedObserver;
import pl.baczkowicz.mqttspy.ui.events.observers.ScriptListChangeObserver;
import pl.baczkowicz.mqttspy.ui.events.observers.ScriptStateChangeObserver;
import pl.baczkowicz.mqttspy.ui.events.observers.SubscriptionStatusChangeObserver;
import pl.baczkowicz.mqttspy.ui.events.queuable.ui.BrowseReceivedMessageEvent;
import pl.baczkowicz.mqttspy.ui.events.queuable.ui.BrowseRemovedMessageEvent;

/**
 * There are two ways events are distributed around the application. First,
 * using the EventManager - this uses observers, registrations and
 * notifications. Second is by using individual events, that can be buffered on
 * a queue or list - see the pl.baczkowicz.mqttspy.events.queuable package.
 */
public class EventManager implements IScriptEventManager
{
	private final Map<MessageAddedObserver, MessageListWithObservableTopicSummary> messageAddedObservers = new HashMap<>();
	
	private final Map<MessageRemovedObserver, MessageList> messageRemovedObservers = new HashMap<>();
	
	private final Map<MessageListChangedObserver, MessageListWithObservableTopicSummary> messageListChangeObservers = new HashMap<>();
	
	private final Map<ConnectionStatusChangeObserver, MqttAsyncConnection> connectionStatusChangeObservers = new HashMap<>();
	
	private final Map<SubscriptionStatusChangeObserver, MqttSubscription> subscriptionStatusChangeObservers = new HashMap<>();
	
	private final Map<ClearTabObserver, ManagedMessageStoreWithFiltering> clearTabObservers = new HashMap<>();

	private final Map<MessageIndexChangeObserver, MessageStore> changeMessageIndexObservers = new HashMap<>();
	
	private final Map<MessageIndexToFirstObserver, MessageStore> displayFirstMessageObservers = new HashMap<>();
	
	private final Map<MessageIndexIncrementObserver, MessageStore> incrementMessageIndexObservers = new HashMap<>();
	
	private final Map<MessageFormatChangeObserver, MessageStore> formatChangeObservers = new HashMap<>();
	
	private final Map<ScriptStateChangeObserver, String> scriptStateChangeObservers = new HashMap<>();
	
	private final Map<ScriptListChangeObserver, MqttAsyncConnection> scriptListChangeObservers = new HashMap<>();
	
	/**
	 * 
	 * Registers an observer for connection status changes.
	 * 
	 * @param observer The observer to register
	 * @param filter Null for all, or value to match
	 */
	public void registerConnectionStatusObserver(final ConnectionStatusChangeObserver observer, final MqttAsyncConnection filter)
	{
		connectionStatusChangeObservers.put(observer, filter);
	}
	
	public void registerMessageAddedObserver(final MessageAddedObserver observer, final MessageListWithObservableTopicSummary filter)
	{
		messageAddedObservers.put(observer, filter);
	}
	
	public void deregisterMessageAddedObserver(final MessageAddedObserver observer)
	{
		messageAddedObservers.remove(observer);
	}
	
	public void registerMessageRemovedObserver(final MessageRemovedObserver observer, final MessageList filter)
	{
		messageRemovedObservers.put(observer, filter);
	}
	
	public void registerMessageListChangedObserver(final MessageListChangedObserver observer, final MessageListWithObservableTopicSummary filter)
	{
		messageListChangeObservers.put(observer, filter);
	}
	
	public void registerSubscriptionStatusObserver(final SubscriptionStatusChangeObserver observer, final MqttSubscription filter)
	{
		subscriptionStatusChangeObservers.put(observer, filter);
	}
	
	public void deregisterConnectionStatusObserver(final ConnectionStatusChangeObserver observer)
	{
		connectionStatusChangeObservers.remove(observer);
	}
	
	public void registerClearTabObserver(final ClearTabObserver observer, final ManagedMessageStoreWithFiltering filter)
	{
		clearTabObservers.put(observer, filter);
	}
	
	public void registerChangeMessageIndexObserver(final MessageIndexChangeObserver observer, final MessageStore filter)
	{
		changeMessageIndexObservers.put(observer, filter);
	}
	
	public void registerChangeMessageIndexFirstObserver(final MessageIndexToFirstObserver observer, final MessageStore filter)
	{
		displayFirstMessageObservers.put(observer, filter);
	}
	
	public void registerIncrementMessageIndexObserver(final MessageIndexIncrementObserver observer, final MessageStore filter)
	{
		incrementMessageIndexObservers.put(observer, filter);
	}
	
	public void registerFormatChangeObserver(final MessageFormatChangeObserver observer, final MessageStore filter)
	{
		formatChangeObservers.put(observer, filter);
	}
	
	public void deregisterFormatChangeObserver(MessageFormatChangeObserver observer)
	{
		formatChangeObservers.remove(observer);		
	}
	
	public void registerScriptStateChangeObserver(final ScriptStateChangeObserver observer, final String filter)
	{
		scriptStateChangeObservers.put(observer, filter);
	}
	
	public void registerScriptListChangeObserver(final ScriptListChangeObserver observer, final MqttAsyncConnection filter)
	{
		scriptListChangeObservers.put(observer, filter);
	}
	
	public void notifyMessageAdded(final List<BrowseReceivedMessageEvent> browseEvents, final MessageListWithObservableTopicSummary list)
	{
		for (final MessageAddedObserver observer : messageAddedObservers.keySet())
		{
			final MessageListWithObservableTopicSummary filter = messageAddedObservers.get(observer);
			
			if (filter == null || filter.equals(list))
			{				
				observer.onMessageAdded(browseEvents);
			}			
		}				
	}
	
	public void notifyMessageRemoved(final List<BrowseRemovedMessageEvent> browseEvents, final MessageList messageList)
	{
		for (final MessageRemovedObserver observer : messageRemovedObservers.keySet())
		{
			final MessageList filter = messageRemovedObservers.get(observer);
			
			if (filter == null || filter.equals(messageList))
			{				
				//observer.onMessageRemoved(browseEvent.getMessage(), browseEvent.getMessageIndex());
				observer.onMessageRemoved(browseEvents);
			}			
		}		
	}
	
	public void notifyMessageListChanged(final MessageListWithObservableTopicSummary list)
	{
		for (final MessageListChangedObserver observer : messageListChangeObservers.keySet())
		{
			final MessageListWithObservableTopicSummary filter = messageListChangeObservers.get(observer);
			
			if (filter == null || filter.equals(list))
			{				
				observer.onMessageListChanged();
			}			
		}				
	}
	
	public void notifyConnectionStatusChanged(final MqttAsyncConnection changedConnection)
	{
		Platform.runLater(new Runnable()
		{			
			@Override
			public void run()
			{
				for (final ConnectionStatusChangeObserver observer : connectionStatusChangeObservers.keySet())
				{
					final MqttAsyncConnection filter = connectionStatusChangeObservers.get(observer);
					
					if (filter == null || filter.equals(changedConnection))
					{				
						observer.onConnectionStatusChanged(changedConnection);
					}
				}				
			}
		});		
	}
	
	public void notifySubscriptionStatusChanged(final MqttSubscription changedSubscription)
	{
		Platform.runLater(new Runnable()
		{			
			@Override
			public void run()
			{
				for (final SubscriptionStatusChangeObserver observer : subscriptionStatusChangeObservers.keySet())
				{
					final MqttSubscription filter = subscriptionStatusChangeObservers.get(observer);
					
					if (filter == null || filter.equals(changedSubscription))
					{				
						observer.onSubscriptionStatusChanged(changedSubscription);
					}
				}				
			}
		});		
		
	}
		
	public void notifyFormatChanged(final MessageStore store)
	{
		Platform.runLater(new Runnable()
		{			
			@Override
			public void run()
			{
				for (final MessageFormatChangeObserver observer : formatChangeObservers.keySet())
				{
					final MessageStore filter = formatChangeObservers.get(observer);
					
					if (filter == null || filter.equals(store))
					{				
						observer.onFormatChange();
					}
				}				
			}
		});		
	}
	
	public void navigateToFirst(final MessageStore store)
	{
		for (final MessageIndexToFirstObserver observer : displayFirstMessageObservers.keySet())
		{
			final MessageStore filter = displayFirstMessageObservers.get(observer);

			if (filter == null || filter.equals(store))
			{
				observer.onNavigateToFirst();
			}
		}
	}
	
	public void changeMessageIndex(final MessageStore store, final Object dispatcher, final int index)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				for (final MessageIndexChangeObserver observer : changeMessageIndexObservers.keySet())
				{
					final MessageStore filter = changeMessageIndexObservers.get(observer);

					if ((filter == null || filter.equals(store)) && (dispatcher != observer))
					{
						observer.onMessageIndexChange(index);
					}
				}
			}
		});
	}
	
	public void incrementMessageIndex(final MessageStore store)
	{
		for (final MessageIndexIncrementObserver observer : incrementMessageIndexObservers.keySet())
		{
			final MessageStore filter = incrementMessageIndexObservers.get(observer);

			if (filter == null || filter.equals(store))
			{
				observer.onMessageIndexIncrement(1);
			}
		}
	}

	public void notifyConfigurationFileWriteFailure()
	{
		// No action
	}

	public void notifyConfigurationFileCopyFailure()
	{
		// No action		
	}

	public void notifyConfigurationFileReadFailure()
	{
		// No action
	}

	public void notifyClearHistory(final ManagedMessageStoreWithFiltering store)
	{
		for (final ClearTabObserver observer : clearTabObservers.keySet())
		{
			final ManagedMessageStoreWithFiltering filter = clearTabObservers.get(observer);
			
			if (filter == null || filter.equals(store))
			{
				observer.onClearTab(store);
			}
		}
	}

	public void notifyScriptStateChange(final String scriptName, final ScriptRunningState state)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				for (final ScriptStateChangeObserver observer : scriptStateChangeObservers.keySet())
				{
					final String filter = scriptStateChangeObservers.get(observer);

					if (filter == null || filter.equals(scriptName))
					{
						observer.onScriptStateChange(scriptName, state);
					}
				}
			}
		});		
	}

	public void notifyScriptListChange(final MqttAsyncConnection connection)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				for (final ScriptListChangeObserver observer : scriptListChangeObservers.keySet())
				{
					final MqttAsyncConnection filter = scriptListChangeObservers.get(observer);

					if (filter == null || filter.equals(connection))
					{
						observer.onScriptListChange();
					}
				}
			}
		});
	}
}
