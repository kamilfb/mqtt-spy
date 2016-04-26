package pl.baczkowicz.mqttspy.connectivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.layout.AnchorPane;
import pl.baczkowicz.mqttspy.common.generated.ProtocolVersionEnum;
import pl.baczkowicz.mqttspy.configuration.ConfiguredMqttConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.MqttConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.ui.MqttConnectionViewManager;
import pl.baczkowicz.mqttspy.ui.controllers.EditMqttConnectionController;
import pl.baczkowicz.mqttspy.utils.ConnectionUtils;
import pl.baczkowicz.mqttspy.utils.MqttUtils;
import pl.baczkowicz.spy.common.generated.ConnectionGroupReference;
import pl.baczkowicz.spy.common.generated.ConnectionReference;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.ui.configuration.ConfiguredConnectionGroupDetails;
import pl.baczkowicz.spy.ui.connections.IConnectionFactory;
import pl.baczkowicz.spy.ui.controllers.EditConnectionsController;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;
import pl.baczkowicz.spy.ui.properties.ConnectionListItemProperties;
import pl.baczkowicz.spy.ui.properties.ModifiableItem;
import pl.baczkowicz.spy.ui.utils.FxmlUtils;
import pl.baczkowicz.spy.ui.utils.ImageUtils;

public class MqttConnectionFactory implements IConnectionFactory
{
	private static final String MQTT = "MQTT";
	
	private EditMqttConnectionController editConnectionPaneController;

	private MqttConnectionViewManager connectionManager;

	private IKBus eventBus;

	private MqttConfigurationManager configurationManager;

	private AnchorPane editConnectionPane;
	
	private String getIconNameForProtocol(final String protocol)
	{
		if (MQTT.equals(protocol))
		{
			return "mqtt-icon";
		}
		
		return null;
	}

	@Override
	public void populateProtocolCell(TableCell<ConnectionListItemProperties, String> cell, String item)
	{
		if (item.contains(MQTT))
		{							
			cell.setGraphic(ImageUtils.createIcon(getIconNameForProtocol(MQTT), 18));
			cell.setText(item.replace("Default", ""));
		}									
		else
		{
			cell.setText(item);
		}			
	}

	public ModifiableItem newConnection(final String protocol)
	{
		if (MQTT.equals(protocol))
		{
			final UserInterfaceMqttConnectionDetails baseConnection = new UserInterfaceMqttConnectionDetails();				
			baseConnection.getServerURI().add("127.0.0.1");
			baseConnection.setClientID(MqttUtils.generateClientIdWithTimestamp(System.getProperty("user.name"), ProtocolVersionEnum.MQTT_DEFAULT));
			baseConnection.setName(ConnectionUtils.composeConnectionName(baseConnection.getClientID(), baseConnection.getServerURI()));
			baseConnection.setAutoConnect(true);
			
			final ConfiguredMqttConnectionDetails connectionDetails = new ConfiguredMqttConnectionDetails(
					true, true, baseConnection);
			connectionDetails.setID(MqttConfigurationManager.generateConnectionId());
			
			return connectionDetails;
		}
		
		return null;
	}
	
	public ModifiableItem duplicateConnection(final ModifiableItem copyFrom)
	{
		if (copyFrom instanceof UserInterfaceMqttConnectionDetails)
		{
			final ConfiguredMqttConnectionDetails connectionDetails = new ConfiguredMqttConnectionDetails(				
					true, true, (UserInterfaceMqttConnectionDetails) copyFrom);		
			connectionDetails.setID(MqttConfigurationManager.generateConnectionId());
			
			return connectionDetails;
		}
				
		return null;
	}
	
	@Override
	public Collection<AnchorPane> loadControllers(final Object parent)
	{
		final Collection<AnchorPane> items = new ArrayList<AnchorPane>();
		
		items.add(loadController(MQTT, parent));
		
		return items;
	}
	
	@Override
	public Collection<MenuItem> createMenuItems()
	{
		final Collection<MenuItem> items = new ArrayList<MenuItem>();
		
		items.add(createMenuItemForProtocol(MQTT));
		
		return items;
	}
	
