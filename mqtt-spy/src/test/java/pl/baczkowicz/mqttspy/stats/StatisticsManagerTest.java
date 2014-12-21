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
package pl.baczkowicz.mqttspy.stats;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class StatisticsManagerTest
{
	private StatisticsManager statisticsManager;
	
	@Before
	public void setUp() throws Exception
	{
		statisticsManager = new StatisticsManager();
		statisticsManager.loadStats();
	}

	@Test
	public final void testAverage()
	{
		statisticsManager.messagePublished(3, "/test");
		statisticsManager.messagePublished(3, "/test");
		StatisticsManager.nextInterval();
		assertEquals(0.4, StatisticsManager.getMessagesPublished(3, 5).overallCount, 0);
		
		statisticsManager.messagePublished(3, "/test");
		statisticsManager.messagePublished(3, "/test");
		statisticsManager.messagePublished(3, "/test");
		statisticsManager.messagePublished(3, "/test");
		statisticsManager.messagePublished(3, "/test");
		StatisticsManager.nextInterval();
		assertEquals(1.4, StatisticsManager.getMessagesPublished(3, 5).overallCount, 0);
		
		StatisticsManager.nextInterval();
		StatisticsManager.nextInterval();
		StatisticsManager.nextInterval();
		assertEquals(1.4, StatisticsManager.getMessagesPublished(3, 5).overallCount, 0);
		
		StatisticsManager.nextInterval();
		assertEquals(1.0, StatisticsManager.getMessagesPublished(3, 5).overallCount, 0);
		
		StatisticsManager.nextInterval();
		assertEquals(0, StatisticsManager.getMessagesPublished(3, 5).overallCount, 0);
	}

}
