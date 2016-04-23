/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 * The Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import javax.script.ScriptException;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.MqttConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.ConfiguredMqttConnectionDetails;
import pl.baczkowicz.mqttspy.connectivity.BaseMqttConnection;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.scripts.MqttScriptManager;
import pl.baczkowicz.spy.common.generated.ConversionMethod;
import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.common.generated.FormatterFunction;
import pl.baczkowicz.spy.common.generated.ScriptExecutionDetails;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.formatting.FormattingManager;
import pl.baczkowicz.spy.formatting.FormattingUtils;
import pl.baczkowicz.spy.formatting.ScriptBasedFormatter;
import pl.baczkowicz.spy.scripts.Script;
import pl.baczkowicz.spy.ui.events.FormattersChangedEvent;
import pl.baczkowicz.spy.ui.utils.DialogFactory;
import pl.baczkowicz.spy.ui.utils.TooltipFactory;
import pl.baczkowicz.spy.ui.utils.UiUtils;
import pl.baczkowicz.spy.utils.ConversionUtils;

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
	
	private FormatterDetails selectedFormatter = FormattingUtils.createBasicFormatter("default", "Plain", "", ConversionMethod.PLAIN);

	private MqttConfigurationManager configurationManager;
	
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

	private boolean ignoreChanges;
	
	private IKBus eventBus;

	@FXML
	private AnchorPane formattersWindow;

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
				final FormattedMqttMessage message = new FormattedMqttMessage(0, "", new MqttMessage(sampleInput.getText().getBytes()), connection);							
				
				formatInput(message);
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
							setText(item == newFormatter ? UiUtils.MODIFIED_ITEM + item.getName() : item.getName());
						}
					}
				};
			}
		});		
	}		
	
	public void init()
	{					
		scriptBasedFormatter = new ScriptBasedFormatter(new MqttScriptManager(null, null, connection));
		
		formattersList.getItems().clear();
		
		final List<FormatterDetails> defaultFormatters = FormattingUtils.createBaseFormatters();
		defaultFormatters.addAll(FormattingManager.createDefaultScriptFormatters());
		
		formattersList.getItems().addAll(defaultFormatters);
		addFormattersToList(configurationManager.getConfiguration().getFormatting().getFormatter(), formattersList.getItems());
		
		// Select first on the list
		formattersList.getSelectionModel().select(0);
	}
	
	private void readValues(final FormatterDetails formatter)
	{  
		formatter.setName(formatterName.getText());
		
		if (FormattingUtils.isScriptBased(formatter))
		{
			formatter.getFunction().get(0).getScriptExecution().setInlineScript(ConversionUtils.stringToBase64(formatterDetails.getText()));
		}
	}
	
	private void checkInlineScript(final FormatterDetails formatter) throws ScriptException
	{
		// TODO: should this be a new script object?
		final Script script = scriptBasedFormatter.getScript(formatter);
		script.setScriptContent(formatterDetails.getText());
		
		// Evaluate it
		scriptBasedFormatter.evaluate(script);
		if (script.getScriptRunner().getLastThrownException() != null)
		{
			formatterDetails.getStyleClass().add("invalid");
		}
		else
		{
			sampleOutput.setText(scriptBasedFormatter.formatMessage(formatter, 
				new FormattedMqttMessage(0, "", new MqttMessage(sampleInput.getText().getBytes()), connection), true));
			formatterDetails.getStyleClass().add("valid");
		}
	}
	
	private void onChange()
	{		
		// TODO: split this method into individual on change
		if (ignoreChanges || (newFormatter == null && selectedFormatter == null))
		{
			return;
		}
		
		formatterDetails.getStyleClass().removeAll("valid", "invalid");
		applyChangesButton.setDisable(true);
	
		// New formatter
		if (newFormatter != null)
		{
			try
			{
				checkInlineScript(newFormatter);
				applyChangesButton.setDisable(false);
			}
			catch (ScriptException e)
			{
				formatterDetails.getStyleClass().add("invalid");
				logger.error("Script error", e);
			}
			return;
		}
		
		// Modifying existing formatter
		if (selectedFormatter.getID().startsWith(FormattingUtils.SCRIPT_PREFIX))
		{
			try
			{
				if (!formatterName.getText().equals(selectedFormatter.getName()) 
						|| !formatterDetails.getText().equals(scriptBasedFormatter.getScript(selectedFormatter).getScriptContent()))
				{
					checkInlineScript(selectedFormatter);
					applyChangesButton.setDisable(false);				
				}
			}
			catch (ScriptException e)
			{
				logger.error("Script error: ", e);
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
		if (selectedFormatter == null)			
		{
			return;
		}
		
		int count = 0;
		for (final ConfiguredMqttConnectionDetails connectionDetails : configurationManager.getConnections())
		{
			if (connectionDetails.getFormatter() == null)
			{
				continue;
			}
			
			if (selectedFormatter.getID().equals(((FormatterDetails) connectionDetails.getFormatter()).getID()))
			{
				count++;
			}
		}
		
		Optional<ButtonType> result = null;
		if (count > 0)
		{
			result = DialogFactory.createQuestionDialog("Formatter is still in use", 
					"There are " + count + " connections configured with this formatter. Are you sure you want to delete it?", 
					false);
		}
		
		if (count == 0 || result.get() == ButtonType.YES)
		{
			for (final ConfiguredMqttConnectionDetails connectionDetails : configurationManager.getConnections())
			{					
				connectionDetails.setFormatter(null);					
			}
			configurationManager.getConfiguration().getFormatting().getFormatter().remove(selectedFormatter);
			
			if (configurationManager.saveConfiguration())
			{
				TooltipFactory.createTooltip(deleteButton, "Formatter deleted. Changes saved.");
				init();
				formattersList.getSelectionModel().selectFirst();
				
				eventBus.publish(new FormattersChangedEvent());
			}
		}		
	}
	
	@FXML
	private void newFormatter() throws ScriptException
	{		
		ignoreChanges = true;
		selectedFormatter = null;
		newFormatter = new FormatterDetails();
		
		Optional<String> name = DialogFactory.createInputDialog(
				formattersWindow.getScene().getWindow(), "Enter formatter name", "Name for the new formatter");
		
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
					DialogFactory.createWarningDialog("Invalid name", "Entered formatter name/ID already exists. Please chose a different one.");
					name = DialogFactory.createInputDialog(
							formattersWindow.getScene().getWindow(), "Enter formatter name", "Name for the new formatter");
					cancelled = name.isPresent();
					valid = false;
					break;
				}
			}			
		}
		
		if (!cancelled & valid)
		{
			newFormatter.setID(FormattingUtils.SCRIPT_PREFIX + "-" + name.get().replace(" ", "-").toLowerCase());
			newFormatter.setName(name.get());
			
			formattersList.getItems().add(newFormatter);
			formattersList.getSelectionModel().select(newFormatter);
						
			// TODO: load a proper script here with FileUtils.loadFileByNameBase64Encoded
			final ScriptExecutionDetails scriptExecution = new ScriptExecutionDetails("ZnVuY3Rpb24gZm9ybWF0KCkKewkKCXJldHVybiByZWNlaXZlZE1lc3NhZ2UuZ2V0UGF5bG9hZCgpICsgIi0gbW9kaWZpZWQhIjsKfQ==");
			newFormatter.getFunction().add(new FormatterFunction(null, null, null, null, null, scriptExecution));
					
			formatterName.setText(newFormatter.getName());
			formatterType.setText("Script-based");
			detailsLabel.setText("Inline script");			
			scriptBasedFormatter.addFormatter(newFormatter);
			formatterDetails.setText(scriptBasedFormatter.getScript(newFormatter).getScriptContent());
			sampleOutput.setText(scriptBasedFormatter.formatMessage(newFormatter, 
					new FormattedMqttMessage(0, "", new MqttMessage(sampleInput.getText().getBytes()), connection), 
					true));
			
			newButton.setDisable(true);	
			applyChangesButton.setDisable(false);
			deleteButton.setDisable(true);		
			
			eventBus.publish(new FormattersChangedEvent());
		}
		ignoreChanges = false;
	}
	
	@FXML
	private void applyChanges()
	{		
		if (newFormatter != null)
		{
			readValues(newFormatter);
			configurationManager.getConfiguration().getFormatting().getFormatter().add(newFormatter);
		}
		else
		{
			readValues(selectedFormatter);			
		}
		
		if (configurationManager.saveConfiguration())
		{
			TooltipFactory.createTooltip(newButton, "Formatter added. Changes saved.");
			init();
			formattersList.getSelectionModel().selectFirst();
			
			eventBus.publish(new FormattersChangedEvent());
		}
	}
	
	private void formatInput(final FormattedMqttMessage message)
	{		
		if (FormattingUtils.isScriptBased(selectedFormatter))
		{
			// logger.debug("Formatting using {}", formatter.getName());
			message.setFormattedPayload(scriptBasedFormatter.formatMessage(selectedFormatter, message, false));
			message.setPrettyPayload(scriptBasedFormatter.formatMessage(selectedFormatter, message, true));
		}
		else
		{
			// Use the raw payload to make sure any formatting/encoding that is applied is correct
			message.setFormattedPayload(FormattingUtils.checkAndFormatText(selectedFormatter, message.getRawBinaryPayload()));
			message.setPrettyPayload(message.getFormattedPayload());
		}
		
		sampleOutput.setText(message.getPrettyPayload());
	}
	
	private void showFormatterInfo()	
	{
		// If both are null or the same
		if (selectedFormatter == newFormatter)
		{
			return;
		}	
		
		ignoreChanges = true;
		if (newFormatter != null)
		{
			formattersList.getItems().remove(newFormatter);
			newFormatter = null;
		}
		else if (selectedFormatter != null)
		{
			newButton.setDisable(false);		
			deleteButton.setDisable(false);
			detailsLabel.setText("Details");
			
			formatterName.setText(selectedFormatter.getName());
			if (FormattingUtils.isDefault(selectedFormatter))
			{
				formatterType.setText("Built-in");
				formatterDetails.setText(selectedFormatter.getDescription());
				deleteButton.setDisable(true);
				
				if (selectedFormatter.getName().startsWith("Plain"))
				{
					sampleInput.setText("hello from mqtt-spy!");
				}
				else if (selectedFormatter.getName().contains("JSON"))
				{
					sampleInput.setText("{hello: {from: \"mqtt-spy\", message: \"welcome!\"}}");
				}
				else if (selectedFormatter.getName().contains("XML"))
				{
					sampleInput.setText("<hello><from>mqtt-spy</from><message>welcome!</message></hello>");
				}
				else if (selectedFormatter.getName().startsWith("Base64 decoder"))
				{
					sampleInput.setText(ConversionUtils.stringToBase64("hello from mqtt-spy!"));
				}
				else if (selectedFormatter.getName().startsWith("Base64 encoder"))
				{
					sampleInput.setText("hello from mqtt-spy!");
				}
				else if (selectedFormatter.getName().startsWith("HEX decoder"))
				{
					sampleInput.setText(ConversionUtils.stringToHex("hello from mqtt-spy!"));
				}
				else if (selectedFormatter.getName().startsWith("HEX encoder"))
				{
					sampleInput.setText("hello from mqtt-spy!");
				}				
				
				FormattedMqttMessage message = new FormattedMqttMessage(0, "", new MqttMessage(sampleInput.getText().getBytes()), connection);
				
				if (selectedFormatter.getName().startsWith("Eclipse Kura"))
				{
					final String base64encoded = "H4sIAAAAAAAAAHWR227TQBCGoSU9bB0aSkFNA8JIXLSAjZ0DQeKqpEGINk6V0qJeWZv1Jiz1HrQ+tOHJuObJmHUSkSDwjUf7zcw/889ObfPO7HswD6rz4NfPlX1UJlIISlImRchUpfRh/SI4CfpfA6AHaON7zkOBOQWw/xnn2P4k03Ml04MvvUO7EzMqUvuyB7lltCITyCqdMpHdwoODtrFSMSN42jsydK/TDz46l/7rTu/Y/I67Z6f9K4ig4D3aMmpKyxGLjeBLI1gInXftQSZSxqndFTnTUnAj7LvvXM8Z+o06lFcRkkmYU52AHFRvNl3fbTt5+xXACloHiDX5BmQVa14UbGNCqEpDKoiMmBgDuzf+wRTAZ6icUM1wHIqMD6kGZA1woiDUE+eMFd5tcBnRGFb7B62iyohpfoM1XRhqNXhzBHAHWRFLVIwnc3PNMR4ha8iWdpilv5g68+f9Yb1lFm97Nme3NLLNHEWDtUwZl8z6vve23va9lgfgOdpdPLNIqR5hQpevXUO74DeL8TCm5gqEJonU5mp3m0WPxzIZs3CkYeIbqa8X5imB064R2kNWKlNwjVMu9QTQWr3VaDYNe4LQ1K/Zyn879hRtKazT/xteQ/eXJzDzd0nMVEKL/tZ1pvHCWOjkYnAU+m7D9X4DdrXTyAkDAAA=";
					sampleInput.setText(ConversionUtils.base64ToString(base64encoded));					
					message = new FormattedMqttMessage(0, "", new MqttMessage(ConversionUtils.base64ToArray(base64encoded)), connection);
				}
				
				formatInput(message);
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
					logger.error("Script error", e);
				}
				
				formatterType.setText("Script-based");
				detailsLabel.setText("Inline script");			
				try
				{
					formatterDetails.setText(scriptBasedFormatter.getScript(selectedFormatter).getScriptContent());
				}
				catch (ScriptException e)
				{
					logger.error("Script error", e);
				}
								
				formatInput(new FormattedMqttMessage(0, "", new MqttMessage(sampleInput.getText().getBytes()), connection));		
			} 
			else
			{
				logger.debug("Formatter = {} {}", selectedFormatter.getID(), selectedFormatter.getName());
				formatterType.setText("Function-based");
				formatterDetails.setText("(this formatter type has been deprecated)");
				sampleOutput.setText(FormattingUtils.formatText(selectedFormatter, sampleInput.getText(), sampleInput.getText().getBytes()));
			}		
		}
		applyChangesButton.setDisable(true);
		ignoreChanges = false;
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setConfigurationManager(final MqttConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}

	/**
	 * @param connection the connection to set
	 */
	public void setConnection(final BaseMqttConnection connection)
	{
		this.connection = connection;
	}
	
	/**
	 * Sets the event bus.
	 *  
	 * @param eventBus the eventBus to set
	 */
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}
}
