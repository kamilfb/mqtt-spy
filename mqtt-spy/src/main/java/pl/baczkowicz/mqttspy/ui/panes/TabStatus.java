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
package pl.baczkowicz.mqttspy.ui.panes;

import javafx.scene.control.TabPane;

public class TabStatus extends PaneStatus
{
	private TabPane parentWhenAttached;		

	/**
	 * Gets the parent object.
	 * 
	 * @return the parent
	 */
	public TabPane getParentWhenAttached()
	{
		return parentWhenAttached;
	}

	/**
	 * Sets the parent object.
	 * 
	 * @param parent the parent to set
	 */
	public void setParent(TabPane parent)
	{
		this.parentWhenAttached = parent;
	}
}
