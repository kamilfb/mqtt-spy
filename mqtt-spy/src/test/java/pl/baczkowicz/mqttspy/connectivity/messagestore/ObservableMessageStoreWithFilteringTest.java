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
package pl.baczkowicz.mqttspy.connectivity.messagestore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Before;
import org.junit.Test;

import pl.baczkowicz.mqttspy.events.queuable.ui.MqttSpyUIEvent;
import pl.baczkowicz.mqttspy.storage.ManagedMessageStoreWithFiltering;
import pl.baczkowicz.mqttspy.storage.FormattedMqttMessage;

public class ObservableMessageStoreWithFilteringTest
{
	/** Class under test. */
	private ManagedMessageStoreWithFiltering store;
	
	@Before
	public void setUp() throws Exception
	{
		store = new ManagedMessageStoreWithFiltering("test", 5, 5, 5, new LinkedBlockingQueue<MqttSpyUIEvent>(), null, 0);
	}

	@Test
	public final void testStoreMaxSize()
	{
		final FormattedMqttMessage message = new FormattedMqttMessage(1, "t1", new MqttMessage("test1".getBytes()), null);
		
		store.messageReceived(message);
				
		assertEquals(1, store.getMessages().size());
		
		store.messageReceived(message);
		store.messageReceived(message);
		store.messageReceived(message);
		store.messageReceived(message);
		
		assertEquals(5, store.getMessages().size());
		
		store.messageReceived(message);
		
		assertEquals(5, store.getMessages().size());
	}

	@Test
	public final void testDefaultActiveFilters()
	{
		testStoreMaxSize();
		assertTrue(store.getFilteredMessageStore().getBrowsedTopics().contains("t1"));
		
		final FormattedMqttMessage message = new FormattedMqttMessage(2, "t2", new MqttMessage("test2".getBytes()), null);
		store.messageReceived(message);
		assertTrue(store.getFilteredMessageStore().getBrowsedTopics().contains("t2"));
	}
	
	@Test
	public final void testActiveFiltersWhenNotAllEnabled()	
	{
		testDefaultActiveFilters();
		store.getFilteredMessageStore().updateTopicFilter("t2", false);
		
		final FormattedMqttMessage message = new FormattedMqttMessage(3, "t3", new MqttMessage("test3".getBytes()), null);
		store.messageReceived(message);
		assertFalse(store.getFilteredMessageStore().getBrowsedTopics().contains("t3"));
	}
}
