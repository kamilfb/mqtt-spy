/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 * The Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */
package pl.baczkowicz.mqttspy.daemon;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.ReconnectionSettings;
import pl.baczkowicz.mqttspy.configuration.PropertyFileLoader;
import pl.baczkowicz.mqttspy.connectivity.SimpleMqttConnection;
import pl.baczkowicz.mqttspy.connectivity.reconnection.ReconnectionManager;
import pl.baczkowicz.mqttspy.daemon.configuration.ConfigurationLoader;
import pl.baczkowicz.mqttspy.daemon.configuration.generated.DaemonMqttConnectionDetails;
import pl.baczkowicz.mqttspy.daemon.configuration.generated.RunningMode;
import pl.baczkowicz.mqttspy.daemon.connectivity.MqttCallbackHandler;
import pl.baczkowicz.mqttspy.daemon.connectivity.SimpleMqttConnectionRunnable;
import pl.baczkowicz.mqttspy.scripts.ScriptManager;
import pl.baczkowicz.mqttspy.scripts.io.MqttScriptIO;
import pl.baczkowicz.mqttspy.testcases.TestCase;
import pl.baczkowicz.mqttspy.testcases.TestCaseManager;
import pl.baczkowicz.mqttspy.testcases.TestCaseResult;
import pl.baczkowicz.spy.exceptions.SpyException;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.scripts.Script;
import pl.baczkowicz.spy.scripts.ScriptIO;
import pl.baczkowicz.spy.utils.ThreadingUtils;

/**
 * The main class of the daemon.
 */
public class MqttSpyDaemon
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(MqttSpyDaemon.class);
	
	private ConfigurationLoader loader;

	private ScriptManager scriptManager;
	
	private TestCaseManager testCaseManager;

	private ReconnectionManager reconnectionManager;

	private SimpleMqttConnection connection;

	private MqttCallbackHandler callback;

	private MqttScriptIO scriptIO;
	
	/**
	 * This is an internal method - initialises the daemon class.
	 * 
	 * @throws XMLException Thrown if cannot instantiate itself
	 */
	public void initialise() throws XMLException
	{
		loader = new ConfigurationLoader();
		showInfo();
	}
		
	public boolean start(final String configurationFile)
	{
		try
		{		
			initialise();
									
			loadAndRun(configurationFile);
			
			return true;
		}
		catch (XMLException e)
		{
			logger.error("Cannot load the mqtt-spy-daemon's configuration", e);
		}
		catch (SpyException e)
		{
			logger.error("Error occurred while connecting to broker", e);
		}
		
		return false;
	}
	
	private void showInfo()
	{
		logger.info("#######################################################");
		logger.info("### Starting mqtt-spy-daemon v{}", loader.getFullVersionName());
		logger.info("### If you find it useful, see how you can help at {}", loader.getProperty(PropertyFileLoader.DOWNLOAD_URL));
		logger.info("### To get release updates follow @mqtt_spy on Twitter ");
		logger.info("#######################################################");
	}
	
	/**
	 * This is an internal method - requires "initialise" to be called first.
	 * 
	 * @param configurationFile Location of the configuration file
	 * @throws MqttSpyException Thrown if cannot initialise
	 */
	public void loadAndRun(final String configurationFile) throws SpyException
	{
		// Load the configuration
		loader.loadConfiguration(new File(configurationFile));
		
		// Retrieve connection details
		final DaemonMqttConnectionDetails connectionSettings = loader.getConfiguration().getConnection();

		// Wire up all classes (assuming ID = 0)
		reconnectionManager = new ReconnectionManager();
		connection = new SimpleMqttConnection(reconnectionManager, "0", connectionSettings);
		scriptManager = new ScriptManager(null, null, connection);
		testCaseManager = new TestCaseManager(scriptManager);
		callback = new MqttCallbackHandler(connection, connectionSettings, scriptManager); 
				
		// Set up reconnection
		final ReconnectionSettings reconnectionSettings = connection.getMqttConnectionDetails().getReconnectionSettings();			
		final Runnable connectionRunnable = new SimpleMqttConnectionRunnable(scriptManager, connection, connectionSettings);
		
		connection.connect(callback, connectionRunnable);
		scriptIO = new MqttScriptIO(connection, null, null, null);
		if (reconnectionSettings != null)
		{
			new Thread(reconnectionManager).start();
		}
		
		// Run all configured scripts
		final List<Script> backgroundScripts = scriptManager.addScripts(connectionSettings.getBackgroundScript());
		for (final Script script : backgroundScripts)
		{
			logger.info("About to start background script " + script.getName());
			scriptManager.runScript(script, true);
		}
		
		// If in 'scripts only' mode, exit when all scripts finished
		if (RunningMode.SCRIPTS_ONLY.equals(connectionSettings.getRunningMode()))
		{
			stop();
		}
	}
	
	/**
	 * Tries to stop all running threads.
	 */
	public void stop()
	{
		ThreadingUtils.sleep(1000);
		
		// Wait until all scripts have completed or got frozen
		while (scriptManager.areScriptsRunning())
		{
			ThreadingUtils.sleep(1000);
		}
		
		// Stop reconnection manager
		if (reconnectionManager != null)
		{
			reconnectionManager.stop();
		}
						
		// Disconnect
		connection.disconnect();
		
		// Stop message logger
		callback.stop();
		
		ThreadingUtils.sleep(1000);
		for (final Thread thread : Thread.getAllStackTraces().keySet())
		{
			logger.trace("Thread {} is still running", thread.getName());
		}
		logger.info("All tasks completed - bye bye...");
	}
	
	public TestCaseResult runTestCase(final String testCaseLocation)	
	{
		return runTestCase(testCaseLocation, null);
	}	
	
	public TestCaseResult runTestCase(final String testCaseLocation, final Map<String, String> args)	
	{
		final TestCase testCase = testCaseManager.addTestCase(new File(testCaseLocation));
		// TODO: add protection against missing/invalid files
		testCaseManager.runTestCase(testCase, args);
		return testCase.getTestCaseResult();
	}	
	
	public void runScript(final String scriptLocation)
	{
		runScript(scriptLocation, null);
	}
	
	public void runScript(final String scriptLocation, final Map<String, String> args)
	{
		final Script script = scriptManager.addScript(scriptLocation);
		scriptManager.runScript(script, false, args);
	}
	
	/**
	 * This exposes additional methods, e.g. publish, subscribe, unsubscribe.
	 *  
	 * @return The Script IO with the extra methods
	 */
	public ScriptIO more()
	{
		return scriptIO;
	}
}
