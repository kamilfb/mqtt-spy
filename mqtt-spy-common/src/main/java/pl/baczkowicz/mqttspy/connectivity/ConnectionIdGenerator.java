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
package pl.baczkowicz.mqttspy.connectivity;

/**
 * Class for generating connection IDs. 
 */
public class ConnectionIdGenerator
{
	private int lastUsedId = 0;
	
	public int getNextAvailableId()
	{
		lastUsedId++;
		return lastUsedId;
	}
	
	public void resetLastUsedId()
	{
		lastUsedId = 0;
	}
}
