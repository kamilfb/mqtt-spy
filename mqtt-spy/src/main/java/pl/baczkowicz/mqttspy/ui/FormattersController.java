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
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import javax.script.ScriptException;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.ConversionMethod;
import pl.baczkowicz.mqttspy.common.generated.FormatterDetails;
import pl.baczkowicz.mqttspy.common.generated.FormatterFunction;
import pl.baczkowicz.mqttspy.common.generated.ScriptExecutionDetails;
import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.connectivity.BaseMqttConnection;
import pl.baczkowicz.mqttspy.messages.BaseMqttMessageWithSubscriptions;
import pl.baczkowicz.mqttspy.scripts.Script;
import pl.baczkowicz.mqttspy.scripts.ScriptBasedFormatter;
import pl.baczkowicz.mqttspy.scripts.ScriptManager;
import pl.baczkowicz.mqttspy.ui.utils.DialogUtils;
import pl.baczkowicz.mqttspy.utils.FormattingUtils;

/**
 * Controller for the converter window.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class FormattersController implements Initializable
{
	final static Logger logger = LoggerFactory.getLogger(FormattersController.class);

	@FXML
	private Label detailsLabel;
	
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
	private Button applyChangesButton;
	
	@FXML
	private Button deleteButton;
	
	private FormatterDetails selectedFormatter = FormattingUtils.createBasicFormatter("default", "Plain", ConversionMethod.PLAIN);

	private ConfigurationManager configurationManager;
	
	private ScriptBasedFormatter scriptBasedFormatter;

	private BaseMqttConnection connection;
	
	private final ChangeListener basicOnChangeListener = new ChangeListener()
	{
		@Override
		public void changed(ObservableValue observable, Object oldValue, Object newValue)
		{
			onChange();			
		}		
	};

	private FormatterDetails newFormatter;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{		
		formatterName.textProperty().addListener(basicOnChangeListener);
		formatterDetails.textProperty().addListener(basicOnChangeListener);
		
		sampleInput.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				if (newFormatter != null)
				{
					sampleOutput.setText(scriptBasedFormatter.formatMessage(newFormatter, 
							new BaseMqttMessageWithSubscriptions(0, "", new MqttMessage(sampleInput.getText().getBytes()), connection)));
				}
				else
				{
					sampleOutput.setText(FormattingUtils.formatText(selectedFormatter, sampleInput.getText(), sampleInput.getText().getBytes()));
				}
			}
		});						
		
		formattersList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FormatterDetails>()
		{			
			@Override
			public void changed(ObservableValue<? extends FormatterDetails> observable,
					FormatterDetails oldValue, FormatterDetails newValue)
			{
				selectedFormatter = newValue;		
				showFormatterInfo();
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
		scriptBasedFormatter = new ScriptBasedFormatter();
		scriptBasedFormatter.setScriptManager(new ScriptManager(null, null, connection));
		
		formattersList.getItems().clear();
		
		final List<FormatterDetails> baseFormatters = FormattingUtils.createBaseFormatters();	
		formattersList.getItems().addAll(baseFormatters);
		addFormattersToList(configurationManager.getConfiguration().getFormatting().getFormatter(), formattersList.getItems());
	}
	
	private void onChange()
	{		
		applyChangesButton.setDisable(true);
	
		if (newFormatter != null)
		{
			applyChangesButton.setDisable(false);
			try
			{
				final Script script = scriptBasedFormatter.getScript(newFormatter);
				script.setScriptContent(formatterDetails.getText());
				
				// Evaluate it
				scriptBasedFormatter.evaluate(script);
				
				sampleOutput.setText(scriptBasedFormatter.formatMessage(newFormatter, 
						new BaseMqttMessageWithSubscriptions(0, "", new MqttMessage(sampleInput.getText().getBytes()), connection)));
			}
			catch (ScriptException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		else if (selectedFormatter == null)
		{
			return;
		}
		
		if (selectedFormatter.getID().startsWith(FormattingUtils.SCRIPT_PREFIX))
		{
			try
			{
				if (!formatterName.getText().equals(selectedFormatter.getName()) 
						|| !formatterDetails.getText().equals(scriptBasedFormatter.getScript(selectedFormatter).getScriptContent()))
				{
					applyChangesButton.setDisable(false);
				}
			}
			catch (ScriptException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
	
	@FXML
	private void deleteFormatter()
	{
		// TODO
	}
	
	@FXML
	private void newFormatter() throws ScriptException
	{		
		selectedFormatter = null;
		newFormatter = new FormatterDetails();
		
		Optional<String> name = DialogUtils.askForInput("Enter formatter name", "Name for the new formatter");
		
		boolean cancelled = !name.isPresent();
		boolean valid = false;
		
		while (!cancelled && !valid)
		{
			newFormatter.setID(FormattingUtils.SCRIPT_PREFIX + "-" + name.get().replace(" ", "-").toLowerCase());
			newFormatter.setName(name.get());
			
			valid = true;
			for (FormatterDetails formatter : formattersList.getItems())
			{
				logger.info("{}, {}, {}, {}", formatter.getName(), newFormatter.getName(), formatter.getID(), newFormatter.getID());
				if (formatter.getName().equals(newFormatter.getName()) || formatter.getID().equals(newFormatter.getID()))
				{
					DialogUtils.showWarning("Invalid name", "Entered formatter name/ID already exists. Please chose a different one.");
					name = DialogUtils.askForInput("Enter formatter name", "Name for the new formatter");
					cancelled = name.isPresent();
					valid = false;
					break;
				}
			}			
		}
		
		newFormatter.setID(FormattingUtils.SCRIPT_PREFIX + "-" + name.get().replace(" ", "-").toLowerCase());
		newFormatter.setName(name.get());
					
		final ScriptExecutionDetails scriptExecution = new ScriptExecutionDetails("ZnVuY3Rpb24gZm9ybWF0KCkKewkKCXJldHVybiByZWNlaXZlZE1lc3NhZ2UuZ2V0UGF5bG9hZCgpICsgIi0gbW9kaWZpZWQhIjsKfQ==");
		newFormatter.getFunction().add(new FormatterFunction(null, null, null, null, null, scriptExecution));
				
		formatterName.setText(newFormatter.getName());
		formatterType.setText("Script-based");
		detailsLabel.setText("Inline script");			
		scriptBasedFormatter.addFormatter(newFormatter);
		formatterDetails.setText(scriptBasedFormatter.getScript(newFormatter).getScriptContent());
		sampleOutput.setText(scriptBasedFormatter.formatMessage(newFormatter, 
				new BaseMqttMessageWithSubscriptions(0, "", new MqttMessage(sampleInput.getText().getBytes()), connection)));
		
		newButton.setDisable(true);	
		applyChangesButton.setDisable(false);
		deleteButton.setDisable(true);		
	}
	
	@FXML
	private void applyChanges()
	{
		// TODO
	}
	
	private void showFormatterInfo()	
	{
		newFormatter = null;
		newButton.setDisable(false);		
		deleteButton.setDisable(false);
		detailsLabel.setText("Details");
		
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
			// Add just in case
			try
			{
				scriptBasedFormatter.addFormatter(selectedFormatter);
			}
			catch (ScriptException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			formatterType.setText("Script-based");
			detailsLabel.setText("Inline script");			
			try
			{
				formatterDetails.setText(scriptBasedFormatter.getScript(selectedFormatter).getScriptContent());
			}
			catch (ScriptException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			sampleOutput.setText(scriptBasedFormatter.formatMessage(selectedFormatter, 
					new BaseMqttMessageWithSubscriptions(0, "", new MqttMessage(sampleInput.getText().getBytes()), connection)));
		} 
		else
		{
			formatterType.setText("Function-based");
			formatterDetails.setText("(this formatter type has been deprecated)");
			sampleOutput.setText(FormattingUtils.formatText(selectedFormatter, sampleInput.getText(), sampleInput.getText().getBytes()));
		}		
		
		applyChangesButton.setDisable(true);
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setConfigurationManager(final ConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}
//	
//	public void setScriptBasedFormatter(final ScriptBasedFormatter scriptBasedFormatter)
//	{
//		this.scriptBasedFormatter = scriptBasedFormatter;
//	}

	/**
	 * @param connection the connection to set
	 */
	public void setConnection(final BaseMqttConnection connection)
	{
		this.connection = connection;
	}
}
