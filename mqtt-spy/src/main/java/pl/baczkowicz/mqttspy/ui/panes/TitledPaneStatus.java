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

public class TitledPaneStatus extends PaneStatus
{
	private boolean expanded = false;

	private int displayIndex;
	
	public TitledPaneStatus(final int displayIndex)
	{
		super();
		this.displayIndex = displayIndex;
	}
	
	/**
	 * Gets the expanded flag.
	 * 
	 * @return the expanded
	 */
	public boolean isExpanded()
	{
		return expanded;
	}

	/**
	 * Sets the expanded flag.
	 * 
	 * @param expanded the expanded to set
	 */
	public void setExpanded(final boolean expanded)
	{
		this.expanded = expanded;
	}

	/**
	 * Gets the display index value.
	 * 
	 * @return the displayIndex
	 */
	public int getDisplayIndex()
	{
		return displayIndex;
	}

	/**
	 * Returns the display index value.
	 * 
	 * @param displayIndex the displayIndex to set
	 */
	public void setDisplayIndex(final int displayIndex)
	{
		this.displayIndex = displayIndex;
	}
}
