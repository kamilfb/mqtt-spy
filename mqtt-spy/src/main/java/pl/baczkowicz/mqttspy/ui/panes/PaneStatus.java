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

	public PaneStatus()
	{
		// Default
	}

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
}
