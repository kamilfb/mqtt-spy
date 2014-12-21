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
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.ScriptDetails;
import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.generated.TabbedSubscriptionDetails;
import pl.baczkowicz.mqttspy.connectivity.IMqttConnection;
import pl.baczkowicz.mqttspy.ui.properties.PublicationScriptProperties;
import pl.baczkowicz.mqttspy.ui.utils.RunLaterExecutor;

/**
 * Script manager that interacts with the UI.
 */
public class InteractiveScriptManager extends ScriptManager
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(InteractiveScriptManager.class);
	
	/** List of scripts, as displayed on the UI. */
	private final ObservableList<PublicationScriptProperties> observableScriptList = FXCollections.observableArrayList();
	
	public InteractiveScriptManager(final IScriptEventManager eventManager, final IMqttConnection connection)
	{
		super(eventManager, new RunLaterExecutor(), connection);
	}
			
	public void addScripts(final String directory)
	{
		final List<File> files = new ArrayList<File>(); 
		
		// Adds new entries to the list of new files are present
		if (directory != null && !directory.isEmpty())
		{
			files.addAll(getFileNamesForDirectory(directory, ".js"));				
		}
		else
		{
			// If directory defined, use the mqtt-spy's home directory
			files.addAll(getFileNamesForDirectory(ConfigurationManager.getDefaultHomeDirectory(), ".js"));
		}	
		
		populateScriptsFromFileList(files, ScriptTypeEnum.PUBLICATION);
	}
	
	public void addSubscriptionScripts(final List<TabbedSubscriptionDetails> list)
	{
		final List<ScriptDetails> scripts = new ArrayList<>(); 
		
		for (final TabbedSubscriptionDetails sub : list)
		{
			if (sub.getScriptFile() != null  && !sub.getScriptFile().trim().isEmpty())
			{
				scripts.add(new ScriptDetails(false, sub.getScriptFile()));
			}
		}
		
		populateScripts(scripts, ScriptTypeEnum.SUBSCRIPTION);
	}
	
	public void populateScriptsFromFileList(final List<File> files, final ScriptTypeEnum type)
	{
		for (final File scriptFile : files)
		{
			PublicationScriptProperties script = retrievePublicationScriptProperties(observableScriptList, scriptFile);
			if (script == null)					
			{
				final String scriptName = getScriptName(scriptFile);
				
				final PublicationScriptProperties properties = new PublicationScriptProperties();
				properties.typeProperty().setValue(type);
				
				createScript(properties, scriptName, scriptFile, connection, new ScriptDetails(false, scriptFile.getName())); 			
				
				observableScriptList.add(properties);
				getScripts().put(scriptFile, properties);
			}				
		}			
	}
	
	public void populateScripts(final List<ScriptDetails> scriptDetails, final ScriptTypeEnum type)
	{
		for (final ScriptDetails details : scriptDetails)
		{
			final File scriptFile = new File(details.getFile());
			
			if (!scriptFile.exists())					
			{
				logger.warn("Script {} does not exist!", details.getFile());
			}
			else
			{
				logger.info("Adding script {}", details.getFile());
							
				PublicationScriptProperties script = retrievePublicationScriptProperties(observableScriptList, scriptFile);
				if (script == null)					
				{
					final String scriptName = getScriptName(scriptFile);
					
					final PublicationScriptProperties properties = new PublicationScriptProperties();
					properties.typeProperty().setValue(type);
					
					createScript(properties, scriptName, scriptFile, connection, details); 			
					
					observableScriptList.add(properties);
					getScripts().put(scriptFile, properties);
				}	
			}
		}			
	}
	
	private static PublicationScriptProperties retrievePublicationScriptProperties(final List<PublicationScriptProperties> scriptList, final File scriptFile)
	{
		for (final PublicationScriptProperties script : scriptList)
		{
			if (script.getScriptFile().getAbsolutePath().equals(scriptFile.getAbsolutePath()))
			{
				return script;
			}
		}
		
		return null;
	}
	
	private static List<File> getFileNamesForDirectory(final String directory, final String extension)
	{
		final List<File> files = new ArrayList<File>();
		
		final File folder = new File(directory);
		File[] listOfFiles = folder.listFiles();

		if (listOfFiles != null)
		{
			for (int i = 0; i < listOfFiles.length; i++)
			{
				if (listOfFiles[i].isFile())
				{
					if (extension == null || extension.isEmpty() ||  listOfFiles[i].getName().endsWith(extension))
					{
						files.add(listOfFiles[i]);
					}
				}
			}
		}
		else
		{
			logger.error("No files in {}", directory);
		}
		
		return files;
	}
	
	public void stopScriptFile(final File scriptFile)
	{
		final Thread scriptThread = getPublicationScriptProperties(observableScriptList, getScriptName(scriptFile)).getScriptRunner().getThread();

		if (scriptThread != null)
		{
			scriptThread.interrupt();
		}
	}
	
	public static PublicationScriptProperties getPublicationScriptProperties(final ObservableList<PublicationScriptProperties> observableScriptList, final String scriptName)
	{
		for (final PublicationScriptProperties script : observableScriptList)
		{
			if (script.getName().equals(scriptName))
			{
				return script;				
			}
		}
		
		return null;
	}
	
	public ObservableList<PublicationScriptProperties> getObservableScriptList()
	{
		return observableScriptList;
	}		
}
