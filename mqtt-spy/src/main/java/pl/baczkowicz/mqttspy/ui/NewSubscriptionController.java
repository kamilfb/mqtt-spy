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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.generated.TabbedSubscriptionDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.exceptions.MqttSpyException;
import pl.baczkowicz.mqttspy.ui.connections.ConnectionManager;
import pl.baczkowicz.mqttspy.ui.keyboard.TimeBasedKeyEventFilter;
import pl.baczkowicz.mqttspy.ui.panes.TitledPaneController;
import pl.baczkowicz.mqttspy.ui.panes.TitledPaneStatus;
import pl.baczkowicz.mqttspy.ui.utils.DialogUtils;
import pl.baczkowicz.mqttspy.utils.MqttUtils;

/**
 * Controller for creating new subscriptions.
 */
public class NewSubscriptionController implements Initializable, TitledPaneController
{
	final static Logger logger = LoggerFactory.getLogger(NewSubscriptionController.class);
	
	@FXML
	private Button subscribeButton;

	@FXML
	private ComboBox<String> subscriptionTopicText;
	@FXML
	
	private ChoiceBox<String> subscriptionQosChoice;

	@FXML
	private Label subscriptionQosLabel;
	
	@FXML
	private ColorPicker subscriptionColorPicker;

	private ObservableList<String> subscriptionTopics = FXCollections.observableArrayList();

	private MqttAsyncConnection connection;

	private List<Color> colors = new ArrayList<Color>();

	private ConnectionController connectionController;

	private boolean connected;

	private ConnectionManager connectionManager;

	private boolean detailedView;

	private TimeBasedKeyEventFilter timeBasedFilter;
	
	/** Created pane status with index 2 (second the last pane). */
	private final TitledPaneStatus paneStatus = new TitledPaneStatus(2);

	private TitledPane pane;

	public NewSubscriptionController()
	{
		// TODO: subscription colors - move that to a property file
		// 8
		colors.add(Color.valueOf("f9d900"));
		colors.add(Color.valueOf("a9e200"));
		colors.add(Color.valueOf("22bad9"));
		colors.add(Color.valueOf("0181e2"));
		colors.add(Color.valueOf("2f357f"));
		colors.add(Color.valueOf("860061"));
		colors.add(Color.valueOf("c62b00"));
		colors.add(Color.valueOf("ff5700"));

		// 8
		colors.add(Color.valueOf("f9d950"));
		colors.add(Color.valueOf("a9e250"));
		colors.add(Color.valueOf("22baa9"));
		colors.add(Color.valueOf("018122"));
		colors.add(Color.valueOf("2f351f"));
		colors.add(Color.valueOf("8600F1"));
		colors.add(Color.valueOf("c62b60"));
		colors.add(Color.valueOf("ff5760"));
	}

	public void initialize(URL location, ResourceBundle resources)
	{
		timeBasedFilter = new TimeBasedKeyEventFilter(100);
		
		subscriptionColorPicker.setValue(colors.get(0));
		subscriptionTopicText.setItems(subscriptionTopics);
		
		subscriptionTopicText.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() 
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
		        			subscribe();
		    	        	keyEvent.consume();
		        		}
		        		break;
		        	}		        	
		        	default:
		        		break;
	        	}
	        }
	    });
	}
	
	private void updateVisibility()
	{
		if (detailedView)
		{
			AnchorPane.setRightAnchor(subscriptionTopicText, 327.0);
			subscriptionQosChoice.setVisible(true);
			subscriptionQosLabel.setVisible(true);
		}
		else
		{
			AnchorPane.setRightAnchor(subscriptionTopicText, 244.0);
			subscriptionQosChoice.setVisible(false);
			subscriptionQosLabel.setVisible(false);
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
	
	public void setConnected(final boolean connected)
	{
		this.connected = connected;
		this.subscribeButton.setDisable(!connected);
		this.subscriptionTopicText.setDisable(!connected);
	}

	public boolean recordSubscriptionTopic(final String subscriptionTopic)
	{
		return MqttUtils.recordTopic(subscriptionTopic, subscriptionTopics);
	}
	
	@FXML
	public void subscribe()
	{
		if (subscriptionTopicText.getValue() != null)
		{
			final String subscriptionTopic = subscriptionTopicText.getValue().toString();
			
			try
			{
				MqttUtils.validateTopic(subscriptionTopic);
			
				final TabbedSubscriptionDetails subscriptionDetails = new TabbedSubscriptionDetails();
				subscriptionDetails.setTopic(subscriptionTopic);
				subscriptionDetails.setQos(subscriptionQosChoice.getSelectionModel().getSelectedIndex());
							
				subscribe(subscriptionDetails, true);
			}
			catch (MqttSpyException e)
			{
				DialogUtils.showError("Invalid topic", "Provided topic is not valid. " + e.getMessage());
			}
		}
		else
		{
			DialogUtils.showError("Invalid topic", "Cannot subscribe to an empty topic.");
		}
	}	

	public void subscribe(final TabbedSubscriptionDetails subscriptionDetails, final boolean subscribe)
	{
		logger.info("Subscribing to " + subscriptionDetails.getTopic());
		if (!connection.getSubscriptions().keySet().contains(subscriptionDetails.getTopic()))		
		{
			recordSubscriptionTopic(subscriptionDetails.getTopic());
			
			connectionManager.getSubscriptionManager(connectionController).
				createSubscription(subscriptionColorPicker.getValue(), subscribe, subscriptionDetails, 
						connection, connectionController, this);
			
			subscriptionColorPicker.setValue(colors.get(connection.getLastUsedSubscriptionId() % 16));
		}
		else
		{
			DialogUtils.showError("Duplicate topic", "You already have a subscription tab with " + subscriptionDetails.getTopic() + " topic.");
		}
	}
	
	public void setConnectionManager(final ConnectionManager connectionManager)
	{
		this.connectionManager = connectionManager;
	}
	
	public void setConnectionController(ConnectionController connectionController)
	{
		this.connectionController = connectionController;
	}

	public void setConnection(MqttAsyncConnection connection)
	{
		this.connection = connection;
	}

	@Override
	public TitledPane getTitledPane()
	{
		return pane;
	}

	@Override
	public void setTitledPane(TitledPane pane)
	{
		this.pane = pane;
	}

	@Override
	public TitledPaneStatus getTitledPaneStatus()
	{
		return paneStatus;
	}
}
