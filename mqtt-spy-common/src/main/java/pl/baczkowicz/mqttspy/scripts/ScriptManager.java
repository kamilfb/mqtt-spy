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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.ScriptDetails;
import pl.baczkowicz.mqttspy.connectivity.IMqttConnection;
import pl.baczkowicz.mqttspy.exceptions.CriticalException;
import pl.baczkowicz.mqttspy.messages.IMqttMessage;
import pl.baczkowicz.mqttspy.scripts.io.ScriptIO;

/**
 * This class manages script creation and execution.
 */
public class ScriptManager
{
	/** Name of the variable in JS for received messages. */
	public static final String RECEIVED_MESSAGE_PARAMETER = "receivedMessage";
	
	/** Name of the variable in JS for published/searched message. */
	public static final String MESSAGE_PARAMETER = "message";
	
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(ScriptManager.class);
	
	/** Mapping between script files and scripts. */
	private Map<File, Script> scripts = new HashMap<File, Script>();
	
	/** Used for notifying events related to script execution. */
	private IScriptEventManager eventManager;

	/** Executor for tasks. */
	private Executor executor;

	/** Connection for which the script will be run. */
	protected IMqttConnection connection;
	
	/**
	 * Creates the script manager.
	 * 
	 * @param eventManager The event manager to be used
	 * @param executor The executor to be used
	 * @param connection The connection for which to run the scripts
	 */
	public ScriptManager(final IScriptEventManager eventManager, final Executor executor, final IMqttConnection connection)
	{
		this.eventManager = eventManager;
		this.executor = executor;
		this.connection = connection;
	}
	
	/**
	 * Gets the file (script) name for the given file object.
	 * 
	 * @param file The file from which to get the filename
	 * 
	 * @return The name of the script file
	 */
	public static String getScriptName(final File file)
	{
		return file.getName().replace(".js",  "");
	}
	
	/**
	 * Creates and records a script with the given details.
	 * 
	 * @param scriptDetails The script details
	 */
	public void addScript(final ScriptDetails scriptDetails)
	{
		final File scriptFile = new File(scriptDetails.getFile());
		
		final String scriptName = getScriptName(scriptFile);
		
		final Script script = new Script();
				
		createScript(script, scriptName, scriptFile, connection, scriptDetails);
		
		logger.debug("Adding script {}", scriptDetails.getFile());
		scripts.put(scriptFile, script);
	}
	
	/**
	 * Creates and records scripts with the given details.
	 * 
	 * @param scriptDetails The script details
	 */
	public void addScripts(final List<ScriptDetails> scriptDetails)
	{
		for (final ScriptDetails script : scriptDetails)
		{
			addScript(script);
		}
	}
		
	/**
	 * Populates the script object with the necessary values and references.
	 * 
	 * @param script The script object to be populated
	 * @param scriptName The name of the script
	 * @param scriptFile The script's file name 
	 * @param connection The connection for which this script will be running
	 * @param scriptDetails Script details
	 */
	public void createScript(final Script script,
			String scriptName, File scriptFile, final IMqttConnection connection, final ScriptDetails scriptDetails)
	{
		final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");										
		
		if (scriptEngine != null)
		{
			script.setName(scriptName);
			script.setScriptFile(scriptFile);
			script.setStatus(ScriptRunningState.NOT_STARTED);
			script.setScriptEngine(scriptEngine);
			script.setScriptDetails(scriptDetails);
			
			script.setPublicationScriptIO(new ScriptIO(connection, eventManager, script, executor));
			
			final Map<String, Object> scriptVariables = new HashMap<String, Object>();
			scriptVariables.put("mqttspy", script.getPublicationScriptIO());	
			scriptVariables.put("logger", LoggerFactory.getLogger(ScriptRunner.class));
			scriptVariables.put("messageLog", script.getPublicationScriptIO().getMessageLog());
			
			putJavaVariablesIntoEngine(scriptEngine, scriptVariables);
		}
		else
		{
			throw new CriticalException("Cannot instantiate the nashorn javascript engine - most likely you don't have Java 8 installed. "
					+ "Please either disable scripts in your configuration file or install the appropriate JRE/JDK.");
		}
	}
				
	/**
	 * Puts a the given map of variables into the engine.
	 * 
	 * @param engine The engine to be populated with variables
	 * @param variables The variables to be populated
	 */
	public static void putJavaVariablesIntoEngine(final ScriptEngine engine, final Map<String, Object> variables)
	{
		final Bindings bindings = new SimpleBindings();

		for (String key : variables.keySet())
		{
			bindings.put(key, variables.get(key));
		}

		engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
	}
	
	/**
	 * Runs the given script in a synchronous or asynchronous way.
	 * 
	 * @param script The script to run
	 * @param asynchronous Whether to run the script asynchronously or not
	 */
	public void runScript(final Script script, final boolean asynchronous)
	{
		// Only start if not running already
		if (!ScriptRunningState.RUNNING.equals(script.getStatus()))
		{
			script.createScriptRunner(eventManager, executor);
			
			if (asynchronous)
			{
				new Thread(script.getScriptRunner()).start();
			}
			else
			{
				script.getScriptRunner().run();
			}
		}
	}	
	
	/**
	 * Runs the given script and passes the given message as a parameter. Defaults to the 'receivedMessage' parameter and synchronous execution.
	 * 
	 * @param script The script to run
	 * @param message The message to be passed onto the script
	 */
	public void runScriptFileWithReceivedMessage(final String scriptFile, final IMqttMessage receivedMessage)
	{
		final Script script = getScriptForFile(new File(scriptFile));
		
		if (script != null)
		{
			runScriptFileWithMessage(script, ScriptManager.RECEIVED_MESSAGE_PARAMETER, receivedMessage, false);
		}
		else
		{
			logger.warn("No script found for {}", scriptFile);
		}
	}
	
	/**
	 * Runs the given script and passes the given message as a parameter. Defaults to the 'message' parameter and asynchronous execution.
	 * 
	 * @param script The script to run
	 * @param message The message to be passed onto the script
	 */
	public void runScriptFileWithMessage(final Script script, final IMqttMessage message)
	{				
		runScriptFileWithMessage(script, ScriptManager.MESSAGE_PARAMETER, message, true);
	}
	
	/**
	 * Runs the given script and passes the given message as a parameter.
	 * 
	 * @param script The script to run
	 * @param parameterName The name of the message parameter
	 * @param message The message to be passed onto the script
	 * @param asynchronous Whether the call should be asynchronous
	 */
	public void runScriptFileWithMessage(final Script script, final String parameterName, final IMqttMessage message, final boolean asynchronous)
	{				
		script.getScriptEngine().put(parameterName, message);
		runScript(script, asynchronous);		
	}
	
	/**
	 * Gets script object for the given file.
	 * 
	 * @param scriptFile The file for which to get the script object
	 * 
	 * @return Script object or null if not found
	 */
	public Script getScriptForFile(final File scriptFile)
	{
		return scripts.get(scriptFile);
	}

	/**
	 * Checks if any of the scripts is running.
	 * 
	 * @return True if any of the scripts is running
	 */
	public boolean areScriptsRunning()
	{
		for (final Script script : scripts.values())
		{
			if (ScriptRunningState.RUNNING.equals(script.getStatus()))
			{
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Gets the scripts value.
	 *  
	 * @return The scripts value
	 */
	public Map<File, Script> getScripts()
	{
		return scripts;
	}
}
