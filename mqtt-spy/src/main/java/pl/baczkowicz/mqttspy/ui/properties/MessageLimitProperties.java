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
package pl.baczkowicz.mqttspy.ui.properties;

public class MessageLimitProperties
{
	private final int messageLimit;
	
	private final long timeLimit;
	
	private final String name;
	
	public MessageLimitProperties(final String name, final int messageLimit, final long timeLimit)	
	{
		this.name = name;
		this.messageLimit = messageLimit;
		this.timeLimit = timeLimit;
	}

	/**
	 * Gets the message limit.
	 * 
	 * @return the messageLimit
	 */
	public int getMessageLimit()
	{
		return messageLimit;
	}

	/**
	 * Gets the time limit.
	 * 
	 * @return the timeLimit
	 */
	public long getTimeLimit()
	{
		return timeLimit;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
}
