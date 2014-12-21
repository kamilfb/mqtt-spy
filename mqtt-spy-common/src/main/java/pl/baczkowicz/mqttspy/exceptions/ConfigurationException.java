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
package pl.baczkowicz.mqttspy.exceptions;

/**
 * Represents an exception during configuration loading.
 */
public class ConfigurationException extends XMLException
{
	/** serialVersionUID. */
	private static final long serialVersionUID = 5880158442069517297L;

	public ConfigurationException(String error)
	{
		super(error);
	}
	
	public ConfigurationException(String error, Throwable e)
	{
		super(error, e);
	}
}
