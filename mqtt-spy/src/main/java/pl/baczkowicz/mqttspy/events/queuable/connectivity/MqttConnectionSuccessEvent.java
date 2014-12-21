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
package pl.baczkowicz.mqttspy.events.queuable.connectivity;

import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.events.queuable.MqttSpyEvent;

public class MqttConnectionSuccessEvent implements MqttSpyEvent
{
	private final MqttAsyncConnection connection;
	
	public MqttConnectionSuccessEvent(final MqttAsyncConnection connection)
	{
		this.connection = connection;
	}

	public MqttAsyncConnection getConnection()
	{
		return connection;
	}
}
