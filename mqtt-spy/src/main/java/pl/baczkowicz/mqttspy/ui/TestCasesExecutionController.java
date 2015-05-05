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
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

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
import pl.baczkowicz.mqttspy.testcases.TestCaseManager;
import pl.baczkowicz.mqttspy.testcases.TestCaseStatus;
import pl.baczkowicz.mqttspy.testcases.TestCaseStepResult;
import pl.baczkowicz.mqttspy.ui.properties.SubscriptionTopicSummaryProperties;
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

	private TestCaseManager testCaseManager;
	
	public void initialize(URL location, ResourceBundle resources)
	{
		scriptManager = new InteractiveScriptManager(eventManager, null);
		testCaseManager = new TestCaseManager(scriptManager);
		
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
				
				root.getChildren().clear();
				for (final TestCaseProperties properties : testCaseManager.loadTestCases(scriptLocation))
				{
					root.getChildren().add(new TreeItem<TestCaseProperties>(properties));
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
		final MenuItem runMenu = new MenuItem("Run all test cases");
		// TODO: enable
		runMenu.setDisable(true);
		runMenu.setOnAction(new EventHandler<ActionEvent>()
		{		
			@Override
			public void handle(ActionEvent event)
			{
				// TODO: start
				final TreeItem<TestCaseProperties> selected = scriptTree.getSelectionModel().getSelectedItem();
			
				if (selected != null && selected.getValue() != null)
				{
					final TestCaseProperties testCaseProperties = selected.getValue();
					
					testCaseManager.runTestCase(testCaseProperties);
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
					final TestCaseProperties testCaseProperties = selected.getValue();
					
					testCaseExecutionPaneController.display(testCaseProperties, ((TestCase) testCaseProperties.getScript()).getSteps());
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
	            (TreeTableColumn.CellDataFeatures<TestCaseProperties, String> p) -> 
	            new ReadOnlyStringWrapper(p.getValue().getValue().lastStartedProperty().getValue())
	    );
		
//		statusColumn.setCellValueFactory
//		(
//	            (TreeTableColumn.CellDataFeatures<TestCaseProperties, TestCaseStatus> param) -> 
//	            new ReadOnlyObjectWrapper<TestCaseStatus>(param.getValue().getValue().statusProperty().getValue())
//	    );
		
		statusColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<TestCaseProperties, TestCaseStatus>, ObservableValue<TestCaseStatus>>() 
		{
            @Override public ObservableValue<TestCaseStatus> call(TreeTableColumn.CellDataFeatures<TestCaseProperties, TestCaseStatus> p) 
            {
                return p.getValue().getValue().statusProperty();
            }
        });
		
		statusColumn.setCellFactory(new Callback<TreeTableColumn<TestCaseProperties,TestCaseStatus>, TreeTableCell<TestCaseProperties,TestCaseStatus>>()
		{			
			public TreeTableCell<TestCaseProperties, TestCaseStatus> call(
					TreeTableColumn<TestCaseProperties, TestCaseStatus> param)
			{
				final TreeTableCell<TestCaseProperties, TestCaseStatus> cell = new TreeTableCell<TestCaseProperties, TestCaseStatus>()
				{
					@Override
					public void updateItem(TestCaseStatus item, boolean empty)
					{
						super.updateItem(item, empty);
						if (!isEmpty())
						{
							setGraphic(testCaseExecutionPaneController.getIconForStatus(item));
						}
						else
						{
							setGraphic(null);
						}
					}
				};
				cell.setAlignment(Pos.CENTER);
				cell.setPadding(new Insets(0, 0, 0, 0));
				
				return cell;
			}
		});
		
		// Note: important - without that, cell height goes nuts with progress indicator
		scriptTree.setFixedCellSize(24);		
		
		testCaseExecutionPaneController.setTestCaseManager(testCaseManager);
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