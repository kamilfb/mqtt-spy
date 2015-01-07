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
package pl.baczkowicz.mqttspy.ui.search;

import pl.baczkowicz.mqttspy.connectivity.MqttContent;
import pl.baczkowicz.mqttspy.scripts.Script;
import pl.baczkowicz.mqttspy.scripts.ScriptManager;

public class FileScriptMatcher implements SearchMatcher
{
	private ScriptManager scriptManager;
	
	private Script script;

	public FileScriptMatcher(final ScriptManager scriptManager, final Script script)
	{
		this.scriptManager = scriptManager;
		this.script = script;
	}
	
	@Override
	public boolean matches(MqttContent message)
	{
		scriptManager.runScriptFileWithMessage(script, ScriptManager.MESSAGE_PARAMETER, message, false);
		return (Boolean) script.getScriptRunner().getLastReturnValue();	
	}

}
