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

package pl.baczkowicz.mqttspy.ui.events;

import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import javafx.stage.Window;

public class ShowEditConnectionsWindowEvent
{
	private final Window parent;
	
	private final boolean createNew;

	private final MqttAsyncConnection connection;

	public ShowEditConnectionsWindowEvent(final Window parent, final boolean createNew, final MqttAsyncConnection connection)
	{
		this.parent = parent;
		this.createNew = createNew;
		this.connection = connection;
	}

	/**
	 * @return the changedSubscription
	 */
	public Window getParent()
	{
		return parent;
	}

	/**
	 * @return the createNew
	 */
	public boolean isCreateNew()
	{
		return createNew;
	}

	/**
	 * @return the connection
	 */
	public MqttAsyncConnection getConnection()
	{
		return connection;
	}
}
