/***********************************************************************************
 * 
 * Copyright (c) 2014 Kamil Baczkowicz
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import org.controlsfx.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.ProtocolEnum;
import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.ConnectionGroup;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.exceptions.ConfigurationException;
import pl.baczkowicz.mqttspy.ui.connections.ConnectionManager;
import pl.baczkowicz.mqttspy.ui.controls.DragAndDropTreeViewCell;
import pl.baczkowicz.mqttspy.ui.events.EventManager;
import pl.baczkowicz.mqttspy.ui.events.observers.ConnectionStatusChangeObserver;
import pl.baczkowicz.mqttspy.ui.properties.ConnectionListItemProperties;
import pl.baczkowicz.mqttspy.ui.utils.DialogUtils;
import pl.baczkowicz.mqttspy.utils.ConnectionUtils;
import pl.baczkowicz.mqttspy.utils.MqttUtils;

/**
 * Controller for editing all connections.
 */
@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
public class EditConnectionsController extends AnchorPane implements Initializable, ConnectionStatusChangeObserver
{
	public final static String MODIFIED_ITEM = "* ";
	
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(EditConnectionsController.class);

	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private EditConnectionController editConnectionPaneController;
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private EditConnectionGroupController editConnectionGroupPaneController;
	
	@FXML
	private AnchorPane connectionDetailsPane;
	
	//private AnchorPane editConnectionPane;
	
	@FXML
	private TreeView<ConnectionListItemProperties> connectionList;
	
	//@FXML
	//private Button newConnectionButton;
	
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
	
	int lastUserId = 0;
	
	final ConnectionListItemProperties rootProperties = new ConnectionListItemProperties(lastUserId++);
	
	final TreeItem<ConnectionListItemProperties> rootItem = new TreeItem<ConnectionListItemProperties>(rootProperties);
	
	private List<ConnectionGroup> groups;

	@FXML
	private Node editConnectionPane;

	@FXML
	private Node editConnectionGroupPane;

	// ===============================
	// === Initialisation ============
	// ===============================
	
