/***********************************************************************************
 * 
 * Copyright (c) 2014 Kamil Baczkowicz
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
package pl.baczkowicz.mqttspy.ui.search;

import pl.baczkowicz.mqttspy.scripts.Script;
import pl.baczkowicz.mqttspy.scripts.ScriptManager;
import pl.baczkowicz.mqttspy.storage.FormattedMqttMessage;
import pl.baczkowicz.spy.ui.utils.DialogFactory;

public class ScriptMatcher implements SearchMatcher
{
	private ScriptManager scriptManager;
	
	private Script script;

	public ScriptMatcher(final ScriptManager scriptManager, final Script script)
	{
		this.scriptManager = scriptManager;
		this.script = script;
	}
	
	@Override
	public boolean matches(FormattedMqttMessage message)
	{
		boolean matches = false;
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

	@Override
	public boolean isValid()
	{
		if (script.getScriptRunner().getLastThrownException() != null)
		{
			DialogFactory.createErrorDialog("Script execution error", "Script failed due to: " + script.getScriptRunner().getLastThrownException().getLocalizedMessage());
			return false;
		}

		return true;
	}
}
