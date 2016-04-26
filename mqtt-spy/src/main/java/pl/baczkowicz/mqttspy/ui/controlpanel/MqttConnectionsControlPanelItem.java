package pl.baczkowicz.mqttspy.ui.controlpanel;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.ConfiguredMqttConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.MqttConfigurationManager;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.ui.MqttConnectionViewManager;
import pl.baczkowicz.mqttspy.ui.utils.ActionUtils;
import pl.baczkowicz.mqttspy.ui.utils.MqttStylingUtils;
import pl.baczkowicz.spy.connectivity.ConnectionStatus;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.exceptions.ConfigurationException;
import pl.baczkowicz.spy.ui.configuration.ConfiguredConnectionGroupDetails;
import pl.baczkowicz.spy.ui.controllers.ControlPanelController;
import pl.baczkowicz.spy.ui.controllers.ControlPanelItemController;
import pl.baczkowicz.spy.ui.controlpanel.IControlPanelItem;
import pl.baczkowicz.spy.ui.controlpanel.ItemStatus;
import pl.baczkowicz.spy.ui.events.ShowEditConnectionsWindowEvent;

public class MqttConnectionsControlPanelItem implements IControlPanelItem
{
	private final static Logger logger = LoggerFactory.getLogger(MqttConnectionsControlPanelItem.class);

	private static final double MAX_CONNECTIONS_HEIGHT = 350;
	
	private MqttConfigurationManager configurationManager;

	private IKBus eventBus;

	private MqttConnectionViewManager connectionManager;

	public MqttConnectionsControlPanelItem(final MqttConfigurationManager configurationManager, 
			final MqttConnectionViewManager connectionManager, final IKBus eventBus)
	{
		this.configurationManager = configurationManager;
		this.eventBus = eventBus;
		this.connectionManager = connectionManager;
	}

	@Override
	public void update(ControlPanelItemController controlPanelItemController, Button button)
	{
		showConnections(controlPanelItemController, button, configurationManager, connectionManager, eventBus);		
	}
	
	public static void showConnections(final ControlPanelItemController controller, final Button button, 
			final MqttConfigurationManager configurationManager, final MqttConnectionViewManager connectionManager, final IKBus eventBus)
	{
		button.setMaxHeight(MAX_CONNECTIONS_HEIGHT);
		
		// Clear any previously displayed connections
		while (controller.getCustomItems().getChildren().size() > 2) { controller.getCustomItems().getChildren().remove(2); }
		
		final int connectionCount = configurationManager.getConnections().size();
		if (connectionCount > 0)
		{
			controller.setTitle("You have " + connectionCount + " " + "connection" + (connectionCount > 1 ? "s" : "") + " configured.");
			controller.setDetails("Click here to edit your connections or on the relevant button to open, connect, reconnect or disconnect.");
			controller.setStatus(ItemStatus.OK);
			
			List<ConfiguredConnectionGroupDetails> groups = configurationManager.getOrderedGroups();		
			List<Label> labels = new ArrayList<>();
			for (final ConfiguredConnectionGroupDetails group : groups)
			{
				final List<ConfiguredMqttConnectionDetails> connections = (List<ConfiguredMqttConnectionDetails>) configurationManager.getConnections(group);
				if (connections.isEmpty())
				{
					continue;
				}
				
				FlowPane buttons = new FlowPane();
				buttons.setVgap(4);
				buttons.setHgap(4);
				buttons.setMaxHeight(Double.MAX_VALUE);
				//VBox.setVgrow(buttons, Priority.SOMETIMES);
				
				if (groups.size() > 1)
				{
					final Label groupLabel = new Label(group.getFullName() + " : ");
					
					// Do some basic alignment
					groupLabel.widthProperty().addListener(new ChangeListener<Number>()
					{
						@Override
						public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
						{
							double maxWidth = 0;
							for (final Label label : labels)
							{
								if (maxWidth < label.getWidth())
								{
									maxWidth = label.getWidth();
								}
							}
							for (final Label label : labels)
							{
								logger.trace("Setting min width for " + label.getText() + " to " + maxWidth);
								label.setMinWidth(maxWidth);
							}							
						}
					});
					labels.add(groupLabel);
					buttons.getChildren().add(groupLabel);
				}
				
				for (final ConfiguredMqttConnectionDetails connection : connections)
				{
					buttons.getChildren().add(createConnectionButton(connection, (MqttConnectionViewManager) connectionManager));
				}
				
				controller.getCustomItems().getChildren().add(buttons);
				
				button.setOnAction(new EventHandler<ActionEvent>()
				{			
					@Override
					public void handle(ActionEvent event)
					{
						eventBus.publish(new ShowEditConnectionsWindowEvent(button.getScene().getWindow(), false, null));
					}
				});
			}
		}
		else
		{
			controller.setTitle("You haven't got any connections configured.");
			controller.setDetails("Click here to create a new connection...");
			controller.setStatus(ItemStatus.INFO);
			
			button.setOnAction(new EventHandler<ActionEvent>()
			{			
				@Override
				public void handle(ActionEvent event)
				{
					eventBus.publish(new ShowEditConnectionsWindowEvent(button.getScene().getWindow(), true, null));
				}
			});
		}
		controller.refresh();
	}
	

