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

import pl.baczkowicz.mqttspy.ui.ConnectionController;
import pl.baczkowicz.spy.ui.panes.PaneVisibilityStatus;

public class ShowNewSubscriptionWindowEvent
{
	private final ConnectionController controller;
	
	private final PaneVisibilityStatus status;

	public ShowNewSubscriptionWindowEvent(final ConnectionController controller, final PaneVisibilityStatus status)
	{
		this.controller = controller;
		this.status = status;
	}

	/**
	 * @return the changedSubscription
	 */
	public ConnectionController getConnectionController()
	{
		return controller;
	}

	/**
	 * @return the status
	 */
	public PaneVisibilityStatus getStatus()
	{
		return status;
	}
}