	private MenuItem createMenuItemForProtocol(final String protocol)
	{
		if (MQTT.equals(protocol))
		{
			// Populate menu
			final MenuItem newMqttConnection = new MenuItem("Create new MQTT connection");
			newMqttConnection.setGraphic(ImageUtils.createIcon(getIconNameForProtocol(MQTT), 18));
			newMqttConnection.setOnAction(new EventHandler<ActionEvent>()
			{			
				@Override
				public void handle(ActionEvent event)
				{
					newConnection(MQTT);				
				}
			});
			
			return newMqttConnection;
		}
		
		return null;
	}
	
	public ConnectionListItemProperties createConnectionListItemProperties(final ModifiableItem connection)
	{
		ConnectionListItemProperties properties = null;
	
		if (connection instanceof ConfiguredMqttConnectionDetails)
		{
			final ConfiguredMqttConnectionDetails mqttConnection = (ConfiguredMqttConnectionDetails) connection;
			
			final ProtocolVersionEnum protocol = mqttConnection.getProtocol();
		
			properties = new ConnectionListItemProperties(
				connection.getName(), 
				(protocol == null ? ProtocolVersionEnum.MQTT_DEFAULT.value() : protocol.value()), 
				mqttConnection.getClientID() + "@" + ConnectionUtils.serverURIsToString(mqttConnection.getServerURI()), 
				mqttConnection.getSSL() != null, 
						mqttConnection.getUserAuthentication() != null);
		}
		
		return properties;
	}	
	
	public void findConnections(final ConfiguredConnectionGroupDetails parentGroup, final List<ModifiableItem> connections)
	{		
		for (final ConnectionGroupReference reference : parentGroup.getSubgroups())			
		{
			final ConfiguredConnectionGroupDetails groupDetails = (ConfiguredConnectionGroupDetails) reference.getReference();
						
			// Recursive
			findConnections(groupDetails, connections);
		}
		
		for (final ConnectionReference reference : parentGroup.getConnections())			
		{
			final ConfiguredMqttConnectionDetails connectionDetails = (ConfiguredMqttConnectionDetails) reference.getReference();
			connections.add(connectionDetails);
		}		
	}
	
	private AnchorPane loadController(String protocol, final Object parent)
	{
		if (MQTT.equals(protocol))
		{
			final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("EditConnectionPane.fxml");
			editConnectionPane = FxmlUtils.loadAnchorPane(loader);
			
			editConnectionPaneController = ((EditMqttConnectionController) loader.getController());

			editConnectionPaneController.setConfigurationManager(configurationManager);
			editConnectionPaneController.setEventBus(eventBus);
			editConnectionPaneController.setConnectionManager(connectionManager);
			editConnectionPaneController.setEditConnectionsController((EditConnectionsController) parent);
			editConnectionPaneController.init();
			
			return editConnectionPane;
		}	
		
		return null;
	}

	@Override
	public void editConnection(final ModifiableItem connection)
	{
		if (connection instanceof ConfiguredMqttConnectionDetails)
		{
			editConnectionPaneController.editConnection((ConfiguredMqttConnectionDetails) connection);
		}
	}


	@Override
	public void openConnection(final ModifiableItem connection)
	{
		if (connection instanceof ConfiguredMqttConnectionDetails)
		{
			editConnectionPaneController.openConnection((ConfiguredMqttConnectionDetails) connection);
		}		
	}
	
	@Override
	public void setRecordModifications(final boolean record)
	{
		// Apply to all controllers
		editConnectionPaneController.setRecordModifications(record);		
	}

	@Override
	public void setPerspective(final SpyPerspective perspective)
	{
		// Apply to all controllers
		editConnectionPaneController.setPerspective(perspective);		
	}


	@Override
	public void setEmptyConnectionListMode(boolean empty)
	{
		// Apply to all controllers
		editConnectionPaneController.setEmptyConnectionListMode(empty);
	}
	
	public void setConfigurationManager(final MqttConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}	
	
	public void setConnectionManager(final MqttConnectionViewManager connectionManager)
	{
		this.connectionManager = connectionManager;
	}
	
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}

	@Override
	public void setVisible(boolean groupSelected)
	{
		editConnectionPane.setVisible(!groupSelected);		
	}
}