	private static Button createConnectionButton(final ConfiguredMqttConnectionDetails connectionDetails, final MqttConnectionViewManager connectionManager)
	{
		MqttAsyncConnection connection = null; 
		for (final MqttAsyncConnection openedConnection : connectionManager.getConnections())
		{					
			if (connectionDetails.getID().equals(openedConnection.getId()))
			{
				connection = openedConnection;
			}
		}
		
		final Button connectionButton = new Button();
		connectionButton.setFocusTraversable(false);
		
		// final String connectionName = connectionDetails.getFullName();
		final String connectionName = connectionDetails.getName();
		
		if (connection != null)
		{
			logger.trace("Button for " + connectionName + " " 
				+ connection.getConnectionStatus() + "/" + connection.isOpening() + "/" + connection.isOpened());
		}
		
		if (connection == null || (!connection.isOpened() && !connection.isOpening()))
		{
			final String buttonText = "Open " + connectionName; 
			connectionButton.getStyleClass().add(MqttStylingUtils.getStyleForMqttConnectionStatus(null));	
			connectionButton.setOnAction(new EventHandler<ActionEvent>()
			{						
				@Override
				public void handle(ActionEvent event)
				{
					try
					{				
						connectionManager.openConnection(connectionDetails);
						event.consume();
					}
					catch (ConfigurationException e)
					{
						logger.error("Cannot open connection", e);
					}							
				}
			});
			
			connectionButton.setText(buttonText);
		}		
		else if (connection.isOpening())
		{
			showPending("Opening", null, connection, connectionDetails, connectionButton, connectionName, connectionManager);
		}
		else if (connection.getConnectionStatus() == ConnectionStatus.CONNECTING)
		{
			showPending("Connecting to", connection.getConnectionStatus(), connection, connectionDetails, connectionButton, connectionName, connectionManager);
		}
		else if (connection.getConnectionStatus() != null)
		{
			final String buttonText = ControlPanelController.nextActionTitle.get(connection.getConnectionStatus()) + " " + connectionName; 
			connectionButton.getStyleClass().add(MqttStylingUtils.getStyleForMqttConnectionStatus(connection.getConnectionStatus()));	
			connectionButton.setOnAction(ActionUtils.createNextAction(connection.getConnectionStatus(), connection, connectionManager));
			
			connectionButton.setGraphic(null);
			connectionButton.setText(buttonText);
		}		
				
		return connectionButton;
	}
	
	private static void showPending(final String statusText, final ConnectionStatus status, 
			final MqttAsyncConnection connection, final ConfiguredMqttConnectionDetails connectionDetails, 
			final Button connectionButton, final String connectionName, final MqttConnectionViewManager connectionManager)
	{			
		connectionButton.getStyleClass().add(MqttStylingUtils.getStyleForMqttConnectionStatus(status));	
		connectionButton.setOnAction(ActionUtils.createNextAction(status, connection, connectionManager));
		
		final HBox buttonBox = new HBox();			
		final ProgressIndicator buttonProgress = new ProgressIndicator();
		buttonProgress.setMaxSize(15, 15);
					
		buttonBox.getChildren().add(buttonProgress);
		buttonBox.getChildren().add(new Label(" " + statusText + " " + connectionName));

		connectionButton.setGraphic(buttonBox);
		connectionButton.setText(null);
	}
}
