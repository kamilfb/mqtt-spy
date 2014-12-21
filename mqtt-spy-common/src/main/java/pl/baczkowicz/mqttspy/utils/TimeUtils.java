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

import java.text.SimpleDateFormat;

/** 
 * Time and date related utilities.
 */
public class TimeUtils
{	
	public final static String DATE_FORMAT_WITH_MILLISECONDS = "yyyy/MM/dd HH:mm:ss:SSS";
	
	public final static String DATE_FORMAT_WITH_SECONDS = "yyyy/MM/dd HH:mm:ss";
	
	public final static String DATE_FORMAT_NO_TIME = "yyyy/MM/dd";

	public final static SimpleDateFormat DATE_WITH_MILLISECONDS_SDF = new SimpleDateFormat(DATE_FORMAT_WITH_MILLISECONDS);
	
	public final static SimpleDateFormat DATE_WITH_SECONDS_SDF = new SimpleDateFormat(DATE_FORMAT_WITH_SECONDS);
	
	public final static SimpleDateFormat DATE_SDF = new SimpleDateFormat(DATE_FORMAT_NO_TIME);
	
	/**
	 * Returns the monotonic (not system) time in milliseconds. This can be used
	 * for measuring time intervals as this time is not affected by time
	 * adjustment in the OS.
	 * 
	 * @return The monotonic time in milliseconds
	 */
	public static long getMonotonicTime()
	{
		return System.nanoTime() / 1000000;
	}
}
