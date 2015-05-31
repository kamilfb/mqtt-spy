/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
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
package pl.baczkowicz.mqttspy.testcases;

import java.util.ArrayList;
import java.util.List;

public class TestCaseInfo
{
	private String name;
	
	private List<String> steps;

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the steps
	 */
	public List<String> getSteps()
	{
		if (steps == null)
		{
			steps = new ArrayList<>();
		}
		return steps;
	}

	/**
	 * @param steps the steps to set
	 */
	public void setSteps(List<String> steps)
	{
		this.steps = steps;
	}	
}
