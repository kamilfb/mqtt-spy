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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.ConversionMethod;
import pl.baczkowicz.mqttspy.common.generated.FormatterDetails;
import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.utils.FormattingUtils;

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
	private ListView<FormatterDetails> formattersList;
	
	@FXML
	private Button newButton;
	
	@FXML
	private Button deleteButton;
	
	private FormatterDetails selectedFormatter = FormattingUtils.createBasicFormatter("default", "Plain", ConversionMethod.PLAIN);

	private ConfigurationManager configurationManager;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{		
		sampleInput.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				sampleOutput.setText(FormattingUtils.formatText(selectedFormatter, sampleInput.getText(), sampleInput.getText().getBytes()));	
			}
		});					
		
		final List<FormatterDetails> baseFormatters = FormattingUtils.createBaseFormatters();
		formattersList.getItems().addAll(baseFormatters);	
		
		formattersList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FormatterDetails>()
		{			
			@Override
			public void changed(ObservableValue<? extends FormatterDetails> observable,
					FormatterDetails oldValue, FormatterDetails newValue)
			{
				selectedFormatter = newValue;		
				updateFormatterInfo();
			}
		});
		formattersList.setCellFactory(new Callback<ListView<FormatterDetails>, ListCell<FormatterDetails>>()
		{
			@Override
			public ListCell<FormatterDetails> call(ListView<FormatterDetails> l)
			{
				return new ListCell<FormatterDetails>()
				{
					@Override
					protected void updateItem(FormatterDetails item, boolean empty)
					{
						super.updateItem(item, empty);
						if (item == null || empty)
						{
							setText(null);
						}
						else
						{									
							setText(item.getName());
						}
					}
				};
			}
		});		
	}		
	
	public void init()
	{	
		addFormattersToList(configurationManager.getConfiguration().getFormatting().getFormatter(), formattersList.getItems());
	}

	public static void addFormattersToList(final List<FormatterDetails> formatters, 
			final ObservableList<FormatterDetails> observableList)
	{
		for (final FormatterDetails formatterDetails : formatters)
		{			
			// Make sure the element we're trying to add is not on the list already
			boolean found = false;
			
			for (final FormatterDetails existingFormatterDetails : observableList)
			{
				if (existingFormatterDetails.getID().equals(formatterDetails.getID()))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				observableList.add(formatterDetails);
			}
		}	
	}
	
	private void updateFormatterInfo()	
	{
		// TODO: program that button
		newButton.setDisable(true);
		
		deleteButton.setDisable(false);
		
		formatterName.setText(selectedFormatter.getName());
		if (selectedFormatter.getID().startsWith(FormattingUtils.DEFAULT_PREFIX))
		{
			formatterType.setText("Built-in");
			formatterDetails.setText("N/A");
			deleteButton.setDisable(true);
			sampleOutput.setText(FormattingUtils.formatText(selectedFormatter, sampleInput.getText(), sampleInput.getText().getBytes()));
		}
		else if (selectedFormatter.getID().startsWith(FormattingUtils.SCRIPT_PREFIX))
		{
			formatterType.setText("Script-based");
			formatterDetails.setText("Script location = " + selectedFormatter.getFunction().get(0).getScriptExecution().getScriptLocation());
			
			// TODO:
			sampleOutput.setText("Not implemented yet");
		} 
		else
		{
			formatterType.setText("Function-based");
			formatterDetails.setText("(this formatter type has been deprecated)");
			sampleOutput.setText(FormattingUtils.formatText(selectedFormatter, sampleInput.getText(), sampleInput.getText().getBytes()));
		}			
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setConfigurationManager(final ConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}
}
