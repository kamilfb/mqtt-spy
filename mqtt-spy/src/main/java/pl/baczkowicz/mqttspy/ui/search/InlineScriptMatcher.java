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
package pl.baczkowicz.mqttspy.ui.search;

import pl.baczkowicz.mqttspy.connectivity.MqttContent;
import pl.baczkowicz.mqttspy.scripts.Script;
import pl.baczkowicz.mqttspy.scripts.ScriptManager;

public class InlineScriptMatcher implements SearchMatcher
{
	private Script script;
	
	private ScriptManager scriptManager;

	public InlineScriptMatcher(final ScriptManager scriptManager, final String inlineScript)
	{
		this.scriptManager = scriptManager; 
		script = scriptManager.addInlineScript("inline", 
				"function search() "
				+ "{ "
					+ "var payload = message.getPayload(); "
					+ "var formattedPayload = message.getFormattedPayload(); "
				    + "var content = formattedPayload; "
				    + "var topic = message.getTopic(); "
				    + "var qos = message.getQoS(); "
					+ "if (" + inlineScript + ") "
					+ "{ "
						+ "return true; "
					+ "} "
					+ "return false; "
				+ "} "
				+ "search();");
	}
	
	@Override
	public boolean matches(MqttContent message)
	{
		boolean matches = false;
		
		// TODO: run script in true/false mode? Otherwise might look like it's been stopped or sth			
		scriptManager.runScriptFileWithMessage(script, ScriptManager.MESSAGE_PARAMETER, message, false);
		
		if (script.getScriptRunner().getLastReturnValue() != null)
		{
			matches = (Boolean) script.getScriptRunner().getLastReturnValue();
		}
		else
		{
			matches = false;
		}	
				
		return matches;
	}

}
