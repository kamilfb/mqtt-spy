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
package pl.baczkowicz.mqttspy.tasks;

/**
 * Simple interface for updating progress of a task.
 */
public interface ProgressUpdater
{
	/**
	 * Updates the progress of a task.
	 * 
	 * @param current Current progress
	 * @param max Maximum progress value
	 */
	void update(final long current, final long max);
}
