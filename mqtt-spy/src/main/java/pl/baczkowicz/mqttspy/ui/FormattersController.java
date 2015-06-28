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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.utils.ConversionUtils;

/**
 * Controller for the converter window.
 */
public class FormattersController implements Initializable
{
	final static Logger logger = LoggerFactory.getLogger(FormattersController.class);

	@FXML
	private TextArea sampleInput;
	
	@FXML
	private TextArea sampleOutput;
	
	@FXML
	private TextArea formatterDetails;
	
	@FXML
	private TextField formatterName;
	
	@FXML
	private TextField formatterType;
	
	@FXML
	private Button newButton;
	
	@FXML
	private Button deleteButton;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{		
		sampleInput.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				// TODO: populate output			
			}
		});		
		
	}	
}
