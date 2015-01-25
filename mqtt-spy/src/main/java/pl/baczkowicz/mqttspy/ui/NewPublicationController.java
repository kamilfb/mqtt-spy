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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

import org.eclipse.paho.client.mqttv3.MqttMessage;
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
import pl.baczkowicz.mqttspy.messages.ReceivedMqttMessage;
import pl.baczkowicz.mqttspy.scripts.InteractiveScriptManager;
import pl.baczkowicz.mqttspy.scripts.Script;
import pl.baczkowicz.mqttspy.scripts.ScriptTypeEnum;
import pl.baczkowicz.mqttspy.ui.keyboard.TimeBasedKeyEventFilter;
import pl.baczkowicz.mqttspy.ui.panes.PaneStatus;
import pl.baczkowicz.mqttspy.ui.panes.PaneVisibilityStatus;
import pl.baczkowicz.mqttspy.ui.panes.PaneWithCustomizableVisibility;
import pl.baczkowicz.mqttspy.ui.properties.PublicationScriptProperties;
import pl.baczkowicz.mqttspy.ui.utils.DialogUtils;
import pl.baczkowicz.mqttspy.utils.ConversionUtils;
import pl.baczkowicz.mqttspy.utils.MqttUtils;
import pl.baczkowicz.mqttspy.utils.TimeUtils;

/**
 * Controller for creating new publications.
 */
