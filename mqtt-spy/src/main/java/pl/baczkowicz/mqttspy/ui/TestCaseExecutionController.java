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

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.events.EventManager;
import pl.baczkowicz.mqttspy.testcases.TestCaseStatus;
import pl.baczkowicz.mqttspy.ui.properties.TestCaseStepProperties;

/**
 * Controller for the search window.
 */
public class TestCaseExecutionController extends AnchorPane implements Initializable
{
	final static Logger logger = LoggerFactory.getLogger(TestCaseExecutionController.class);	

	private EventManager eventManager;

	private ConfigurationManager configurationManager;
	
	@FXML
	private TableView<TestCaseStepProperties> stepsView;
	
	@FXML
	private TableColumn<TestCaseStepProperties, String> stepNumberColumn;
	
	@FXML
	private TableColumn<TestCaseStepProperties, String> descriptionColumn;
	
	@FXML
	private TableColumn<TestCaseStepProperties, TestCaseStatus> statusColumn;
	
	@FXML
	private TableColumn<TestCaseStepProperties, String> infoColumn;
	
	public void initialize(URL location, ResourceBundle resources)
	{
		stepNumberColumn.setCellValueFactory(new PropertyValueFactory<TestCaseStepProperties, String>("stepNumber"));
		descriptionColumn.setCellValueFactory(new PropertyValueFactory<TestCaseStepProperties, String>("description"));
		statusColumn.setCellValueFactory(new PropertyValueFactory<TestCaseStepProperties, TestCaseStatus>("status"));
		infoColumn.setCellValueFactory(new PropertyValueFactory<TestCaseStepProperties, String>("executionInfo"));
		
		// TODO: for testing only
		stepsView.setTableMenuButtonVisible(true);
	}	

	public void init()
	{
		//
	}	
	
	public void display(final ObservableList<TestCaseStepProperties> items)
	{
		stepsView.getItems().clear();
		
		stepsView.setItems(items);
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
