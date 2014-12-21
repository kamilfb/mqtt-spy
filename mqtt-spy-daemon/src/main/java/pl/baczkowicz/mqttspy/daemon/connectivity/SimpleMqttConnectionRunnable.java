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
package pl.baczkowicz.mqttspy.daemon.connectivity;

import pl.baczkowicz.mqttspy.common.generated.ReconnectionSettings;
import pl.baczkowicz.mqttspy.common.generated.ScriptDetails;
import pl.baczkowicz.mqttspy.common.generated.SubscriptionDetails;
import pl.baczkowicz.mqttspy.connectivity.SimpleMqttConnection;
import pl.baczkowicz.mqttspy.daemon.configuration.generated.DaemonMqttConnectionDetails;
import pl.baczkowicz.mqttspy.scripts.ScriptManager;
import pl.baczkowicz.mqttspy.utils.ConnectionUtils;
import pl.baczkowicz.mqttspy.utils.ThreadingUtils;

/**
 * This runnable is responsible for establishing a connection.
 */
public class SimpleMqttConnectionRunnable implements Runnable
{
	/** The connection to be used. */	
	private final SimpleMqttConnection connection;
	
	/** The connection settings to be used. */
	private final DaemonMqttConnectionDetails connectionSettings;

	/** The script manager - used for subscription scripts. */
	private final ScriptManager scriptManager;

	/**
	 * Creates a ConnectionRunnable.
	 * 
	 * @param scriptManager The script manager - used for subscription scripts
	 * @param connection The connection to be used
	 * @param connectionSettings The connection settings to be used
	 */
	public SimpleMqttConnectionRunnable(final ScriptManager scriptManager, final SimpleMqttConnection connection, final DaemonMqttConnectionDetails connectionSettings)
	{
		this.connection = connection;
		this.connectionSettings = connectionSettings;
		this.scriptManager = scriptManager;
	}
	
	public void run()
	{
		Thread.currentThread().setName("Connection " + connection.getMqttConnectionDetails().getName());
		ThreadingUtils.logStarting();
		
		// Get reconnection settings
		final ReconnectionSettings reconnectionSettings = connection.getMqttConnectionDetails().getReconnectionSettings();
		
		final boolean neverStarted = connection.getLastConnectionAttemptTimestamp() == ConnectionUtils.NEVER_STARTED;
		
		// If successfully connected, and re-subscription is configured
		if (connection.connect() 
				&& (neverStarted || (reconnectionSettings != null && reconnectionSettings.isResubscribe())))
		{
			// Subscribe to all configured subscriptions
			for (final SubscriptionDetails subscription : connectionSettings.getSubscription())
			{	
				if (neverStarted && subscription.getScriptFile() != null)
				{
					scriptManager.addScript(new ScriptDetails(false, subscription.getScriptFile()));
				}
					
				connection.subscribe(subscription.getTopic(), subscription.getQos());							
			}
		}
		
		ThreadingUtils.logEnding();
	}				
}
