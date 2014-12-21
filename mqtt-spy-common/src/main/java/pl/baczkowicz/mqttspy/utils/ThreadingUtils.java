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
package pl.baczkowicz.mqttspy.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.utils.ThreadingUtils;

/** 
 * Threading related utilities.
 */
public class ThreadingUtils
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(ThreadingUtils.class);
	
	/** Log entry for starting a thread. */
	private static final String STARTING_THREAD = "Starting %s thread...";
	
	/** Log entry for stopping a thread. */
	private static final String ENDING_THREAD = "Ending %s thread...";	 
	
	/**
	 * Logs the fact of starting a thread.
	 */
	public static void logStarting()
	{
		if (logger.isTraceEnabled())
		{
			logger.trace(String.format(ThreadingUtils.STARTING_THREAD, Thread.currentThread().getName()));
		}
	}
	
	/**
	 * Logs the fact of finishing a thread.
	 */
	public static void logEnding()
	{
		if (logger.isTraceEnabled())
		{
			logger.trace(String.format(ThreadingUtils.ENDING_THREAD, Thread.currentThread().getName()));
		}
	}
	
	/**
	 * Performs a sleep on the current thread.
	 * 
	 * @param milliseconds How log to sleep for in milliseconds
	 * 
	 * @return True if interrupted
	 */
	public static boolean sleep(final long milliseconds)	
	{
		try
		{
			Thread.sleep(milliseconds);
			return false;
		}
		catch (InterruptedException e)
		{
			logger.warn("Thread {} has been interrupted", Thread.currentThread().getName(), e);
			return true;
		}
	}
}
