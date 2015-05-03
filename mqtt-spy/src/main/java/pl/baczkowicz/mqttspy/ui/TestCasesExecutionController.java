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
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import pl.baczkowicz.mqttspy.testcases.TestCaseStatus;
import pl.baczkowicz.mqttspy.testcases.TestCaseStepResult;
import pl.baczkowicz.mqttspy.ui.properties.TestCaseProperties;
import pl.baczkowicz.mqttspy.ui.properties.TestCaseStepProperties;
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
	private TreeTableView<TestCaseProperties> scriptTree;
	
	private TreeItem<TestCaseProperties> root = new TreeItem<>();
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private TestCaseExecutionController testCaseExecutionPaneController;
	
	@FXML
	private TreeTableColumn<TestCaseProperties, String> nameColumn;	
	
	@FXML
	private TreeTableColumn<TestCaseProperties, String> lastRunColumn;
	
	@FXML
	private TreeTableColumn<TestCaseProperties, TestCaseStatus> statusColumn;
	
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
					scriptDetails.setRepeat(false);
										
					final String scriptName = ScriptManager.getScriptName(scriptFile);
					
					final TestCase testCase = new TestCase();
							
					scriptManager.createFileBasedScript(testCase, scriptName, scriptFile, null, scriptDetails);
					
					try
					{	
						scriptManager.runScript(testCase, false);
						testCase.setInfo((TestCaseInfo) scriptManager.invokeFunction(testCase, "getInfo"));
//						testCase.getScriptEngine().eval(new FileReader(testCase.getScriptFile()));
//						final Invocable invocable = (Invocable) testCase.getScriptEngine();
//						
//						testCase.setInfo((TestCaseInfo) invocable.invokeFunction("getInfo"));
						
						int stepNumber = 1;
						for (final String step : testCase.getInfo().getSteps())
						{
							testCase.getSteps().add(new TestCaseStepProperties(
									String.valueOf(stepNumber), step, TestCaseStatus.NOT_RUN, ""));
							stepNumber++;
						}
						
						logger.info(testCase.getInfo().getName() + " " + Arrays.asList(testCase.getInfo().getSteps()));
					}
					catch (ScriptException | NoSuchMethodException e)
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
					
					root.getChildren().add(new TreeItem<TestCaseProperties>(new TestCaseProperties(testCase)));
				}
				
				scriptTree.getSelectionModel().clearSelection();
				// TODO: get all dirs
				// TODO: load
			}
		});
		contextMenu.getItems().add(setLocationMenu);
		
		// Separator
		contextMenu.getItems().add(new SeparatorMenuItem());
		
		// Start
		final MenuItem runMenu = new MenuItem("Run test case");
		runMenu.setOnAction(new EventHandler<ActionEvent>()
		{		
			@Override
			public void handle(ActionEvent event)
			{
				// TODO: start
				final TreeItem<TestCaseProperties> selected = scriptTree.getSelectionModel().getSelectedItem();
			
				if (selected != null && selected.getValue() != null)
				{
					runTestCase(selected, selected.getValue().getScript());
				}
			}
		});
		contextMenu.getItems().add(runMenu);
		
		scriptTree.setContextMenu(contextMenu);				
		scriptTree.setRoot(root);
		scriptTree.setShowRoot(false);
		
		scriptTree.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TreeItem<TestCaseProperties>>(){

			@Override
			public void onChanged(Change<? extends TreeItem<TestCaseProperties>> c)
			{
				final TreeItem<TestCaseProperties> selected = scriptTree.getSelectionModel().getSelectedItem();

				if (selected != null && selected.getValue() != null)
				{					
					testCaseExecutionPaneController.display(((TestCase) selected.getValue().getScript()).getSteps());
				}
			}
			
		});
		
		nameColumn.setCellValueFactory
		(
	            (TreeTableColumn.CellDataFeatures<TestCaseProperties, String> param) -> 
	            new ReadOnlyStringWrapper(param.getValue().getValue().getName())
	    );
		
		lastRunColumn.setCellValueFactory
		(
	            (TreeTableColumn.CellDataFeatures<TestCaseProperties, String> param) -> 
	            new ReadOnlyStringWrapper(param.getValue().getValue().lastStartedProperty().getValue())
	    );
		
		statusColumn.setCellValueFactory
		(
	            (TreeTableColumn.CellDataFeatures<TestCaseProperties, TestCaseStatus> param) -> 
	            new ReadOnlyObjectWrapper<TestCaseStatus>(param.getValue().getValue().statusProperty().getValue())
	    );
		
		scriptManager = new InteractiveScriptManager(eventManager, null);
		
		// TODO: for testing only
		// scriptTree.setTableMenuButtonVisible(true);
	}	

	public void init()
	{
		//
	}	
	
	public void runTestCase(final TreeItem<TestCaseProperties> selected, final TestCase testCase)
	{
		final TestCaseProperties testCaseProperties = selected.getValue();
		
		// testCase.setTestCaseStatus(TestCaseStatus.NOT_RUN);
		testCaseProperties.statusProperty().setValue(TestCaseStatus.NOT_RUN);
		// testCaseProperties.update();		
		
		// Clear last run for this test case
		for (final TestCaseStepProperties properties : testCase.getSteps())
		{
			properties.statusProperty().setValue(TestCaseStatus.NOT_RUN);
			properties.executionInfoProperty().setValue("");
		}
		
		new Thread(new Runnable()
		{			
			@Override
			public void run()
			{
				TestCaseStepResult lastResult = null;
				int step = 0;				
				
				while (step < testCase.getSteps().size())
				{
					final TestCaseStepProperties properties = testCase.getSteps().get(step);
					
					try
					{
						final TestCaseStepResult result = (TestCaseStepResult) scriptManager.invokeFunction(
								testCase, "step" + properties.stepNumberProperty().getValue());
						lastResult = result;
						
						if (result == null)
						{
							// TODO
							continue;
						}
						
						Platform.runLater(new Runnable()
						{							
							@Override
							public void run()
							{
								properties.statusProperty().setValue(result.getStatus());
								properties.executionInfoProperty().setValue(result.getInfo());
							}
						});
						
						// If not in progress any more, move to next
						if (!TestCaseStatus.IN_PROGRESS.equals(result.getStatus()))
						{
							step++;
						}														
					}
					catch (NoSuchMethodException | ScriptException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		
				}		
				
				final TestCaseStepResult testCaseStatus = lastResult;
				Platform.runLater(new Runnable()
				{							
					@Override
					public void run()
					{
						testCaseProperties.statusProperty().setValue(testCaseStatus.getStatus());
						// selected.setValue(testCaseProperties);
						selected.valueProperty().set(testCaseProperties);
					}
				});
				
				// testCase.setTestCaseStatus(lastResult.getStatus());
				// testCaseProperties.update();					
			}
		}).start();		
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