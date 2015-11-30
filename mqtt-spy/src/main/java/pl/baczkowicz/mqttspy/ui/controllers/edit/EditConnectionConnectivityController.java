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
package pl.baczkowicz.mqttspy.ui.controllers.edit;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.ReconnectionSettings;
import pl.baczkowicz.mqttspy.configuration.ConfigurationUtils;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.ui.EditConnectionController;
import pl.baczkowicz.mqttspy.utils.ConnectionUtils;
import pl.baczkowicz.mqttspy.utils.MqttUtils;
import pl.baczkowicz.spy.ui.keyboard.KeyboardUtils;

/**
 * Controller for editing a single connection - connectivity tab.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EditConnectionConnectivityController extends AnchorPane implements Initializable, EditConnectionSubController
{
	private final static Logger logger = LoggerFactory.getLogger(EditConnectionConnectivityController.class);

	// Connectivity
	
	@FXML
	private TextField brokerAddressText;
	
	@FXML
	private CheckBox reconnect;
		
	@FXML
	private TextField reconnectionInterval;
	
	@FXML
	private CheckBox resubscribe;

	@FXML
	private TextField clientIdText;

	@FXML
	private Button addTimestampButton;
	
	@FXML
	private Label lengthLabel;
	
	@FXML
	private TextField connectionTimeout;
	
	@FXML
	private TextField keepAlive;
	
	@FXML
	private CheckBox cleanSession;
	
	// Other fields

	private final ChangeListener basicOnChangeListener = new ChangeListener()
	{
		@Override
		public void changed(ObservableValue observable, Object oldValue, Object newValue)
		{
			onChange();			
		}		
	};

	/** The parent controller. */
	private EditConnectionController parent;
	
	// ===============================
	// === Initialisation ============
	// ===============================

	public void initialize(URL location, ResourceBundle resources)
	{	
		brokerAddressText.textProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{				
				parent.updateConnectionName();
				
				onChange();
			}		
		});
		
		clientIdText.textProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				parent.updateConnectionName();
				
				onChange();
			}		
		});
		
		cleanSession.selectedProperty().addListener(basicOnChangeListener);
		
		connectionTimeout.textProperty().addListener(basicOnChangeListener);
		connectionTimeout.addEventFilter(KeyEvent.KEY_TYPED, KeyboardUtils.nonNumericKeyConsumer);
		keepAlive.textProperty().addListener(basicOnChangeListener);
		keepAlive.addEventFilter(KeyEvent.KEY_TYPED, KeyboardUtils.nonNumericKeyConsumer);
		
		reconnect.selectedProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				updateReconnection();
				
				onChange();
			}		
		});
		reconnectionInterval.textProperty().addListener(basicOnChangeListener);
		reconnectionInterval.addEventFilter(KeyEvent.KEY_TYPED, KeyboardUtils.nonNumericKeyConsumer);
		resubscribe.selectedProperty().addListener(basicOnChangeListener);
	}

	public void init()
	{
		// Nothing to do here	
	}

	// ===============================
	// === FXML ======================
	// ===============================

	@FXML
	private void addTimestamp()
	{
		updateClientId(true);
	}	
	
	public boolean updateClientId(final boolean addTimestamp)
	{
		String clientId = clientIdText.getText();
		String newClientId = clientId;
		
		if (MqttUtils.limitClientId(parent.getEditedConnectionDetails().getProtocol()) 
				&& clientId.length() > MqttUtils.MAX_CLIENT_LENGTH_FOR_3_1)
		{
			newClientId = clientId.substring(0, MqttUtils.MAX_CLIENT_LENGTH_FOR_3_1);
		}
		
		if (addTimestamp)
		{
			newClientId = MqttUtils.generateClientIdWithTimestamp(newClientId, parent.getEditedConnectionDetails().getProtocol());
		}
		
		if (!clientId.equals(newClientId))
		{
			final int currentCurrentPosition = clientIdText.getCaretPosition();
			clientIdText.setText(newClientId);
			clientIdText.positionCaret(currentCurrentPosition);
			return true;
		}
		
		return false;
	}

	public void updateClientIdLength()
	{				
		if (MqttUtils.limitClientId(parent.getEditedConnectionDetails().getProtocol()))
		{
			lengthLabel.setText("Length = " + clientIdText.getText().length() + "/" + MqttUtils.MAX_CLIENT_LENGTH_FOR_3_1);
		}
		else
		{
			lengthLabel.setText("Length = " + clientIdText.getText().length());
		}
	}

	// ===============================
	// === Logic =====================
	// ===============================

	private void onChange()
	{
		parent.onChange();
	}

	@Override
	public UserInterfaceMqttConnectionDetails readValues(final UserInterfaceMqttConnectionDetails connection)
	{
		final List<String> serverURIs = Arrays.asList(brokerAddressText.getText().split(ConnectionUtils.SERVER_DELIMITER));
		for (final String serverURI : serverURIs)
		{
			logger.trace("Adding " + serverURI);
			// Trim and remove any prefixes - these are done dynamically based on SSL mode
			connection.getServerURI().add(serverURI.trim().replaceAll(MqttUtils.TCP_PREFIX, "").replaceAll(MqttUtils.SSL_PREFIX, ""));
		}
		if (brokerAddressText.getText().endsWith(ConnectionUtils.SERVER_DELIMITER))
		{
			logger.trace("Adding empty");
			connection.getServerURI().add("");
		}
		
		connection.setClientID(clientIdText.getText());
		
		connection.setCleanSession(cleanSession.isSelected());
		connection.setConnectionTimeout(Integer.valueOf(connectionTimeout.getText()));
		connection.setKeepAliveInterval(Integer.valueOf(keepAlive.getText()));
		
		if (reconnect.isSelected())
		{
			connection.setReconnectionSettings(
					new ReconnectionSettings(
							Integer.valueOf(reconnectionInterval.getText()), 
							resubscribe.isSelected()));
		}
		
		return connection;
	}
	
	public void updateReconnection()
	{
		if (reconnect.isSelected())
		{
			reconnectionInterval.setDisable(false);
			if (reconnectionInterval.getText().length() == 0)
			{
				reconnectionInterval.setText(String.valueOf(ConfigurationUtils.DEFAULT_RECONNECTION_INTERVAL));
			}
			resubscribe.setDisable(false);
		}
		else
		{
			reconnectionInterval.setDisable(true);
			resubscribe.setDisable(true);
		}
	}
	
	@Override
	public void displayConnectionDetails(final ConfiguredConnectionDetails connection)
	{	
		// Connectivity			
		
		brokerAddressText.setText(ConnectionUtils.serverURIsToString(connection.getServerURI()));
		clientIdText.setText(connection.getClientID());
				
		connectionTimeout.setText(connection.getConnectionTimeout().toString());
		keepAlive.setText(connection.getKeepAliveInterval().toString());
		cleanSession.setSelected(connection.isCleanSession());
		
		reconnect.setSelected(connection.getReconnectionSettings() != null);
		if (connection.getReconnectionSettings() != null)
		{
			reconnect.setSelected(true);
			reconnectionInterval.setText(String.valueOf(connection.getReconnectionSettings().getRetryInterval()));
			resubscribe.setSelected(connection.getReconnectionSettings().isResubscribe());
		}
		else
		{
			reconnect.setSelected(false);
			reconnectionInterval.setText("");
			resubscribe.setSelected(false);
		}
		
		updateReconnection();
	}		

	// ===============================
	// === Setters and getters =======
	// ===============================
	
	@Override
	public void setParent(final EditConnectionController controller)
	{
		this.parent = controller;
	}
	
	public TextField getClientIdText()	
	{
		return clientIdText;
	}
	
	public TextField getBrokerAddressText()
	{
		return brokerAddressText;
	}
}
