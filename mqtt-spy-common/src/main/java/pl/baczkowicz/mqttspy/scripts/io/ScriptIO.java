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
package pl.baczkowicz.mqttspy.scripts.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.concurrent.Executor;

import javax.script.Bindings;
import javax.script.ScriptContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.IMqttConnection;
import pl.baczkowicz.mqttspy.scripts.IScriptEventManager;
import pl.baczkowicz.mqttspy.scripts.Script;
import pl.baczkowicz.mqttspy.scripts.ScriptRunner;
import pl.baczkowicz.mqttspy.scripts.ScriptRunningState;
import pl.baczkowicz.mqttspy.utils.TimeUtils;

/**
 * Implementation of the interface between a script and the mqttspy object.
 */
public class ScriptIO implements IScriptIO
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(ScriptIO.class);
	
	/** Reference to the MQTT connection. */
	private final IMqttConnection connection;
	
	/** Script properties. */
	private Script script;
	
	// TODO: could possibly replace that with a local variable
	/** The number of messages published by the script. */
	private int publishedMessages;
	
	/** Last time the script touched or published a message. */
	private long lastTouch;

	/** Event manager for notifying about various events. */
	private final IScriptEventManager eventManager;

	/** Task executor. */
	private Executor executor;

	/** The messageLog object, as seen by the script. */
	private final IMessageLogIO messageLog;
	
	/**
	 * Creates the PublicationScriptIO.
	 * 
	 * @param connection The connection for which the script is executed
	 * @param eventManager The global event manager
	 * @param script The script itself
	 * @param executor Task executor
	 */
	public ScriptIO(
			final IMqttConnection connection, final IScriptEventManager eventManager, 
			final Script script, final Executor executor)
	{
		this.eventManager = eventManager;
		this.connection = connection;
		this.script = script;
		this.executor = executor;
		this.messageLog = new MessageLogIO();
	}

	@Override
	public void touch()
	{
		this.lastTouch = TimeUtils.getMonotonicTime();
	}
	
	@Override
	public void setScriptTimeout(final long customTimeout)
	{
		script.setScriptTimeout(customTimeout);
		logger.debug("Timeout for script {} changed to {}", script.getName(), customTimeout);
	}
	
	@Override
	public boolean instantiate(final String className)
	{
		try
		{
			final Bindings bindings = script.getScriptEngine().getBindings(ScriptContext.ENGINE_SCOPE);
			bindings.put(className.replace(".", "_"), Class.forName(className).newInstance());
			script.getScriptEngine().setBindings(bindings, ScriptContext.ENGINE_SCOPE);
			return true;
		}
		catch (Exception e)
		{
			logger.error("Cannot instantiate class " + className, e);
			return false;
		}
	}
	
	@Override
	public String execute(final String command) throws IOException, InterruptedException
	{
		Runtime rt = Runtime.getRuntime();
		Process p = rt.exec(command);
		p.waitFor();
		BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = null;

		try
		{
			final StringBuffer sb = new StringBuffer();
			while ((line = input.readLine()) != null)
			{
				sb.append(line);
			}
			return sb.toString();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}			
		
		return null;
	}
	
	@Override
	public void publish(final String publicationTopic, final String data)
	{
		publish(publicationTopic, data, 0, false);
	}
	
	@Override
	public void publish(final String publicationTopic, final String data, final int qos, final boolean retained)
	{
		touch();

		if (!script.getStatus().equals(ScriptRunningState.RUNNING))
		{
			ScriptRunner.changeState(eventManager, script.getName(), ScriptRunningState.RUNNING, script, executor);
		}
		
		logger.debug("[JS {}] Publishing message to {} with payload = {}, qos = {}, retained = {}", script.getName(), publicationTopic, data, qos, retained);
		final boolean published = connection.publish(publicationTopic, data, qos, retained);
		
		if (published)
		{				
			publishedMessages++;
			
			if (executor != null)
			{
				executor.execute(new Runnable()
				{			
					public void run()
					{
						script.setLastPublished(new Date());
						script.setMessagesPublished(publishedMessages);				
					}
				});
			}
			else
			{
				script.setLastPublished(new Date());
				script.setMessagesPublished(publishedMessages);		
			}
		}
	}

	/**
	 * Returns the time of the last touch.
	 * 
	 * @return Time of the last touch (in milliseconds)
	 */
	public long getLastTouch()
	{
		return lastTouch;
	}

	/**
	 * Gets the messageLog object.
	 * 
	 * @return The messageLog object
	 */
	public IMessageLogIO getMessageLog()
	{
		return messageLog;
	}
	
	/**
	 * Stops any running tasks (threads).
	 */
	public void stop()
	{
		messageLog.stop();
	}
}
