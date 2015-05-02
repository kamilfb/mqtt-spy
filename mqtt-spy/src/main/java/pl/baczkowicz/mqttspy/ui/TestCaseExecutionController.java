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
import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.events.EventManager;

/**
 * Controller for the search window.
 */
public class TestCaseExecutionController extends AnchorPane implements Initializable
{
	final static Logger logger = LoggerFactory.getLogger(TestCaseExecutionController.class);	

	private EventManager eventManager;

	private ConfigurationManager configurationManager;
	
	public void initialize(URL location, ResourceBundle resources)
	{
		//				
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
