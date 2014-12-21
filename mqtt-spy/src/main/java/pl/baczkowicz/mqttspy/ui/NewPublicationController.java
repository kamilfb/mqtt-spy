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
package pl.baczkowicz.mqttspy.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;

import org.fxmisc.richtext.StyleClassedTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.BaseMqttMessage;
import pl.baczkowicz.mqttspy.configuration.generated.ConversionMethod;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.events.EventManager;
import pl.baczkowicz.mqttspy.events.observers.ScriptListChangeObserver;
import pl.baczkowicz.mqttspy.exceptions.ConversionException;
import pl.baczkowicz.mqttspy.messages.BaseMqttMessageWrapper;
import pl.baczkowicz.mqttspy.scripts.InteractiveScriptManager;
import pl.baczkowicz.mqttspy.scripts.Script;
import pl.baczkowicz.mqttspy.scripts.ScriptTypeEnum;
import pl.baczkowicz.mqttspy.ui.properties.PublicationScriptProperties;
import pl.baczkowicz.mqttspy.ui.utils.DialogUtils;
import pl.baczkowicz.mqttspy.utils.ConversionUtils;
import pl.baczkowicz.mqttspy.utils.MqttUtils;

/**
 * Controller for creating new publications.
 */
public class NewPublicationController implements Initializable, ScriptListChangeObserver
{
	final static Logger logger = LoggerFactory.getLogger(NewPublicationController.class);

	@FXML
	private SplitMenuButton publishButton;
	
	@FXML
	private ToggleGroup publishScript;

	@FXML
	private ComboBox<String> publicationTopicText;

	@FXML
	private ChoiceBox<String> publicationQosChoice;

	@FXML
	private StyleClassedTextArea publicationData;
		
	@FXML
	private ToggleGroup formatGroup;
	
	@FXML
	private CheckBox retainedBox;
	
	@FXML
	private Label retainedLabel;
	
	@FXML
	private Label publicationQosLabel;
	
	@FXML
	private MenuButton formatMenu;
		
	private ObservableList<String> publicationTopics = FXCollections.observableArrayList();

	private MqttAsyncConnection connection;

	private boolean plainSelected = true;

	private boolean previouslyPlainSelected = true;

	private boolean detailedView;
	
	private InteractiveScriptManager scriptManager;

	private EventManager eventManager;

