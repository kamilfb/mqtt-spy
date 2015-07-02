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
import pl.baczkowicz.mqttspy.messages.BaseMqttMessageWithSubscriptions;
import pl.baczkowicz.mqttspy.utils.ConversionUtils;

public class ScriptBasedFormatter
{
	final static Logger logger = LoggerFactory.getLogger(ScriptBasedFormatter.class);	
	
	private ScriptManager scriptManager;
	
	private Map<FormatterDetails, Script> formattingScripts = new HashMap<>();
		
	public ScriptBasedFormatter(final ScriptManager scriptManager)
	{
		this.scriptManager = scriptManager;
	}

	public Script getScript(final FormatterDetails formatter) throws ScriptException
	{
		Script script = formattingScripts.get(formatter);
		
		if (script == null)
		{
			addFormatter(formatter);
			script = formattingScripts.get(formatter);
		}
		
		return script;
	}
	
	public void evaluate(final Script script)
	{
		// Evaluate it
		scriptManager.runScript(script, false);
		
		// Run before / setup
		try
		{
			scriptManager.invokeFunction(script, "before");
		}
		catch (NoSuchMethodException | ScriptException e)
		{
			logger.info("No setup method present");
		}
	}
	
	public void addFormatter(final FormatterDetails formatter) throws ScriptException
	{
		final Script script = scriptManager.addInlineScript(formatter.getID(), 
				ConversionUtils.base64ToString(formatter.getFunction().get(0).getScriptExecution().getInlineScript()));
		
		// Store it for future
		formattingScripts.put(formatter, script);
		
		evaluate(script);
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
//
//	/**
//	 * @param scriptManager the scriptManager to set
//	 */
//	public void setScriptManager(final ScriptManager scriptManager)
//	{
//		this.scriptManager = scriptManager;
//	}

}
