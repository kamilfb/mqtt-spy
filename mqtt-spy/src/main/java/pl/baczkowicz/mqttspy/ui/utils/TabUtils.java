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
package pl.baczkowicz.mqttspy.ui.utils;

import javafx.scene.control.Tab;

import com.sun.javafx.scene.control.behavior.TabPaneBehavior;
import com.sun.javafx.scene.control.skin.TabPaneSkin;

/**
 * Tab pane utilities.
 */
public class TabUtils
{
	/**
	 * Requests the given tab to be closed.
	 * 
	 * @param tab The tab to be closed
	 */
	public static void requestClose(final Tab tab)
	{
		TabPaneBehavior behavior = getBehavior(tab);
		if (behavior.canCloseTab(tab))
		{
			behavior.closeTab(tab);
		}
	}

	/**
	 * Gets the behavior object for the given tab.
	 * 
	 * @param tab The tab for which to get the behaviour
	 *  
	 * @return TabPaneBehavior
	 */
	private static TabPaneBehavior getBehavior(final Tab tab)
	{
		return ((TabPaneSkin) tab.getTabPane().getSkin()).getBehavior();
	}
}
