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

import pl.baczkowicz.mqttspy.scripts.Script;

public class TestCase extends Script
{
	private TestCaseInfo info;

	/**
	 * @return the info
	 */
	public TestCaseInfo getInfo()
	{
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(TestCaseInfo info)
	{
		this.info = info;
	}
	
	
}
