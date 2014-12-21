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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import org.controlsfx.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.events.EventManager;
import pl.baczkowicz.mqttspy.events.observers.ConnectionStatusChangeObserver;
import pl.baczkowicz.mqttspy.exceptions.ConfigurationException;
import pl.baczkowicz.mqttspy.ui.connections.ConnectionManager;
import pl.baczkowicz.mqttspy.ui.utils.DialogUtils;
import pl.baczkowicz.mqttspy.utils.ConnectionUtils;
import pl.baczkowicz.mqttspy.utils.MqttUtils;

/**
 * Controller for editing all connections.
 */
@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
public class EditConnectionsController extends AnchorPane implements Initializable, ConnectionStatusChangeObserver
{
	private final static String MODIFIED_ITEM = "* ";
	
	private final static Logger logger = LoggerFactory.getLogger(EditConnectionsController.class);

	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private EditConnectionController editConnectionPaneController;
	
	@FXML
	private ListView<String> connectionList;
	
	@FXML
	private Button newConnectionButton;
	
	@FXML
	private Button duplicateConnectionButton;
	
	@FXML
	private Button deleteConnectionButton;
	
	@FXML
	private Button importConnectionsButton;
	
	@FXML
	private Button applyAllButton;
	
	@FXML
	private Button undoAllButton;
	
	private MainController mainController;

	private ConfigurationManager configurationManager;

	private List<ConfiguredConnectionDetails> connections = new ArrayList<ConfiguredConnectionDetails>();

	private EventManager eventManager;

	private ConnectionManager connectionManager;

	// ===============================
	// === Initialisation ============
	// ===============================
	
	private void showSelected()
	{
		synchronized (connections)
		{
			updateSelected();
		}
	}
	
	private void updateSelected()	
	{
		if (connectionList.getItems().size() > 0 && getSelectedIndex() == -1)
		{
			selectFirst();
			return;
		}
		
		if (connectionList.getItems().isEmpty())
		{
			duplicateConnectionButton.setDisable(true);
			deleteConnectionButton.setDisable(true);
			editConnectionPaneController.setEmptyConnectionListMode(true);
		}
		else 
		{
			deleteConnectionButton.setDisable(false);
			duplicateConnectionButton.setDisable(false);
			editConnectionPaneController.setEmptyConnectionListMode(false);
			
			if (!connections.get(getSelectedIndex()).isBeingCreated())
			{
				logger.trace("Editing connection {}", connections.get(getSelectedIndex()).getName());
				
				editConnectionPaneController.setRecordModifications(false);
				editConnectionPaneController.editConnection(connections.get(getSelectedIndex()));
				editConnectionPaneController.setRecordModifications(true);							
			}
		}
	}
	
