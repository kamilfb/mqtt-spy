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


public class PaneStatus
{
	private PaneVisibilityStatus visibility = PaneVisibilityStatus.NOT_LOADED;
	
	// TODO: this is only for titled panes, so move out?
	private boolean expanded = false;
	
//	/** The object that encapsulates the pane's controller. */
//	private Pane container;
	

	
//	private int displayIndex;

	public PaneStatus()
	{
		// Default
	}
	
//	public PaneStatus(final int displayIndex)
//	{
//		this.displayIndex = displayIndex;
//	}
	
//	public PaneStatus(final int displayIndex, final Pane container)
//	{
//		this.displayIndex = displayIndex;
//		this.container = container;
//	}

	/**
	 * Gets the visibility status.
	 * 
	 * @return the visibility
	 */
	public PaneVisibilityStatus getVisibility()
	{
		return visibility;
	}

	/**
	 * Sets the visibility status.
	 * 
	 * @param visibility the visibility to set
	 */
	public void setVisibility(final PaneVisibilityStatus visibility)
	{
		this.visibility = visibility;
	}

	/**
	 * Gets the expanded status.
	 * 
	 * @return the expanded
	 */
	public boolean isExpanded()
	{
		return expanded;
	}

	/**
	 * Sets the expanded status.
	 * 
	 * @param expanded the expanded to set
	 */
	public void setExpanded(final boolean expanded)
	{
		this.expanded = expanded;
	}


//	/**
//	 * Gets the display index.
//	 * 
//	 * @return the displayIndex
//	 */
//	public int getDisplayIndex()
//	{
//		return displayIndex;
//	}

//	/**
//	 * Gets the container.
//	 * 
//	 * @return the container
//	 */
//	public Pane getContainer()
//	{
//		return container;
//	}
//
//	/**
//	 * Sets the container.
//	 * 
//	 * @param container the container to set
//	 */
//	public void setContainer(Pane container)
//	{
//		this.container = container;
//	}
	
//	/**
//	 * Sets the display index.
//	 * 
//	 * @param displayIndex the displayIndex to set
//	 */
//	public void setDisplayIndex(int displayIndex)
//	{
//		this.displayIndex = displayIndex;
//	}
}
