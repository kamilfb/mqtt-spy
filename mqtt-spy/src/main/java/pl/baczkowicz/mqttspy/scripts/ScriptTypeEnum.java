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
package pl.baczkowicz.mqttspy.scripts;

/**
 * This defines the type of script.
 */
public enum ScriptTypeEnum
{
	PUBLICATION("Script folder"), SUBSCRIPTION("Subscription"), BACKGROUND("Predefined")//, SEARCH("Search")
	;
	
	private final String name;

	private ScriptTypeEnum(String s)
	{
		name = s;
	}

	public boolean equalsName(String otherName)
	{
		return (otherName == null) ? false : name.equals(otherName);
	}

	public String toString()
	{
		return name;
	}
}
