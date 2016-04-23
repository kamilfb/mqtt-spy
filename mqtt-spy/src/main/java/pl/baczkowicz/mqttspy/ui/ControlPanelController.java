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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.MqttConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.ConfiguredMqttConnectionDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.ui.connections.ConnectionManager;
import pl.baczkowicz.mqttspy.ui.events.ConfigurationLoadedEvent;
import pl.baczkowicz.mqttspy.ui.events.ConnectionsChangedEvent;
import pl.baczkowicz.mqttspy.ui.utils.ActionUtils;
import pl.baczkowicz.mqttspy.ui.utils.DialogUtils;
import pl.baczkowicz.mqttspy.ui.utils.MqttStylingUtils;
import pl.baczkowicz.spy.configuration.BasePropertyNames;
import pl.baczkowicz.spy.connectivity.ConnectionStatus;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.exceptions.ConfigurationException;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.ui.configuration.ConfiguredConnectionGroupDetails;
import pl.baczkowicz.spy.ui.controllers.ControlPanelItemController;
import pl.baczkowicz.spy.ui.controlpanel.ControlPanelStatsUpdater;
import pl.baczkowicz.spy.ui.controlpanel.ItemStatus;
import pl.baczkowicz.spy.ui.controls.GettingInvolvedTooltip;
import pl.baczkowicz.spy.ui.events.ConnectionStatusChangeEvent;
import pl.baczkowicz.spy.ui.events.LoadConfigurationFileEvent;
import pl.baczkowicz.spy.ui.events.ShowExternalWebPageEvent;
import pl.baczkowicz.spy.ui.events.VersionInfoErrorEvent;
import pl.baczkowicz.spy.ui.events.VersionInfoReceivedEvent;
import pl.baczkowicz.spy.ui.generated.versions.SpyVersions;
import pl.baczkowicz.spy.ui.properties.VersionInfoProperties;
import pl.baczkowicz.spy.ui.threading.SimpleRunLaterExecutor;
import pl.baczkowicz.spy.ui.versions.VersionManager;
import pl.baczkowicz.spy.utils.ThreadingUtils;

/**
 * The controller looking after the control panel.
 */
public class ControlPanelController extends AnchorPane implements Initializable
{
	private final static Logger logger = LoggerFactory.getLogger(ControlPanelController.class);

	private static final double MAX_CONNECTIONS_HEIGHT = 350;
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private ControlPanelItemController controlPanelItem1Controller;
	
	@FXML
	private ControlPanelItemController controlPanelItem2Controller;
	
	@FXML
	private ControlPanelItemController controlPanelItem3Controller;
	
	@FXML
	private ControlPanelItemController controlPanelItem4Controller;
	
	@FXML
	private Button button1;
	
	@FXML
	private Button button2;
	
	@FXML
	private Button button3;
	
	@FXML
	private Button button4;

	private VersionManager versionManager;

	private MqttConfigurationManager configurationManager;

	private MainController mainController;
	
	private IKBus eventBus;

	private ConnectionManager connectionManager;
	
	private ControlPanelStatsUpdater statsUpdater;
	
	private Map<ConnectionStatus, String> nextActionTitle = new HashMap<ConnectionStatus, String>();

	private GettingInvolvedTooltip gettingInvolvedTooltip;
	
	// ===============================
	// === Initialisation ============
	// ===============================
	
	public void initialize(URL location, ResourceBundle resources)
	{
		nextActionTitle.put(ConnectionStatus.NOT_CONNECTED, "Connect to");
		nextActionTitle.put(ConnectionStatus.CONNECTING, "Connecting to");
		nextActionTitle.put(ConnectionStatus.CONNECTED, "Disconnect from");
		nextActionTitle.put(ConnectionStatus.DISCONNECTED, "Connect to");
		nextActionTitle.put(ConnectionStatus.DISCONNECTING, "Disconnecting from");
	}
		
	public void init()
	{		
		eventBus.subscribe(this, this::onVersionInfoReceived, VersionInfoReceivedEvent.class, new SimpleRunLaterExecutor());
		eventBus.subscribe(this, this::onVersionInfoError, VersionInfoErrorEvent.class, new SimpleRunLaterExecutor());
		eventBus.subscribe(this, this::onConnectionStatusChanged, ConnectionStatusChangeEvent.class, new SimpleRunLaterExecutor());
		eventBus.subscribe(this, this::onConnectionsChanged, ConnectionsChangedEvent.class);
		eventBus.subscribe(this, this::onConfigurationFileStatusChange, ConfigurationLoadedEvent.class);
		
		// Item 1
		showConfigurationFileStatus(controlPanelItem1Controller, button1);		
		
		// Item 2
		showConnections(controlPanelItem2Controller, button2);					
		
		// Item 3
		checkForUpdates(controlPanelItem3Controller, button3);	
		
		// Item 4			
		showStats(controlPanelItem4Controller, button4);
	}
	

