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

import pl.baczkowicz.mqttspy.ui.ConnectionController;
import pl.baczkowicz.mqttspy.ui.connections.ConnectionManager;
import pl.baczkowicz.mqttspy.utils.ThreadingUtils;
import javafx.application.Platform;

/**
 * This class is responsible for updating connection statistics for all connections.
 */
public class ConnectionStatsUpdater implements Runnable
{
	private static final int REFRESH_INTERVAL = 1000;
	
	private ConnectionManager connectionManager;

	public ConnectionStatsUpdater(final ConnectionManager connectionManager)
	{
		this.connectionManager = connectionManager;
	}
	
	@Override
	public void run()
	{
		while (true)
		{
			if (ThreadingUtils.sleep(REFRESH_INTERVAL))			
			{
				break;
			}
		
			Platform.runLater(new Runnable()
			{					
				@Override
				public void run()
				{
					updateConnectionStats();					
				}
			});						
		}
	}

	private void updateConnectionStats()
	{		
		for (final ConnectionController connectionController : connectionManager.getConnectionControllers())
		{
			connectionController.updateConnectionStats();
		}	
	}
}