	public void initialize(URL location, ResourceBundle resources)
	{
		publicationTopicText.setItems(publicationTopics);
		formatGroup.getToggles().get(0).setUserData(ConversionMethod.PLAIN);
		formatGroup.getToggles().get(1).setUserData(ConversionMethod.HEX_DECODE);
		formatGroup.selectToggle(formatGroup.getToggles().get(0));
		
		formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue)
			{
				// If plain has been selected
				if (newValue != null)
				{
					if (formatGroup.getSelectedToggle().getUserData().equals(ConversionMethod.PLAIN))
					{
						showAsPlain();
					}
					else
					{
						showAsHex();
					}
				}
			}
		});
			
		publicationData.setWrapText(true);
		
		publishScript.getToggles().get(0).setUserData(null);
	}		

	public void init()
	{
		eventManager.registerScriptListChangeObserver(this, connection);		
	}

	@Override
	public void onScriptListChange()
	{
		List<PublicationScriptProperties> scripts = scriptManager.getObservableScriptList();
		
		List<PublicationScriptProperties> pubScripts = new ArrayList<>();
		
		for (final PublicationScriptProperties script : scripts)
		{
			if (ScriptTypeEnum.PUBLICATION.equals(script.typeProperty().getValue()))
			{
				pubScripts.add(script);
			}
		}
		
		updateScriptList(pubScripts);
	}
	
	public void updateScriptList(final List<PublicationScriptProperties> scripts)
	{
		while (publishButton.getItems().size() > 1)
		{
			publishButton.getItems().remove(1);
		}
		
		if (scripts.size() > 1)
		{
			publishButton.getItems().add(new SeparatorMenuItem());
			for (final PublicationScriptProperties script : scripts)
			{
				final RadioMenuItem item = new RadioMenuItem("Publish with '" + script.getName() + "' script");
				item.setToggleGroup(publishScript);
				item.setUserData(script);
				publishButton.getItems().add(item);
			}
		}
	}

	public void recordPublicationTopic(final String publicationTopic)
	{
		MqttUtils.recordTopic(publicationTopic, publicationTopics);
	}
	
	public void setConnected(final boolean connected)
	{
		this.publishButton.setDisable(!connected);
		this.publicationTopicText.setDisable(!connected);
	}
	
	@FXML
	public void showAsPlain()
	{
		plainSelected = true;
		if (previouslyPlainSelected != plainSelected)
		{
			try
			{
				final String convertedText = ConversionUtils.hexToString(publicationData.getText());
				logger.info("Converted {} to {}", publicationData.getText(), convertedText);
				
				publicationData.clear();				
				publicationData.appendText(convertedText);
				
				formatMenu.setText("Input format: Plain");
				previouslyPlainSelected = plainSelected;
			}
			catch (ConversionException e)
			{
				showAndLogHexError();
				
				formatGroup.selectToggle(formatGroup.getToggles().get(1));
				formatMenu.setText("Input format: Hex");
				plainSelected = false;
			}
		}
	}
	
	@FXML
	public void showAsHex()
	{
		plainSelected = false;
		if (previouslyPlainSelected != plainSelected)
		{
			final String convertedText = ConversionUtils.stringToHex(publicationData.getText());
			logger.info("Converted {} to {}", publicationData.getText(), convertedText);
			
			publicationData.clear();
			publicationData.appendText(convertedText);
			
			formatMenu.setText("Input format: Hex");
			previouslyPlainSelected = plainSelected;
		}
	}
	
	private void updateVisibility()
	{
		if (detailedView)
		{
			AnchorPane.setRightAnchor(publicationTopicText, 327.0);
			publicationQosChoice.setVisible(true);
			publicationQosLabel.setVisible(true);
			retainedBox.setVisible(true);
			retainedLabel.setVisible(true);
		}
		else
		{
			AnchorPane.setRightAnchor(publicationTopicText, 128.0);
			publicationQosChoice.setVisible(false);
			publicationQosLabel.setVisible(false);
			retainedBox.setVisible(false);
			retainedLabel.setVisible(false);
		}
	}
	
	public void setDetailedViewVisibility(final boolean visible)
	{
		detailedView = visible;
		updateVisibility();
	}
	
	public void toggleDetailedViewVisibility()
	{
		detailedView = !detailedView;
		updateVisibility();
	}
	
	public void displayMessage(final BaseMqttMessage message)
	{
		if (message == null)
		{
			publicationTopicText.setValue("");
			publicationTopicText.setPromptText("(cannot be empty)");
			publicationQosChoice.getSelectionModel().select(0);
			publicationData.clear();
			retainedBox.setSelected(false);
		}
		else
		{
			publicationTopicText.setValue(message.getTopic());
			publicationQosChoice.getSelectionModel().select(message.getQos());
			publicationData.clear();
			publicationData.appendText(message.getValue());
			retainedBox.setSelected(message.isRetained());
		}
	}
	
	public BaseMqttMessage readMessage(final boolean verify)
	{
		if (verify && (publicationTopicText.getValue() == null || publicationTopicText.getValue().isEmpty()))
		{
			logger.error("Cannot publish to an empty topic");
			
			DialogUtils.showError("Invalid topic", "Cannot publish to an empty topic.");
			return null;
		}
		
		final BaseMqttMessage message = new BaseMqttMessage();
		try
		{
			String data = publicationData.getText();
		
			if (!previouslyPlainSelected)
			{
				data = ConversionUtils.hexToString(data);
			}
					
			message.setTopic(publicationTopicText.getValue());
			message.setQos(publicationQosChoice.getSelectionModel().getSelectedIndex());
			message.setValue(data);
			message.setRetained(retainedBox.isSelected());
			
			return message;
		}
		catch (ConversionException e)
		{
			showAndLogHexError();
			return null;
		}		
	}
	
	@FXML
	public void publish()
	{						
		final BaseMqttMessage message = readMessage(true);
		
		final Script script = (Script) publishScript.getSelectedToggle().getUserData();
				
		if (script == null)
		{			
			logger.debug("Publishing with no script");
			connection.publish(message.getTopic(), message.getValue(), message.getQos(), message.isRetained());
		
			recordPublicationTopic(message.getTopic());
		}
		else
		{
			logger.debug("Publishing with '{}' script", script.getName());
			
			// Publish with script
			scriptManager.runScriptFileWithMessage(script, new BaseMqttMessageWrapper(message));
		}
	}
	
	private void showAndLogHexError()
	{
		logger.error("Cannot convert " + publicationData.getText() + " to plain text");
		
		DialogUtils.showError("Invalid hex format", "Provided text is not a valid hex string.");
	}

	public void setConnection(MqttAsyncConnection connection)
	{
		this.connection = connection;
	}

	public void clearTopics()
	{
		publicationTopics.clear();		
	}	

	public ComboBox<String> getPublicationTopicText()
	{
		return publicationTopicText;
	}

	public ChoiceBox<String> getPublicationQosChoice()
	{
		return publicationQosChoice;
	}

	public StyleClassedTextArea getPublicationData()
	{
		return publicationData;
	}

	public CheckBox getRetainedBox()
	{
		return retainedBox;
	}
	
	public void setScriptManager(final InteractiveScriptManager scriptManager)
	{
		this.scriptManager = scriptManager;
	}
	
	public void setEventManager(final EventManager eventManager)
	{
		this.eventManager = eventManager;
	}
}
