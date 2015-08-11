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
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionGroupDetails;
import pl.baczkowicz.mqttspy.configuration.generated.ConnectionGroup;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.exceptions.ConfigurationException;
import pl.baczkowicz.mqttspy.ui.connections.ConnectionManager;
import pl.baczkowicz.mqttspy.ui.controls.DragAndDropTreeViewCell;
import pl.baczkowicz.mqttspy.ui.events.EventManager;
import pl.baczkowicz.mqttspy.ui.events.observers.ConnectionStatusChangeObserver;
import pl.baczkowicz.mqttspy.ui.properties.ConnectionTreeItemProperties;
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
	
	@FXML
	private TreeView<ConnectionTreeItemProperties> connectionList;
	
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
	
	int lastUsedId = 0;
	
	final ConnectionTreeItemProperties rootProperties = new ConnectionTreeItemProperties(lastUsedId++);
	
	final TreeItem<ConnectionTreeItemProperties> rootItem = new TreeItem<ConnectionTreeItemProperties>(rootProperties);
	
	private List<ConfiguredConnectionGroupDetails> groups;

	@FXML
	private Node editConnectionPane;

	@FXML
	private Node editConnectionGroupPane;

	// ===============================
	// === Initialisation ============
	// ===============================
	
	public void initialize(URL location, ResourceBundle resources)
	{
		connectionList.setCellFactory(new Callback<TreeView<ConnectionTreeItemProperties>, TreeCell<ConnectionTreeItemProperties>>() 
		{
            @Override
            public TreeCell call(TreeView<ConnectionTreeItemProperties> param) 
            {
                return new DragAndDropTreeViewCell(param);
            }
        });
		
		duplicateConnectionButton.setDisable(true);
		deleteConnectionButton.setDisable(true);
		
		connectionList.setShowRoot(true);
		connectionList.setRoot(rootItem);
		rootItem.setExpanded(true);
		rootItem.expandedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue)
			{				
				if (!rootItem.isExpanded())
				{
					rootItem.setExpanded(true);
				}				
			}
		});
		rootProperties.setConnectionGroup(new ConfiguredConnectionGroupDetails(
				new ConnectionGroup(ConfigurationManager.DEFAULT_GROUP, "All connections", null), false));
		
		connectionList.getStyleClass().add("connectionList");
		connectionList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				logger.debug("Item selected = " + newValue);
				if (newValue == null)
				{					
					return;
				}
				
				// showSelected();
				updateUIForSelectedItem(((TreeItem<ConnectionTreeItemProperties>) newValue).getValue());
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
							if (getSelectedItem() != null && !getSelectedItem().isGroup())
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
	
	public void init()
	{
		connections = configurationManager.getConnections();
		groups = configurationManager.getConnectionGrops();
		eventManager.registerConnectionStatusObserver(this, null);

		editConnectionGroupPaneController.setMainController(mainController);
		editConnectionGroupPaneController.setEditConnectionsController(this);
		editConnectionGroupPaneController.init();

		editConnectionPaneController
				.setConfigurationManager(configurationManager);
		editConnectionPaneController.setConnectionManager(connectionManager);
		editConnectionPaneController.setMainController(mainController);
		editConnectionPaneController.setEditConnectionsController(this);
		editConnectionPaneController.init();

		editConnectionPaneController.getConnectionName().textProperty()
				.addListener(new ChangeListener()
				{
					@Override
					public void changed(ObservableValue observable,
							Object oldValue, Object newValue)
					{
						if (editConnectionPaneController
								.isRecordModifications())
						{
							connectionNameChanged();
						}
					}

				});

		editConnectionPaneController.setRecordModifications(false);
		listConnections();
		editConnectionPaneController.setRecordModifications(true);
	}

	private void populateConnections(
			final List<ConfiguredConnectionGroupDetails> groups, 
			final List<ConfiguredConnectionDetails> connectionList)
	{
		rootItem.getChildren().clear();
		rootProperties.getChildren().clear();
		
		final List<ConnectionTreeItemProperties> groupProperties = new ArrayList<>();
		final List<ConnectionTreeItemProperties> connectionProperties = new ArrayList<>();
		
		for (final ConfiguredConnectionGroupDetails group : groups)
		{
			final ConnectionTreeItemProperties properties = new ConnectionTreeItemProperties(lastUsedId++);
			properties.setConnectionGroup(group);
			
			groupProperties.add(properties);
		}
		
		for (final ConnectionTreeItemProperties item : groupProperties)
		{
			findParentForGroup(groupProperties, item, rootProperties);
		}
		
		for (final ConfiguredConnectionDetails connection : connectionList)
		{
			final ConnectionTreeItemProperties properties = new ConnectionTreeItemProperties(lastUsedId++);
			properties.setConnection(connection);
			
			connectionProperties.add(properties);
		}
		
		for (final ConnectionTreeItemProperties item : connectionProperties)
		{
			findParentForConnection(connectionProperties, item, rootProperties);
		}
		
		addToTree(rootItem, rootProperties);
	}
	
	private boolean addToTree(TreeItem<ConnectionTreeItemProperties> treeItem, ConnectionTreeItemProperties properties)
	{
		boolean added = false;
		for (final ConnectionTreeItemProperties item : properties.getChildren()) 
		{
			final TreeItem<ConnectionTreeItemProperties> newTreeItem = new TreeItem<ConnectionTreeItemProperties>(item);						
			
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
			final List<ConnectionTreeItemProperties> groupProperties, 
			final ConnectionTreeItemProperties group, 
			final ConnectionTreeItemProperties defaultParent)
	{
		if (group.getConnectionGroup().getGroup().getParent() == null)
		{
			group.setParent(defaultParent);
			defaultParent.addChild(group);
		}
		else
		{		
			for (final ConnectionTreeItemProperties item : groupProperties)
			{
				if (group.getConnectionGroup().getGroup().getParent().equals(item.getConnectionGroup()))
				{
					group.setParent(item);
					item.addChild(group);
				}
			}
		}
	}
	
	private void findParentForConnection(
			final List<ConnectionTreeItemProperties> connectionProperties, 
			final ConnectionTreeItemProperties connection, 
			final ConnectionTreeItemProperties defaultParent)
	{
		if (connection.getConnection().getConnectionGroup() == null)
		{
			connection.setParent(defaultParent);
			defaultParent.addChild(connection);
		}
		else
		{		
			for (final ConnectionTreeItemProperties item : connectionProperties)
			{
				if (item.isGroup() && item.getConnectionGroup().getGroup().equals(connection.getConnection().getConnectionGroup()))
				{
					connection.setParent(item);
					item.addChild(connection);
				}
			}
		}
	}
	
//	private void showSelected()
//	{
//		synchronized (connections)
//		{
//			updateUIForSelectedItem();
//		}
//	}
	
	public void updateUIForSelectedItem()	
	{
		updateUIForSelectedItem(getSelectedItem());
	}
	
	public void updateUIForSelectedItem(final ConnectionTreeItemProperties selected)	
	{
		if (selected == null)
		{
			logger.debug("Selection is null");
			selectFirst();
			return;
		}
		
		if (connections.isEmpty() && groups.isEmpty())
		{
			duplicateConnectionButton.setDisable(true);
			deleteConnectionButton.setDisable(true);
			editConnectionPaneController.setEmptyConnectionListMode(true);
		}
		else 
		{
			// This this is not the default group (first one on the list)
			if (!selected.isGroup() 
					|| !selected.getConnectionGroup().getGroup().getID().equals(ConfigurationManager.DEFAULT_GROUP))
			{
				deleteConnectionButton.setDisable(false);
			}
			else
			{
				deleteConnectionButton.setDisable(true);
			}
						
			duplicateConnectionButton.setDisable(selected.isGroup());
			editConnectionPaneController.setEmptyConnectionListMode(false);
			
			editConnectionPane.setVisible(!selected.isGroup());
			editConnectionGroupPane.setVisible(selected.isGroup());
			
			if (selected.isGroup())
			{
				editConnectionGroupPaneController.setRecordModifications(false);
				editConnectionGroupPaneController.editConnectionGroup(selected.getConnectionGroup(), selected.getChildren());
				editConnectionGroupPaneController.setRecordModifications(true);			
			}
			else if (!selected.getConnection().isBeingCreated())
			{			
				logger.trace("Editing connection {}", selected.getName());
				
				editConnectionPaneController.setRecordModifications(false);
				editConnectionPaneController.editConnection(selected.getConnection());
				editConnectionPaneController.setRecordModifications(true);							
			}
		}
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
				configurationManager.getConnectionIdGenerator().getNextAvailableId(), true, true, baseConnection);
		
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
			final ConnectionGroup group = new ConnectionGroup("cg", result.get(), null); 
			final ConfiguredConnectionGroupDetails groupDetails = new ConfiguredConnectionGroupDetails(group, true);
			//final ConnectionTreeItemProperties properties = new ConnectionTreeItemProperties(lastUsedId++);
			//properties.setConnectionGroup(groupDetails);
			
			groups.add(groupDetails);
			
			//rootItem.getChildren().add(new TreeItem<ConnectionTreeItemProperties>(properties));
			
			// TODO: the new group needs to be marked as new
			populateConnections(groups, connections);
			selectGroup(groupDetails);
		}
	}
	
	@FXML
	private void duplicateConnection()
	{
		final ConfiguredConnectionDetails connection = new ConfiguredConnectionDetails(
				configurationManager.getConnectionIdGenerator().getNextAvailableId(), 
				true, true, getSelectedItem().getConnection());		
		connections.add(connection);
		newConnectionMode(connection);
	}
	
	@FXML
	private void deleteConnection()
	{
		final ConnectionTreeItemProperties selected = getSelectedItem();
		if (getSelectedItem().isGroup())
		{
			final int connectionCount = selected.getChildren().size();
			
			if (DialogUtils.showDeleteQuestion("Deleting connection group", 
					"Are you sure you want to delete connection group '" + selected.getName() + "'"
					+ (connectionCount == 0 ? "" : " and all subitems")
					+ "?") == Dialog.ACTION_YES)
			{
				// TODO: delete
			}
		}
		else
		{
			getSelectedItem().getConnection().setDeleted(true);
			
			final String connectionName = getSelectedItem().getConnection().getName();
			
			if (DialogUtils.showDeleteConnectionQuestion(connectionName) == Dialog.ACTION_YES)
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
	}
	
	@FXML
	private void undoAll()
	{
		// TODO: delete new ones?
		for (final ConfiguredConnectionDetails connection : connections)
		{
			connection.undo();
		}
		for (final ConfiguredConnectionGroupDetails group : groups)
		{
			group.undo();
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
		for (final ConfiguredConnectionGroupDetails group : groups)
		{
			group.apply();
		}
		
		listConnections();
		
		logger.debug("Saving all connections & groups");
		if (configurationManager.saveConfiguration())
		{
			DialogUtils.showTooltip(applyAllButton, "Changes for all connections and groups have been saved.");
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
		logger.info("Selecting first item...");
		// Select the first item if any connections present
		if (connections.size() > 0)
		{
			connectionList.getSelectionModel().select(0);
		}
	}
	
	public void selectConnection(final ConfiguredConnectionDetails connection)
	{
		selectConnection(rootItem, connection);
	}
	
	public void selectGroup(final ConfiguredConnectionGroupDetails group)
	{
		selectGroup(rootItem, group);
	}
	
	public void selectConnection(final TreeItem<ConnectionTreeItemProperties> parentItem, final ConfiguredConnectionDetails connection)
	{
		for (final TreeItem<ConnectionTreeItemProperties> treeItem : parentItem.getChildren()) 
		{
			final ConnectionTreeItemProperties item = treeItem.getValue();
			if (!item.isGroup() && item.getConnection().equals(connection))
			{
				connectionList.getSelectionModel().select(treeItem);
				updateUIForSelectedItem(item);
				return;
			}
			
			selectConnection(treeItem, connection);
		}
	}
	
	public void selectGroup(final TreeItem<ConnectionTreeItemProperties> parentItem, final ConfiguredConnectionGroupDetails group)
	{
		for (final TreeItem<ConnectionTreeItemProperties> treeItem : parentItem.getChildren()) 
		{
			final ConnectionTreeItemProperties item = treeItem.getValue();
			if (item.isGroup() && item.getConnectionGroup().equals(group))
			{
				connectionList.getSelectionModel().select(treeItem);
				updateUIForSelectedItem(item);
				return;
			}
			
			selectGroup(treeItem, group);
		}
	}
	
	private ConnectionTreeItemProperties getSelectedItem()
	{
		if (connectionList.getSelectionModel().getSelectedItem() == null)
		{
			return null;
		}
		
		return connectionList.getSelectionModel().getSelectedItem().getValue();
	}
	
	// TODO: pass extra parameter whether to recreate to just update text?
	public void listConnections()
	{
		final TreeItem<ConnectionTreeItemProperties> selectedItem = connectionList.getSelectionModel().getSelectedItem();
		ConnectionTreeItemProperties selected = null;
		
		if (selectedItem != null)
		{
			selected = selectedItem.getValue();			
		}

		applyAllButton.setDisable(true);
		undoAllButton.setDisable(true);
		
		populateConnections(groups, connections);
		
		for (int i = 0; i < connections.size(); i++)
		{	
			final ConfiguredConnectionDetails connection = connections.get(i);
			
			if (connection.isModified())
			{
				applyAllButton.setDisable(false);
				undoAllButton.setDisable(false);
			}
		}
		
		// Reselect
		if (selected != null)
		{
			if (selected.isGroup())
			{
				selectGroup(selected.getConnectionGroup());
			}
			else
			{
				selectConnection(selected.getConnection());
			}
			//connectionList.getSelectionModel().select(selected);
		}
		else
		{
			logger.debug("No selection present");
			selectFirst();
		}
		updateUIForSelectedItem();
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
		selectConnection(rootItem, createdConnection);
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
			//showSelected();
			updateUIForSelectedItem();
		}
	}
	
	public void setConnectionManager(final ConnectionManager connectionManager)
	{
		this.connectionManager = connectionManager;
	}
}