public class NewPublicationController implements Initializable, ScriptListChangeObserver, PaneWithCustomizableVisibility
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(NewPublicationController.class);

	/** How many recent messages to store. */
	private final static int MAX_RECENT_MESSAGES = 10;
	
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
	
	@FXML
	private Menu publishWithScriptsMenu;
	
	@FXML
	private Menu recentMessagesMenu;
		
	private ObservableList<String> publicationTopics = FXCollections.observableArrayList();

	private MqttAsyncConnection connection;

	private boolean plainSelected = true;

	private boolean previouslyPlainSelected = true;
	
	private boolean connected;

	private boolean detailedView;
	
	private InteractiveScriptManager scriptManager;

	private EventManager eventManager;
	
	private List<ReceivedMqttMessage> recentMessages = new ArrayList<>();

	private TimeBasedKeyEventFilter timeBasedFilter;
	
	/** Created pane status with index 0 (the first pane). */
	private final PaneStatus paneStatus = new PaneStatus(0);

	public void initialize(URL location, ResourceBundle resources)
	{
		timeBasedFilter = new TimeBasedKeyEventFilter(15);
		
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
		
		publicationTopicText.addEventFilter(KeyEvent.KEY_PRESSED, 
				new EventHandler<KeyEvent>() 
		{
	        @Override
	        public void handle(KeyEvent keyEvent) 
	        {
	        	switch (keyEvent.getCode())
	        	{
		        	case ENTER:
		        	{
		        		if (connected && timeBasedFilter.processEvent(keyEvent))
		        		{
		        			publish();
		        			keyEvent.consume();
		        		}
		        		break;
		        	}		      
		        	case DIGIT0:
		        	{
		        		restoreFromKeypress(keyEvent, 0);
		        		break;
		        	}
		        	case DIGIT1:
		        	{
		        		restoreFromKeypress(keyEvent, 1);
		        		break;
		        	}
		        	case DIGIT2:
		        	{
		        		restoreFromKeypress(keyEvent, 2);
		        		break;
		        	}
		        	case DIGIT3:
		        	{
		        		restoreFromKeypress(keyEvent, 3);
		        		break;
		        	}
		        	case DIGIT4:
		        	{
		        		restoreFromKeypress(keyEvent, 4);
		        		break;
		        	}
		        	case DIGIT5:
		        	{
		        		restoreFromKeypress(keyEvent, 5);
		        		break;
		        	}
		        	case DIGIT6:
		        	{
		        		restoreFromKeypress(keyEvent, 6);
		        		break;
		        	}
		        	case DIGIT7:
		        	{
		        		restoreFromKeypress(keyEvent, 7);
		        		break;
		        	}
		        	case DIGIT8:
		        	{
		        		restoreFromKeypress(keyEvent, 8);
		        		break;
		        	}
		        	case DIGIT9:
		        	{
		        		restoreFromKeypress(keyEvent, 9);
		        		break;
		        	}
		        	default:
		        		break;
	        	}
	        }
	    });
			
		publicationData.setWrapText(true);
		
		publishScript.getToggles().get(0).setUserData(null);
		
		paneStatus.setVisibility(PaneVisibilityStatus.NOT_VISIBLE);
	}		

	public void init()
	{
		eventManager.registerScriptListChangeObserver(this, connection);		
	}

	@Override
	public void onScriptListChange()
	{
		final List<PublicationScriptProperties> scripts = scriptManager.getObservableScriptList();
		
		final List<Script> pubScripts = new ArrayList<>();
		
		for (final PublicationScriptProperties properties : scripts)
		{
			if (ScriptTypeEnum.PUBLICATION.equals(properties.typeProperty().getValue()))
			{
				pubScripts.add(properties.getScript());
			}
		}
		
		updateScriptList(pubScripts, publishWithScriptsMenu, publishScript, "Publish with '%s' script", null);
	}
	
	public static void updateScriptList(final List<Script> scripts, final Menu scriptsMenu, final ToggleGroup toggleGroup, 
			final String format, final EventHandler<ActionEvent> eventHandler)
	{
		while (scriptsMenu.getItems().size() > 0)
		{
			scriptsMenu.getItems().remove(0);
		}
		
		if (scripts.size() > 0)
		{
			for (final Script script : scripts)
			{
				final RadioMenuItem item = new RadioMenuItem(String.format(format, script.getName()));
				item.setOnAction(eventHandler);
				item.setToggleGroup(toggleGroup);
				item.setUserData(script);
				
				scriptsMenu.getItems().add(item);
			}
		}
	}

	public void recordPublicationTopic(final String publicationTopic)
	{
		MqttUtils.recordTopic(publicationTopic, publicationTopics);
	}
	
	public void setConnected(final boolean connected)
	{
		this.connected = connected;
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
	
	/**
	 * Displays the given message.
	 * 
	 * @param message The message to display
	 */
	private void displayMessage(final ReceivedMqttMessage message)
	{
		displayMessage(new BaseMqttMessage(message.getPayload(), message.getTopic(), message.getQoS(), message.isRetained()));	
	}
	
	/**
	 * Displays the given message.
	 * 
	 * @param message The message to display
	 */
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
		// Note: here using the editor, as the value stored directly in the ComboBox might
		// not be committed yet, whereas the editor (TextField) has got the current text in it
		final String topic = publicationTopicText.getEditor().getText();
		
		if (verify && (topic == null || topic.isEmpty()))
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
					
			message.setTopic(topic);
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
		
		if (message != null)
		{
			recordMessage(message);
			
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
	}
	
	/**
	 * Records the given message on the list of 'recent' messages.
	 * 
	 * @param message The message to record
	 */
	private void recordMessage(final BaseMqttMessage message)
	{
		// If the message is the same as previous one, remove the old one
		if (recentMessages.size() > 0 
				&& message.getTopic().equals(recentMessages.get(0).getTopic()) 
				&& message.getValue().equals(recentMessages.get(0).getPayload()))
		{
			recentMessages.remove(0);
		}
		
		final MqttMessage mqttMessage = new MqttMessage();
		mqttMessage.setQos(message.getQos());
		mqttMessage.setRetained(message.isRetained());
		mqttMessage.setPayload(message.getValue().getBytes());
		
		final ReceivedMqttMessage messageToStore = new ReceivedMqttMessage(0, message.getTopic(), mqttMessage);
		
		recentMessages.add(0, messageToStore);
		
		while (recentMessages.size() > MAX_RECENT_MESSAGES)
		{
			recentMessages.remove(MAX_RECENT_MESSAGES);
		}
		
		refreshRecentMessages();
	}
	
	/**
	 * Refreshes the list of recent messages shown in the publish button's context menu.
	 */
	private void refreshRecentMessages()
	{
		// Remove all elements
		while (recentMessagesMenu.getItems().size() > 0)
		{
			recentMessagesMenu.getItems().remove(0);
		}
		
		// Add all elements
		for (final ReceivedMqttMessage message : recentMessages)
		{
			final String topic = message.getTopic();
			final String payload = message.getPayload().length() > 10 ? message.getPayload().substring(0, 10) + "..." : message.getPayload();
			final String time = TimeUtils.DATE_WITH_SECONDS_SDF.format(message.getDate());
			
			final MenuItem item = new MenuItem("Topic = '" + topic + "', payload = '" + payload + "', published at " + time);
			item.setOnAction(new EventHandler<ActionEvent>()
			{	
				@Override
				public void handle(ActionEvent event)
				{
					displayMessage(message);
				}
			});
			recentMessagesMenu.getItems().add(item);
		}
	}	
	
	/**
	 * Restores message from the key event.
	 * 
	 * @param keyEvent The generated key event
	 * @param keyNumber The key number
	 */
	private void restoreFromKeypress(final KeyEvent keyEvent, final int keyNumber)
	{
		if (keyEvent.isAltDown())
		{
			// 1 means first message (most recent); 2 is second, etc.; 0 is the 10th (the oldest)
			final int arrayIndex = (keyNumber > 0 ? keyNumber : MAX_RECENT_MESSAGES) - 1;
			
			if (arrayIndex < recentMessages.size())
			{
				displayMessage(recentMessages.get(arrayIndex));
			}
        	keyEvent.consume();
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

	public void hidePublishButton()
	{
		this.publishButton.setVisible(false);	
	}
	
	@Override
	public PaneStatus getPaneStatus()
	{		
		return paneStatus;
	}
}