	public void initialize(URL location, ResourceBundle resources)
	{
		//editConnectionPane = (AnchorPane) connectionDetailsPane.getChildren().get(0);
		connectionList.setCellFactory(new Callback<TreeView<ConnectionListItemProperties>, TreeCell<ConnectionListItemProperties>>() 
		{
            @Override
            public TreeCell call(TreeView<ConnectionListItemProperties> param) 
            {
                return new DragAndDropTreeViewCell(param);
            }
        });
		
		duplicateConnectionButton.setDisable(true);
		deleteConnectionButton.setDisable(true);
		
		connectionList.setShowRoot(false);
		connectionList.setRoot(rootItem);		
		
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
							if (getSelectedItem() != null)
							{
								// Open the connection
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
	
	private void populateConnections(
			final List<ConnectionGroup> groupList, 
			final List<ConfiguredConnectionDetails> connectionList)
	{
		rootItem.getChildren().clear();
		rootProperties.getChildren().clear();
		
		final List<ConnectionListItemProperties> groupProperties = new ArrayList<>();
		final List<ConnectionListItemProperties> connectionProperties = new ArrayList<>();
		
		for (final ConnectionGroup group : groupList)
		{
			final ConnectionListItemProperties properties = new ConnectionListItemProperties(lastUserId++);
			properties.setConnectionGroup(group);
			
			groupProperties.add(properties);
		}
		
		for (final ConnectionListItemProperties item : groupProperties)
		{
			findParentForGroup(groupProperties, item, rootProperties);
		}
		
		for (final ConfiguredConnectionDetails connection : connectionList)
		{
			final ConnectionListItemProperties properties = new ConnectionListItemProperties(lastUserId++);
			properties.setConnection(connection);
			
			connectionProperties.add(properties);
		}
		
		for (final ConnectionListItemProperties item : connectionProperties)
		{
			findParentForConnection(connectionProperties, item, groupProperties.get(0));
		}
		
		addToTree(rootItem, rootProperties);
	}
	
	private boolean addToTree(TreeItem<ConnectionListItemProperties> treeItem, ConnectionListItemProperties properties)
	{
		boolean added = false;
		for (final ConnectionListItemProperties item : properties.getChildren()) 
		{
			final TreeItem<ConnectionListItemProperties> newTreeItem = new TreeItem<ConnectionListItemProperties>(item);						
			
			if (item.isGroup())
			{
				if (addToTree(newTreeItem, item))			
				{
					newTreeItem.setExpanded(true);
				}			
			}
			
			treeItem.getChildren().add(newTreeItem);
			added = true;
		}
		
		return added;
	}
	
	private void findParentForGroup(
			final List<ConnectionListItemProperties> groupProperties, 
			final ConnectionListItemProperties group, 
			final ConnectionListItemProperties defaultParent)
	{
		if (group.getConnectionGroup().getParent() == null)
		{
			group.setParent(defaultParent);
			defaultParent.addChild(group);
		}
		else
		{		
			for (final ConnectionListItemProperties item : groupProperties)
			{
				if (group.getConnectionGroup().getParent().equals(item.getConnectionGroup()))
				{
					group.setParent(item);
					item.addChild(group);
				}
			}
		}
	}
	
	private void findParentForConnection(
			final List<ConnectionListItemProperties> connectionProperties, 
			final ConnectionListItemProperties connection, 
			final ConnectionListItemProperties defaultParent)
	{
		if (connection.getConnection().getConnectionGroup() == null)
		{
			connection.setParent(defaultParent);
			defaultParent.addChild(connection);
		}
		else
		{		
			for (final ConnectionListItemProperties item : connectionProperties)
			{
				if (connection.getConnection().getConnectionGroup().equals(item.getConnectionGroup()))
				{
					connection.setParent(item);
					item.addChild(connection);
				}
			}
		}
	}
	
	private void showSelected()
	{
		synchronized (connections)
		{
			updateSelected();
		}
	}
	
	public void updateSelected()	
	{
		if (getSelectedItem() == null)
		// if (connectionList.getItems().size() > 0 && getSelectedIndex() == -1)
		{
			selectFirst();
			return;
		}
		
		if (connections.isEmpty())
		// if (connectionList.getItems().isEmpty())
		{
			duplicateConnectionButton.setDisable(true);
			deleteConnectionButton.setDisable(true);
			editConnectionPaneController.setEmptyConnectionListMode(true);
		}
		else 
		{
			deleteConnectionButton.setDisable(false);
			duplicateConnectionButton.setDisable(getSelectedItem().isGroup());
			editConnectionPaneController.setEmptyConnectionListMode(false);
			
			if (getSelectedItem().isGroup())
			{
				editConnectionPane.setVisible(false);
				editConnectionGroupPane.setVisible(true);
			}
			else if (!getSelectedItem().isGroup() && !getSelectedItem().getConnection().isBeingCreated())
			{
				editConnectionPane.setVisible(true);
				editConnectionGroupPane.setVisible(false);
				
				logger.trace("Editing connection {}", getSelectedItem().getName());
				
				editConnectionPaneController.setRecordModifications(false);
				editConnectionPaneController.editConnection(getSelectedItem().getConnection());
				editConnectionPaneController.setRecordModifications(true);							
			}
		}
	}
		
	public void init()
	{		
		connections = configurationManager.getConnections();
		groups = configurationManager.getConnectionGrops();
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
	public void newMqttConnection()
	{
		final UserInterfaceMqttConnectionDetails baseConnection = new UserInterfaceMqttConnectionDetails();				
		baseConnection.getServerURI().add("127.0.0.1");
		baseConnection.setClientID(MqttUtils.generateClientIdWithTimestamp(System.getProperty("user.name"), ProtocolEnum.MQTT_DEFAULT));
		baseConnection.setName(ConnectionUtils.composeConnectionName(baseConnection.getClientID(), baseConnection.getServerURI()));
		baseConnection.setAutoConnect(true);
		
		final ConfiguredConnectionDetails connection = new ConfiguredConnectionDetails(
				configurationManager.getConnectionIdGenerator().getNextAvailableId(), true, true, true, baseConnection);
		
		connections.add(connection);
		newConnectionMode(connection);
	}
	
	@FXML
	private void newGroup()
	{
		final Optional<String> result = DialogUtils.askForInput(
				connectionList.getScene().getWindow(), "New connection group", "Please enter the connection group name: ");
		
		if (result.isPresent())
		{
			// TODO: ID needs to be unique
			groups.add(new ConnectionGroup("cg", result.get(), null));
			populateConnections(groups, connections);
		}
	}
	
	@FXML
	private void duplicateConnection()
	{
		final ConfiguredConnectionDetails connection = new ConfiguredConnectionDetails(
				configurationManager.getConnectionIdGenerator().getNextAvailableId(), 
				true, true, true, getSelectedItem().getConnection());		
		connections.add(connection);
		newConnectionMode(connection);
	}
	
	@FXML
	private void deleteConnection()
	{
		getSelectedItem().getConnection().setDeleted(true);
		
		final String connectionName = getSelectedItem().getConnection().getName();
		
		if (DialogUtils.showDeleteQuestion(connectionName) == Dialog.ACTION_YES)
		{	
			editConnectionPaneController.setRecordModifications(false);
			connections.remove(getSelectedItem().getConnection());			
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
		if (connections.size() > 0)
		// if (connectionList.getItems().size() > 0)
		{
			connectionList.getSelectionModel().select(0);
		}
	}
	
	private void selectLast()
	{
		// TODO: tree
		// connectionList.getSelectionModel().select(connectionList.getItems().size() - 1);
	}
	
	private ConnectionListItemProperties getSelectedItem()
	{
		if (connectionList.getSelectionModel().getSelectedItem() == null)
		{
			return null;
		}
		
		return connectionList.getSelectionModel().getSelectedItem().getValue();
	}
	
//	private int getSelectedIndex()
//	{
//		return connectionList.getSelectionModel().getSelectedIndex();
//	}
	
	// TODO: pass extra parameter whether to recreate to just update text?
	public void listConnections()
	{
		final TreeItem<ConnectionListItemProperties> selected = connectionList.getSelectionModel().getSelectedItem();

		applyAllButton.setDisable(true);
		undoAllButton.setDisable(true);
		
		populateConnections(groups, connections);
		// Adjust the list size
		// TODO: tree
//		while (ungroupedItem.getChildren().size() > connections.size())
//		{
//			ungroupedItem.getChildren().remove(ungroupedItem.getChildren().size() - 1);
//			//connectionList.getItems().remove(connectionList.getItems().size() - 1);
//		}
//		
//		while (ungroupedItem.getChildren().size() < connections.size())
//		{
//			ungroupedItem.getChildren().add(new TreeItem(""));
//			//connectionList.getItems().add("");
//		}
		
		for (int i = 0; i < connections.size(); i++)
		{	
			final ConfiguredConnectionDetails connection = connections.get(i);
			
			if (connection.isModified())
			{
				// TODO:
				//ungroupedItem.getChildren().set(i, new TreeItem(MODIFIED_ITEM + connection.getName()));
				// connectionList.getItems().set(i, MODIFIED_ITEM + connection.getName());
				applyAllButton.setDisable(false);
				undoAllButton.setDisable(false);
			}
			else
			{
				// ungroupedItem.getChildren().set(i, new TreeItem(connection.getName()));
				// connectionList.getItems().set(i, connection.getName());
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
		if (getSelectedItem() != null)
		{
			connectionList.getSelectionModel().select(selected);
		}
		else
		{
			selectFirst();
		}
		updateSelected();
		
		// if (connectionList.getItems().size() > 0)
		if (connections.size() > 0)
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
		if (getSelectedItem() != null)
		{
			final String newName = editConnectionPaneController.getConnectionName().getText();
			getSelectedItem().getConnection().setName(newName);
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
		if (getSelectedItem() != null)
		{
			showSelected();
		}
	}
	
	public void setConnectionManager(final ConnectionManager connectionManager)
	{
		this.connectionManager = connectionManager;
	}
}
