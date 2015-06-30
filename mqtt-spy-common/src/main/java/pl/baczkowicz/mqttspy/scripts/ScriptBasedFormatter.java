/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
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

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.FormatterDetails;
import pl.baczkowicz.mqttspy.common.generated.ScriptDetails;
import pl.baczkowicz.mqttspy.messages.BaseMqttMessageWithSubscriptions;

public class ScriptBasedFormatter
{
	final static Logger logger = LoggerFactory.getLogger(ScriptBasedFormatter.class);	
	
	private ScriptManager scriptManager;
	
	private Map<FormatterDetails, Script> formattingScripts = new HashMap<>();
		
	public void addFormatter(final FormatterDetails formatter) throws ScriptException
	{
		final Script script = scriptManager.addScript(
				new ScriptDetails(false, false, formatter.getFunction().get(0).getScriptExecution().getScriptLocation()));
		
		// Store it for future
		formattingScripts.put(formatter, script);
		
		// Evaluate it
		scriptManager.runScript(script, false);
		
		// Run before / setup
		try
		{
			scriptManager.invokeFunction(script, "before");
		}
		catch (NoSuchMethodException e)
		{
			logger.info("No setup method present");
		}
	}
	
	public String formatMessage(final FormatterDetails formatter, final BaseMqttMessageWithSubscriptions message)
	{
		try
		{
			Script script = formattingScripts.get(formatter);
			
			if (script == null)
			{
				addFormatter(formatter);
				script = formattingScripts.get(formatter);
			}
			
			script.getScriptEngine().put(ScriptManager.RECEIVED_MESSAGE_PARAMETER, message);		
		
			return (String) scriptManager.invokeFunction(script, "format");
		}
		catch (NoSuchMethodException | ScriptException e)
		{
			return message.getPayload();
		}	
	}

	/**
	 * @param scriptManager the scriptManager to set
	 */
	public void setScriptManager(final ScriptManager scriptManager)
	{
		this.scriptManager = scriptManager;
	}

}
