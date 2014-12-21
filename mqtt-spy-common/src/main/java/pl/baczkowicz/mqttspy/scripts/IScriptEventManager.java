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

/**
 * Interface for notifying script's state changes.
 */
public interface IScriptEventManager
{
	/**
	 * Notifies any consumers that the state of the given script has changed.
	 * 
	 * @param scriptName The name of the script
	 * @param state The new state
	 */
	// TODO: could possibly change the scriptName to the script object
	void notifyScriptStateChange(final String scriptName, final ScriptRunningState state);
}
