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

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for catching any uncaught exceptions.
 */
public class MqttSpyUncaughtExceptionHandler implements UncaughtExceptionHandler
{
	private final static Logger logger = LoggerFactory.getLogger(MqttSpyUncaughtExceptionHandler.class);
	
	@Override
	public void uncaughtException(Thread t, Throwable e)
	{
		logger.error("Thread " + t + " failed with " + e, e);
	}
}
