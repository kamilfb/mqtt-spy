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
package pl.baczkowicz.mqttspy.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.AnchorPane;

import javax.script.Invocable;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.ScriptDetails;
import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.events.EventManager;
import pl.baczkowicz.mqttspy.scripts.InteractiveScriptManager;
import pl.baczkowicz.mqttspy.scripts.ScriptManager;
import pl.baczkowicz.mqttspy.testcases.TestCase;
import pl.baczkowicz.mqttspy.testcases.TestCaseInfo;
import pl.baczkowicz.mqttspy.ui.properties.PublicationScriptProperties;
import pl.baczkowicz.mqttspy.utils.FileUtils;

/**
 * Controller for the test cases execution window.
 */
public class TestCasesExecutionController extends AnchorPane implements Initializable
{
	/** Initial and minimal scene/stage width. */	
	public final static int WIDTH = 780;
	
	/** Initial and minimal scene/stage height. */
	public final static int HEIGHT = 550;
		
	final static Logger logger = LoggerFactory.getLogger(TestCaseExecutionController.class);	

	@FXML
	private TreeTableView<PublicationScriptProperties> scriptTree;
	
	private TreeItem<PublicationScriptProperties> root = new TreeItem<PublicationScriptProperties>();
	
	@FXML
	private TreeTableColumn<PublicationScriptProperties, String> nameColumn;	
	
	private String scriptLocation;
	
	private EventManager eventManager;

	private ConfigurationManager configurationManager;
	
	private InteractiveScriptManager scriptManager;
	
	public void initialize(URL location, ResourceBundle resources)
	{
		final ContextMenu contextMenu = new ContextMenu();
		
		// Set location
		final MenuItem setLocationMenu = new MenuItem("Load test cases from folder");	
		setLocationMenu.setOnAction(new EventHandler<ActionEvent>()
		{		
			@Override
			public void handle(ActionEvent event)
			{
				// TODO: select location
				scriptLocation = "/home/kamil/Programming/Git/mqtt-spy/src/test/resources/test_cases";
				
				final List<File> scripts = FileUtils.getDirectoriesWithFile(scriptLocation, "tc.*js");
				root.getChildren().clear();
				for (final File scriptFile : scripts)
				{
					logger.info("Adding " + scriptFile.getName() + " with parent " + scriptFile.getParent());
					
					final ScriptDetails scriptDetails = new ScriptDetails();					
					scriptDetails.setFile(scriptFile.getAbsolutePath());
										
					final String scriptName = ScriptManager.getScriptName(scriptFile);
					
					final TestCase testCase = new TestCase();
							
					scriptManager.createFileBasedScript(testCase, scriptName, scriptFile, null, scriptDetails);
					
					try
					{					
						testCase.getScriptEngine().eval(new FileReader(testCase.getScriptFile()));
						final Invocable invocable = (Invocable) testCase.getScriptEngine();
						
						testCase.setInfo((TestCaseInfo) invocable.invokeFunction("getInfo"));
						
						logger.info(testCase.getInfo().getName() + " " + Arrays.asList(testCase.getInfo().getSteps()));
					}
					catch (FileNotFoundException | ScriptException | NoSuchMethodException e)
					{
						logger.error("Cannot read test case", e);
					}
					
					// Override name
					if (testCase.getInfo() != null && testCase.getInfo().getName() != null)
					{
						testCase.setName(testCase.getInfo().getName());
					}
					else
					{
						testCase.setName(scriptFile.getParentFile().getName());
					}
					
					root.getChildren().add(new TreeItem<PublicationScriptProperties>(new PublicationScriptProperties(testCase)));
				}
				// TODO: get all dirs
				// TODO: load
			}
		});
		contextMenu.getItems().add(setLocationMenu);
		
		// Separator
		contextMenu.getItems().add(new SeparatorMenuItem());
		
		// Start
		final MenuItem startMenu = new MenuItem("Start");
		startMenu.setOnAction(new EventHandler<ActionEvent>()
		{		
			@Override
			public void handle(ActionEvent event)
			{
				// TODO: start
				final TreeItem<PublicationScriptProperties> selected = scriptTree.getSelectionModel().getSelectedItem();
				
			}
		});
		contextMenu.getItems().add(startMenu);
		
		scriptTree.setContextMenu(contextMenu);				
		scriptTree.setRoot(root);
		scriptTree.setShowRoot(false);
		
		scriptTree.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TreeItem<PublicationScriptProperties>>(){

			@Override
			public void onChanged(Change<? extends TreeItem<PublicationScriptProperties>> c)
			{
				final TreeItem<PublicationScriptProperties> selected = scriptTree.getSelectionModel().getSelectedItem();

				if (selected != null)
				{
//					final Script script = selected.getValue().getScript();
//					
//					try
//					{					
//						script.getScriptEngine().eval(new FileReader(script.getScriptFile()));
//						final Invocable invocable = (Invocable) script.getScriptEngine();
//						
//						final TestCaseInfo info = (TestCaseInfo) invocable.invokeFunction("getInfo");
//						
//						logger.info(info.getName() + " " + Arrays.asList(info.getSteps()));
//					}
//					catch (FileNotFoundException | ScriptException | NoSuchMethodException e)
//					{
//						logger.error("Cannot read test case", e);
//					}
				}
			}
			
		});
		
		nameColumn.setCellValueFactory
		(
	            (TreeTableColumn.CellDataFeatures<PublicationScriptProperties, String> param) -> 
	            new ReadOnlyStringWrapper(param.getValue().getValue().getName())
	    );
		
		scriptManager = new InteractiveScriptManager(eventManager, null);
	}	

	public void init()
	{
		//
	}	
	
	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setEventManager(final EventManager eventManager)
	{
		this.eventManager = eventManager;
	}

	public void setConfingurationManager(final ConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}
}