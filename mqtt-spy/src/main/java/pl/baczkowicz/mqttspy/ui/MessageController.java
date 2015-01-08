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
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;

import org.fxmisc.richtext.StyleClassedTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.generated.FormatterDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttContent;
import pl.baczkowicz.mqttspy.events.observers.MessageFormatChangeObserver;
import pl.baczkowicz.mqttspy.events.observers.MessageIndexChangeObserver;
import pl.baczkowicz.mqttspy.storage.BasicMessageStore;
import pl.baczkowicz.mqttspy.ui.search.SearchOptions;
import pl.baczkowicz.mqttspy.ui.utils.FormattingUtils;
import pl.baczkowicz.mqttspy.utils.TimeUtils;

/**
 * Controller for displaying a message.
 */
public class MessageController implements Initializable, MessageIndexChangeObserver, MessageFormatChangeObserver
{
	final static Logger logger = LoggerFactory.getLogger(MessageController.class);

	@FXML
	private StyleClassedTextArea dataField;

	@FXML
	private ToggleButton wrapToggle;
	
	@FXML
	private CheckBox retainedField;

	@FXML
	private TextField topicField;

	@FXML
	private TextField timeField;

	@FXML
	private TextField qosField;

	@FXML
	private Label lengthLabel;
	
	@FXML
	private Label retainedFieldLabel;
	
	@FXML
	private Label qosFieldLabel;

	private BasicMessageStore store;
	
	private MqttContent message;

	private FormatterDetails selectionFormat = null;

	private Tooltip tooltip;
	
	private Tooltip lengthTooltip;

	private SearchOptions searchOptions;

	private boolean detailedView;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{		
		dataField.selectedTextProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue,
					String newValue)
			{
				updateTooltipText();				
			}
		});		
	}
	
	public void init()
	{
		tooltip = new Tooltip("");
		tooltip.setWrapText(true);
		
		lengthTooltip = new Tooltip();
		lengthLabel.setTooltip(lengthTooltip);
	}
	
	private void updateVisibility()
	{
		if (detailedView)
		{
			// Doing this so the panel doesn't get bigger
			AnchorPane.setRightAnchor(topicField, null);
			topicField.setPrefWidth(100);
			
			// Apply sizing and visibility
			AnchorPane.setRightAnchor(topicField, 342.0);
			qosField.setVisible(true);
			retainedField.setVisible(true);
			qosFieldLabel.setVisible(true);
			retainedFieldLabel.setVisible(true);
		}
		else
		{
			AnchorPane.setRightAnchor(topicField, 205.0);
			qosField.setVisible(false);
			retainedField.setVisible(false);
			qosFieldLabel.setVisible(false);
			retainedFieldLabel.setVisible(false);
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
	
	@Override
	public void onMessageIndexChange(final int index)
	{
		updateMessage(index);
	}
	

	@Override
	public void onFormatChange()
	{
		showMessageData();		
	}
	
	private void updateMessage(final int messageIndex)
	{
		if (messageIndex > 0)
		{
			MqttContent message = null; 
		
			// Optimised for showing the latest message
			if (messageIndex == 1)
			{
				synchronized (store)
				{
					message = store.getMessages().get(0);
					populate(message);
				}
			}
			else
			{
				synchronized (store)
				{
					final List<MqttContent> messages = store.getMessages();
					
					// Make sure we don't try to re-display a message that is not in the store anymore
					if (messageIndex <= messages.size())
					{
						message = messages.get(messageIndex - 1);
						populate(message);
					}
				}				
			}			
		}
		else
		{
			clear();
		}
	}

	public void populate(final MqttContent message)
	{
		// Don't populate with the same message object
		if (message != null && !message.equals(this.message))
		{
			this.message = message;
	
			final String payload = new String(message.getMessage().getPayload());
			logger.trace("Message payload = " + payload);
	
			topicField.setText(message.getTopic());
			qosField.setText(String.valueOf(message.getMessage().getQos()));
			timeField.setText(TimeUtils.DATE_WITH_MILLISECONDS_SDF.format(message.getDate()));
			populateLength(payload.length());
			retainedField.setSelected(message.getMessage().isRetained());
	
			showMessageData();
		}
	}
	
	private void populateLength(final long length)
	{
		lengthTooltip.setText("Message length = " + length);
		
		if (length < 1000)
		{
			lengthLabel.setText("(" + length + "B)");
		}
		else
		{
			final long lengthInKB = length / 1000;
			
			if (lengthInKB < 1000)
			{
				lengthLabel.setText("(" + lengthInKB + "kB)");
			}
			else
			{
				final long lengthInMB = lengthInKB / 1000;
				lengthLabel.setText("(" + lengthInMB + "MB)");				
			}
		}
	}

	public void clear()
	{
		this.message = null;

		topicField.setText("");
		
		dataField.clear();		
		
		qosField.setText("");
		timeField.setText("");
		lengthLabel.setText("(0)");
		retainedField.setSelected(false);
	}
	
	public void formatSelection(final FormatterDetails messageFormat)
	{
		this.selectionFormat = messageFormat;
		
		if (selectionFormat != null)
		{
			updateTooltipText();
			dataField.setTooltip(tooltip);
		}
		else			
		{
			dataField.setTooltip(null);
		}
	}

	private void showMessageData()
	{
		if (message != null)
		{
			final String textToDisplay = message.getFormattedPayload(store.getFormatter());

			// Won't refresh the text if it is the same...
			if (!textToDisplay.equals(dataField.getText()))
			{
				dataField.clear();
				dataField.appendText(textToDisplay);
				dataField.setStyleClass(0, dataField.getText().length(), "messageText");
			
				if (searchOptions != null && searchOptions.getSearchValue().length() > 0)
				{
					final String textToSearch = searchOptions.isMatchCase() ? dataField.getText() : dataField.getText().toLowerCase();
					
					int pos = textToSearch.indexOf(searchOptions.getSearchValue());
					while (pos >= 0)
					{
						dataField.setStyleClass(pos, pos + searchOptions.getSearchValue().length(), "messageTextHighlighted");
						pos = textToSearch.indexOf(searchOptions.getSearchValue(), pos + 1);
					}
				}
				dataField.positionCaret(0);;
				
				updateTooltipText();
			}						
		}
	}
	
	private void updateTooltipText()
	{
		if (selectionFormat != null)
		{
			final String tooltipText = FormattingUtils.checkAndFormatText(selectionFormat, dataField.getSelectedText());
			
			if (tooltipText.length() > 0)
			{
				tooltip.setText(tooltipText);
			}
			else
			{
				tooltip.setText("[select text to convert]");
			}
		}
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setSearchOptions(final SearchOptions searchOptions)
	{
		this.searchOptions = searchOptions;
	}
	
	public void setStore(final BasicMessageStore store)
	{
		this.store = store;
	}
}
