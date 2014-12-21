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
import java.util.Date;
import java.util.concurrent.Executor;

import javax.script.ScriptEngine;

import pl.baczkowicz.mqttspy.scripts.io.ScriptIO;

/**
 * This class represents a JS script run with the Nashorn engine.
 */
public class Script extends BasicScriptProperties
{	
	/** The running state of the script. */
	private ScriptRunningState status;
	
	/** Number of messages published by the script. */
	private Long messagesPublished;

	/** Timestamp of the last publication. */
	private Date lastPublished;
	
	/** The associated script file. */
	private File scriptFile;
	
	/** Script engine instance. */
	private ScriptEngine scriptEngine;

	/** The publication script IO. */
	private ScriptIO scriptIO;
	
	/** The script runner - dedicated runnable for that script. */
	private ScriptRunner scriptRunner;

	/**
	 * Creates a script.
	 */
	public Script()
	{
		// Default
	}
	
	/**
	 * Creates a script runner for the script if it doesn't exist yet.
	 * 
	 * @param eventManager The event manager to use
	 * @param executor The executor to use
	 */
	public void createScriptRunner(final IScriptEventManager eventManager, final Executor executor)
	{
		if (scriptRunner == null)
		{
			this.scriptRunner = new ScriptRunner(eventManager, this, executor);
		}
	}

	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setMessagesPublished(final long messageCount)
	{
		this.messagesPublished = messageCount;
	}
	
	public Date getLastPublishedDate()
	{
		return lastPublished;
	}

	public Long getMessagesPublished()
	{
		return messagesPublished;
	}

	public void setLastPublished(final Date lastPublishedDate)
	{
		this.lastPublished = lastPublishedDate;
	}
	
	public void setStatus(final ScriptRunningState status)
	{
		this.status = status;
	}
	
	public File getScriptFile()
	{
		return this.scriptFile;
	}

	public ScriptEngine getScriptEngine()
	{
		return scriptEngine;
	}

	public ScriptRunningState getStatus()
	{
		return status;
	}

	public void setPublicationScriptIO(ScriptIO publicationScriptIO)
	{
		this.scriptIO = publicationScriptIO;
	}
	
	public ScriptIO getPublicationScriptIO()
	{
		return scriptIO;
	}


	public void setScriptEngine(final ScriptEngine scriptEngine)
	{
		this.scriptEngine = scriptEngine;
	}

	public void setScriptFile(final File scriptFile)
	{
		this.scriptFile = scriptFile;
	}

	public ScriptRunner getScriptRunner()
	{
		return this.scriptRunner;
	}
}