	// ===============================
	// === FXML ======================
	// ===============================

	// ===============================
	// === Logic =====================
	// ===============================	
	
	private void showStats(final ControlPanelItemController controller, final Button button)
	{
		controlPanelItem4Controller.refresh();
		
		statsUpdater = new ControlPanelStatsUpdater(controlPanelItem4Controller, button, eventBus);
		statsUpdater.show();
		
		final String text = 
				"mqtt-spy needs you! Please support the project" + System.lineSeparator()
				+ "by raising bugs, " + "helping out with testing" + System.lineSeparator()
				+ "or making a charity donation. " + System.lineSeparator()
				+ "See http://github.com/kamilfb/mqtt-spy/wiki/Getting-involved" + System.lineSeparator()
				+ "for more information on how to get involved." + System.lineSeparator();		
		
		gettingInvolvedTooltip = new GettingInvolvedTooltip(text, "mqtt-spy-logo");				  
		button.setTooltip(gettingInvolvedTooltip);
		button.setOnMouseMoved(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				gettingInvolvedTooltip.setCurrentMousePosition(event);
				
				if (gettingInvolvedTooltip.isShowing())
				{
					gettingInvolvedTooltip.checkAndHide();
				}
			}
		});
	}
	
	public void onConnectionStatusChanged(final ConnectionStatusChangeEvent event)
	{
		refreshConnectionsStatus();
	}
	
	public void onConnectionsChanged(final ConnectionsChangedEvent event)
	{
		refreshConnectionsStatus();
	}
	
	public void refreshConnectionsStatus()
	{
		logger.trace("Refreshing connection status...");
		showConnections(controlPanelItem2Controller, button2);				
	}

	public void onConfigurationFileStatusChange(final ConfigurationLoadedEvent event)
	{
		showConfigurationFileStatus(controlPanelItem1Controller, button1);		
	}

	
	private void showConfigurationFileStatus(
			final ControlPanelItemController controller, final Button button)
	{
		if (configurationManager.getLoadedConfigurationFile() == null)
		{
			controller.setTitle("No configuration file found.");
			controller.setDetails("Click here display all available options for resolving missing configuration file.");
			controller.setStatus(ItemStatus.WARN);
			
			button.setOnAction(new EventHandler<ActionEvent>()
			{			
				@Override
				public void handle(ActionEvent event)
				{
					if (DialogUtils.showDefaultConfigurationFileMissingChoice("Configuration file not found", button.getScene().getWindow()))
					{
						eventBus.publish(new LoadConfigurationFileEvent(MqttConfigurationManager.getDefaultConfigurationFile()));
						// mainController.loadConfigurationFileOnRunLater(ConfigurationManager.getDefaultConfigurationFile());
					}					
				}
			});
		}
		else
		{
			button.setOnAction(null);
			
			if (configurationManager.isConfigurationReadOnly())
			{
				controller.setTitle("Configuration file loaded, but it's read-only.");
				controller.setDetails("The configuration that has been loaded from " + configurationManager.getLoadedConfigurationFile().getAbsolutePath() + " is read-only.");
				controller.setStatus(ItemStatus.WARN);
			}
			else
			{
				controller.setTitle("Configuration file loaded successfully.");
				controller.setDetails("The configuration has been loaded from " + configurationManager.getLoadedConfigurationFile().getAbsolutePath() + ".");				
				controller.setStatus(ItemStatus.OK);
			}
		}
		
		controller.refresh();		
	}	
	
	private void showPending(final String statusText, final ConnectionStatus status, 
			final MqttAsyncConnection connection, final ConfiguredMqttConnectionDetails connectionDetails, 
			final Button connectionButton, final String connectionName)
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
	
	private Button createConnectionButton(final ConfiguredMqttConnectionDetails connectionDetails)
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
			showPending("Opening", null, connection, connectionDetails, connectionButton, connectionName);
		}
		else if (connection.getConnectionStatus() == ConnectionStatus.CONNECTING)
		{
			showPending("Connecting to", connection.getConnectionStatus(), connection, connectionDetails, connectionButton, connectionName);
		}
		else if (connection.getConnectionStatus() != null)
		{
			final String buttonText = nextActionTitle.get(connection.getConnectionStatus()) + " " + connectionName; 
			connectionButton.getStyleClass().add(MqttStylingUtils.getStyleForMqttConnectionStatus(connection.getConnectionStatus()));	
			connectionButton.setOnAction(ActionUtils.createNextAction(connection.getConnectionStatus(), connection, connectionManager));
			
			connectionButton.setGraphic(null);
			connectionButton.setText(buttonText);
		}		
				
		return connectionButton;
	}
	
	public void showConnections(final ControlPanelItemController controller, final Button button)
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
				final List<ConfiguredMqttConnectionDetails> connections = configurationManager.getConnections(group);
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
				// for (final ConfiguredConnectionDetails connection : configurationManager.getConnections())
				{
					buttons.getChildren().add(createConnectionButton(connection));
				}
				
				controller.getCustomItems().getChildren().add(buttons);
				
				button.setOnAction(new EventHandler<ActionEvent>()
				{			
					@Override
					public void handle(ActionEvent event)
					{
						mainController.editConnections();			
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
					mainController.createNewConnection();			
				}
			});
		}
		controller.refresh();
	}
	
	public void checkForUpdates(final ControlPanelItemController controller, final Button button)
	{
		button.setOnAction(new EventHandler<ActionEvent>()
		{			
			@Override
			public void handle(ActionEvent event)
			{				
				eventBus.publish(new ShowExternalWebPageEvent(configurationManager.getDefaultPropertyFile().getProperty(BasePropertyNames.DOWNLOAD_URL)));			
			}
		});
		
		// Set the default state
		controller.setStatus(ItemStatus.INFO);
		controller.setTitle("Connecting to the mqtt-spy update server...");
		controller.setShowProgress(true);
		controller.setDetails("Please wait while mqtt-spy retrieves information about available updates.");

		// Run the version check in a separate thread, so that it doesn't block JavaFX
		new Thread(new Runnable()
		{			
			@Override
			public void run()
			{
				try
				{
					versionManager.setLoading(true);
					
					// Wait some time for the app to start properly
					ThreadingUtils.sleep(5000);					
					
					final SpyVersions versions = versionManager.loadVersions();
					
					logger.debug("Retrieved version info = " + versions.toString());
					eventBus.publish(new VersionInfoReceivedEvent(versions));
					// eventManager.notifyVersionInfoRetrieved(versions);
				}
				catch (final XMLException e)
				{
					// If an error occurred					
					eventBus.publish(new VersionInfoErrorEvent(e));
					// eventManager.notifyVersionInfoError(e);				
				}
			}
		}).start();		
			
		controller.refresh();
	}
	
	public void showUpdateInfo(final ControlPanelItemController controller, final Button button)
	{
		controller.setShowProgress(false);
		
		final VersionInfoProperties properties = versionManager.getVersionInfoProperties(configurationManager.getDefaultPropertyFile());
		controller.setStatus(properties.getStatus());
		controller.setTitle(properties.getTitle());
		controller.setDetails(properties.getDetails());
		
		controller.refresh();
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setConfigurationMananger(final MqttConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}
	
	public void setMainController(final MainController mainController)
	{
		this.mainController = mainController;
	}

	public void setConnectionManager(final ConnectionManager connectionManager)
	{
		this.connectionManager = connectionManager;		
	}

	public void onVersionInfoReceived(final VersionInfoReceivedEvent event)
	{
		// If all OK
//		Platform.runLater(new Runnable()
//		{						
//			@Override
//			public void run()
//			{
				showUpdateInfo(controlPanelItem3Controller, button3);							
//			}
//		});
	}

	public void onVersionInfoError(final VersionInfoErrorEvent event)
	{
//		Platform.runLater(new Runnable()
//		{						
//			@Override
//			public void run()
//			{
				controlPanelItem3Controller.setStatus(ItemStatus.ERROR);
				controlPanelItem3Controller.setShowProgress(false);
				controlPanelItem3Controller.setTitle("Error occurred while getting version info. Please perform manual update.");
				logger.error("Cannot retrieve version info", event.getException());
				
				controlPanelItem3Controller.refresh();
//			}
//		});		
	}

	public void setVersionManager(final VersionManager versionManager)
	{
		this.versionManager = versionManager;		
	}
	
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}
}
