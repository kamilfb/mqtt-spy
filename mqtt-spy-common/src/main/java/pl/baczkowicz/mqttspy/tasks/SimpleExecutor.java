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

import java.util.concurrent.Executor;

/**
 * Simple runnable executor.
 */
public class SimpleExecutor implements Executor
{
	/**
	 * Starts a new thread for the given runnable.
	 */
	public void execute(final Runnable runnable)
	{
		new Thread(runnable).start();
	}	 
}
