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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.Executor;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.utils.ThreadingUtils;

/**
 * This runnable implementation is responsible for running a script in its own thread.
 */
public class ScriptRunner implements Runnable
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(ScriptRunner.class);
	
	/** The associated script. */
	private final Script script;

	/** The Event Manager. */
	private IScriptEventManager eventManager;

	/** The executor. */
	private Executor executor;
	
	/** The thread running the script. */
	private Thread runningThread;

	/**
	 * Creates a ScriptRunner.
	 * 
	 * @param eventManager The Event Manager to be used
	 * @param script The associated script
	 * @param executor The executor to be used
	 */
	public ScriptRunner(final IScriptEventManager eventManager, final Script script, final Executor executor)
	{
		this.script = script;
		this.eventManager = eventManager;
		this.executor = executor;
	}
	
	/**
	 * Runs once or in a loop if repeat flag is set.
	 */
	public void run()
	{
		Thread.currentThread().setName("Script " + script.getName());
		ThreadingUtils.logStarting();
		
		script.getPublicationScriptIO().touch();
		runningThread = Thread.currentThread();
		
		boolean firstRun = true;

		while (firstRun || script.isRepeat())
		{
			firstRun = false;
			
			changeState(ScriptRunningState.RUNNING);
			new Thread(new ScriptHealthDetector(eventManager, script, executor)).start();		
			
			try
			{
				runScript();
			}
			catch (Exception e)
			{
				changeState(ScriptRunningState.FAILED);
				logger.error("Script execution exception", e);
				break;
			}		
			
			if (script.isRepeat())
			{
				logger.debug("Re-running script {}", script.getName());
			}
		}
		
		script.getPublicationScriptIO().stop();
		
		ThreadingUtils.logEnding();
	}
	
	/**
	 * Runs the script and checks the returned value.
	 * 
	 * @throws FileNotFoundException Thrown when the script file couldn't be found
	 * @throws ScriptException Thrown when a script executor error occurs
	 */
	private void runScript() throws FileNotFoundException, ScriptException
	{
		final Object returnValue = script.getScriptEngine().eval(new FileReader(script.getScriptFile()));
		logger.debug("Script {} returned with value {}", script.getName(), returnValue);
		
		// If nothing returned, assume all good
		if (returnValue == null)
		{
			changeState(ScriptRunningState.FINISHED);
		}
		// If boolean returned, check if OK
		else if (returnValue instanceof Boolean)
		{
			if ((Boolean) returnValue)
			{
				changeState(ScriptRunningState.FINISHED);
			}
			else
			{
				changeState(ScriptRunningState.STOPPED);
			}
		}
		// Anything else, assume all good
		else
		{
			changeState(ScriptRunningState.FINISHED);
		}
	}

	/**
	 * Changes the state of the script.
	 * 
	 * @param newState New state
	 */
	private void changeState(final ScriptRunningState newState)
	{
		changeState(eventManager, script.getName(), newState, script, executor);
	}
	
	/**
	 * Changes the state of the script.
	 * 
	 * @param eventManager The Event Manager to be used
	 * @param scriptName The script name
	 * @param newState The new state requested
	 * @param script The script itself
	 * @param executor The executor to be used
	 */
	public static void changeState(final IScriptEventManager eventManager, final String scriptName, 
			final ScriptRunningState newState, final Script script, final Executor executor)
	{		
		logger.trace("Changing [{}] script's state to [{}]", scriptName, newState);
		script.setStatus(newState);
		
		if (eventManager != null && executor != null)
		{
			executor.execute(new Runnable()
			{			
				public void run()
				{							
					eventManager.notifyScriptStateChange(scriptName, newState);
				}
			});
		}
		else
		{
			logger.info("Changed [{}] script's state to [{}]", scriptName, newState);
		}
	}
	
	/**
	 * Gets the runner's thread.
	 * 
	 * @return The runner's Thread object
	 */
	public Thread getThread()
	{
		return this.runningThread;
	}
}
