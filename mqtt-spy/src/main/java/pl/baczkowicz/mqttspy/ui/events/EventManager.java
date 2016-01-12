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
import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.storage.MessageList;
import pl.baczkowicz.spy.storage.MessageStore;
import pl.baczkowicz.spy.ui.events.observers.MessageAddedObserver;
import pl.baczkowicz.spy.ui.events.observers.MessageFormatChangeObserver;
import pl.baczkowicz.spy.ui.events.observers.MessageIndexChangeObserver;
import pl.baczkowicz.spy.ui.events.observers.MessageIndexIncrementObserver;
import pl.baczkowicz.spy.ui.events.observers.MessageIndexToFirstObserver;
import pl.baczkowicz.spy.ui.events.observers.MessageListChangedObserver;
import pl.baczkowicz.spy.ui.events.observers.MessageRemovedObserver;
import pl.baczkowicz.spy.ui.events.queuable.ui.BrowseReceivedMessageEvent;
import pl.baczkowicz.spy.ui.events.queuable.ui.BrowseRemovedMessageEvent;
import pl.baczkowicz.spy.ui.storage.MessageListWithObservableTopicSummary;

/**
 * This is to be entirely replaced by the KBus.
 * 
 * There are two ways events are distributed around the application. First,
 * using the EventManager - this uses observers, registrations and
 * notifications. Second is by using individual events, that can be buffered on
 * a queue or list - see the pl.baczkowicz.mqttspy.events.queuable package.
 */
@Deprecated
public class EventManager<T extends FormattedMessage>
{
	private final Map<MessageAddedObserver<T>, MessageListWithObservableTopicSummary<T>> messageAddedObservers = new HashMap<>();
	
	private final Map<MessageRemovedObserver<T>, MessageList<T>> messageRemovedObservers = new HashMap<>();
	
	private final Map<MessageListChangedObserver, MessageListWithObservableTopicSummary<T>> messageListChangeObservers = new HashMap<>();

	private final Map<MessageIndexChangeObserver, MessageStore<T>> changeMessageIndexObservers = new HashMap<>();
	
	private final Map<MessageIndexToFirstObserver, MessageStore<T>> displayFirstMessageObservers = new HashMap<>();
	
	private final Map<MessageIndexIncrementObserver, MessageStore<T>> incrementMessageIndexObservers = new HashMap<>();
	
	private final Map<MessageFormatChangeObserver, MessageStore<T>> formatChangeObservers = new HashMap<>();
	
	public void registerMessageAddedObserver(final MessageAddedObserver<T> observer, final MessageListWithObservableTopicSummary<T> filter)
	{
		messageAddedObservers.put(observer, filter);
	}
	
	public void deregisterMessageAddedObserver(final MessageAddedObserver<T> observer)
	{
		messageAddedObservers.remove(observer);
	}
	
	public void registerMessageRemovedObserver(final MessageRemovedObserver<T> observer, final MessageList<T> filter)
	{
		messageRemovedObservers.put(observer, filter);
	}
	
	public void registerMessageListChangedObserver(final MessageListChangedObserver observer, final MessageListWithObservableTopicSummary<T> filter)
	{
		messageListChangeObservers.put(observer, filter);
	}
	
	public void registerChangeMessageIndexObserver(final MessageIndexChangeObserver observer, final MessageStore<T> filter)
	{
		changeMessageIndexObservers.put(observer, filter);
	}
	
	public void registerChangeMessageIndexFirstObserver(final MessageIndexToFirstObserver observer, final MessageStore<T> filter)
	{
		displayFirstMessageObservers.put(observer, filter);
	}
	
	public void registerIncrementMessageIndexObserver(final MessageIndexIncrementObserver observer, final MessageStore<T> filter)
	{
		incrementMessageIndexObservers.put(observer, filter);
	}
	
	public void registerFormatChangeObserver(final MessageFormatChangeObserver observer, final MessageStore<T> filter)
	{
		formatChangeObservers.put(observer, filter);
	}
	
	public void deregisterFormatChangeObserver(MessageFormatChangeObserver observer)
	{
		formatChangeObservers.remove(observer);		
	}
	
	public void notifyMessageAdded(final List<BrowseReceivedMessageEvent<T>> browseEvents, 
			final MessageListWithObservableTopicSummary<T> list)
	{
		for (final MessageAddedObserver<T> observer : messageAddedObservers.keySet())
		{
			final MessageListWithObservableTopicSummary<T> filter = messageAddedObservers.get(observer);
			
			if (filter == null || filter.equals(list))
			{				
				observer.onMessageAdded(browseEvents);
			}			
		}				
	}
	
	public void notifyMessageRemoved(final List<BrowseRemovedMessageEvent<T>> browseEvents, 
			final MessageList<T> messageList)
	{
		for (final MessageRemovedObserver<T> observer : messageRemovedObservers.keySet())
		{
			final MessageList<T> filter = messageRemovedObservers.get(observer);
			
			if (filter == null || filter.equals(messageList))
			{				
				//observer.onMessageRemoved(browseEvent.getMessage(), browseEvent.getMessageIndex());
				observer.onMessageRemoved(browseEvents);
			}			
		}		
	}
	
	public void notifyMessageListChanged(final MessageListWithObservableTopicSummary<T> list)
	{
		for (final MessageListChangedObserver observer : messageListChangeObservers.keySet())
		{
			final MessageListWithObservableTopicSummary<T> filter = messageListChangeObservers.get(observer);
			
			if (filter == null || filter.equals(list))
			{				
				observer.onMessageListChanged();
			}			
		}				
	}
		
	public void notifyFormatChanged(final MessageStore<T> store)
	{
		Platform.runLater(new Runnable()
		{			
			@Override
			public void run()
			{
				for (final MessageFormatChangeObserver observer : formatChangeObservers.keySet())
				{
					final MessageStore<T> filter = formatChangeObservers.get(observer);
					
					if (filter == null || filter.equals(store))
					{				
						observer.onFormatChange();
					}
				}				
			}
		});		
	}
	
	public void navigateToFirst(final MessageStore<T> store)
	{
		for (final MessageIndexToFirstObserver observer : displayFirstMessageObservers.keySet())
		{
			final MessageStore<T> filter = displayFirstMessageObservers.get(observer);

			if (filter == null || filter.equals(store))
			{
				observer.onNavigateToFirst();
			}
		}
	}
	
	public void changeMessageIndex(final MessageStore<T> store, final Object dispatcher, final int index)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				for (final MessageIndexChangeObserver observer : changeMessageIndexObservers.keySet())
				{
					final MessageStore<T> filter = changeMessageIndexObservers.get(observer);

					if ((filter == null || filter.equals(store)) && (dispatcher != observer))
					{
						observer.onMessageIndexChange(index);
					}
				}
			}
		});
	}
	
	public void incrementMessageIndex(final MessageStore<T> store)
	{
		for (final MessageIndexIncrementObserver observer : incrementMessageIndexObservers.keySet())
		{
			final MessageStore<T> filter = incrementMessageIndexObservers.get(observer);

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
}