	public void initialize(URL location, ResourceBundle resources)
	{
		duplicateConnectionButton.setDisable(true);
		deleteConnectionButton.setDisable(true);
		
		connectionList.getStyleClass().add("connectionList");
		connectionList.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				showSelected();
			}
		});
		connectionList.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent mouseEvent)
			{
				if (mouseEvent.getButton().equals(MouseButton.PRIMARY))
				{
					if (mouseEvent.getClickCount() == 2)
					{
						try
						{
							if (getSelectedIndex() >= 0)
							{
								editConnectionPaneController.createConnection();
							}
						}
						catch (ConfigurationException e)
						{
							logger.error("Cannot create connection", e);
						}
					}
				}
			}
		});
	}	
		
	public void init()
	{		
		connections = configurationManager.getConnections();		
		eventManager.registerConnectionStatusObserver(this, null);
		
		editConnectionPaneController.setConfigurationManager(configurationManager);
		editConnectionPaneController.setConnectionManager(connectionManager);
		editConnectionPaneController.setMainController(mainController);
		editConnectionPaneController.setEditConnectionsController(this);
		editConnectionPaneController.init();
		
		editConnectionPaneController.getConnectionName().textProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				if (editConnectionPaneController.isRecordModifications())
				{
					connectionNameChanged();
				}
			}
		
		});
		
		editConnectionPaneController.setRecordModifications(false);
		listConnections();
		editConnectionPaneController.setRecordModifications(true);
	}
	
	// ===============================
	// === FXML ======================
	// ===============================

	@FXML
	public void newConnection()
	{
		final UserInterfaceMqttConnectionDetails baseConnection = new UserInterfaceMqttConnectionDetails();				
		baseConnection.getServerURI().add("127.0.0.1");
		baseConnection.setClientID(MqttUtils.generateClientIdWithTimestamp(System.getProperty("user.name")));
		baseConnection.setName(ConnectionUtils.composeConnectionName(baseConnection.getClientID(), baseConnection.getServerURI()));
		baseConnection.setAutoConnect(true);
		
		final ConfiguredConnectionDetails connection = new ConfiguredConnectionDetails(
				configurationManager.getConnectionIdGenerator().getNextAvailableId(), true, true, true, baseConnection);
		
		connections.add(connection);
		newConnectionMode(connection);
	}
	
	@FXML
	private void duplicateConnection()
	{
		final ConfiguredConnectionDetails connection = new ConfiguredConnectionDetails(
				configurationManager.getConnectionIdGenerator().getNextAvailableId(), true, true, true, connections.get(getSelectedIndex()));		
		connections.add(connection);
		newConnectionMode(connection);
	}
	
	@FXML
	private void deleteConnection()
	{
		connections.get(getSelectedIndex()).setDeleted(true);
		
		final String connectionName = connections.get(getSelectedIndex()).getName();
		
		if (DialogUtils.showDeleteQuestion(connectionName) == Dialog.ACTION_YES)
		{	
			editConnectionPaneController.setRecordModifications(false);
			connections.remove(getSelectedIndex());			
			listConnections();			
			selectFirst();
			editConnectionPaneController.setRecordModifications(true);
				
			logger.debug("Saving all connections");
			if (configurationManager.saveConfiguration())
			{
				// TODO: for some reason, this is not shown
				DialogUtils.showTooltip(deleteConnectionButton, "Connection " + connectionName + " deleted.");
			}
		}
	}
	
	@FXML
	private void undoAll()
	{
		for (final ConfiguredConnectionDetails connection : connections)
		{
			connection.undo();
		}
		
		listConnections();
	}
	
	@FXML
	private void applyAll()
	{
		for (final ConfiguredConnectionDetails connection : connections)
		{
			connection.apply();
		}
		
		listConnections();
		
		logger.debug("Saving all connections");
		if (configurationManager.saveConfiguration())
		{
			DialogUtils.showTooltip(applyAllButton, "Changes for all connections have been saved.");
		}
	}
	
	@FXML
	private void importConnections()
	{
		// TODO: import
	}
	
	// ===============================
	// === Logic =====================
	// ===============================

	private void selectFirst()
	{
		// Select the first item if any connections present
		if (connectionList.getItems().size() > 0)
		{
			connectionList.getSelectionModel().select(0);
		}
	}
	
	private void selectLast()
	{
		connectionList.getSelectionModel().select(connectionList.getItems().size() - 1);
	}
	
	private int getSelectedIndex()
	{
		return connectionList.getSelectionModel().getSelectedIndex();
	}
	
	public void listConnections()
	{
		final int selected = getSelectedIndex();

		applyAllButton.setDisable(true);
		undoAllButton.setDisable(true);
		
		// Adjust the list size
		while (connectionList.getItems().size() > connections.size())
		{
			connectionList.getItems().remove(connectionList.getItems().size() - 1);
		}
		while (connectionList.getItems().size() < connections.size())
		{
			connectionList.getItems().add("");
		}
		
		for (int i = 0; i < connections.size(); i++)
		{	
			final ConfiguredConnectionDetails connection = connections.get(i);
			
			if (connection.isModified())
			{
				connectionList.getItems().set(i, MODIFIED_ITEM + connection.getName());
				applyAllButton.setDisable(false);
				undoAllButton.setDisable(false);
			}
			else
			{
				connectionList.getItems().set(i, connection.getName());
			}
			
			// Apply styling
			// MqttConnectionStatus status = null;
			// boolean opened = false;
			// for (final MqttConnection openedConnection :
			// mqttManager.getConnections())
			// {
			// if (connection.getId() ==
			// openedConnection.getProperties().getId())
			// {
			// status = openedConnection.getConnectionStatus();
			// opened = openedConnection.isOpened();
			// }
			// }
			//final S lastItem = connectionLis
			// if (status != null && opened)
			// {
			// .getStyleClass().add(StylingUtils.getStyleForMqttConnectionStatus(status));
			//
			// }
			// else
			// {
			// .getStyleClass().add(StylingUtils.getStyleForMqttConnectionStatus(null));
			// }
		}
		
		// Reselect
		if (selected >= 0)
		{
			connectionList.getSelectionModel().select(selected);
		}
		else
		{
			selectFirst();
		}
		updateSelected();
		
		if (connectionList.getItems().size() > 0)
		{			
			deleteConnectionButton.setDisable(false);
		}
		else
		{
			deleteConnectionButton.setDisable(true);
			duplicateConnectionButton.setDisable(true);
		}
	}	
	
	protected void connectionNameChanged()
	{
		if (getSelectedIndex() >= 0)
		{
			final String newName = editConnectionPaneController.getConnectionName().getText();
			connections.get(getSelectedIndex()).setName(newName);
			listConnections();
		}
	}
	
	private void newConnectionMode(final ConfiguredConnectionDetails createdConnection)
	{	
		editConnectionPaneController.setRecordModifications(false);		
		listConnections();
		selectLast();
		editConnectionPaneController.editConnection(createdConnection);
		editConnectionPaneController.setRecordModifications(true);
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================

	public void setMainController(MainController mainController)
	{
		this.mainController = mainController;
	}
	
	public void setConfigurationManager(final ConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}	

	public void setEventManager(final EventManager eventManager)
	{
		this.eventManager = eventManager;		
	}

	@Override
	public void onConnectionStatusChanged(final MqttAsyncConnection changedConnection)
	{
		if (getSelectedIndex() >= 0)
		{
			showSelected();
		}
	}
	
	public void setConnectionManager(final ConnectionManager connectionManager)
	{
		this.connectionManager = connectionManager;
	}
}
