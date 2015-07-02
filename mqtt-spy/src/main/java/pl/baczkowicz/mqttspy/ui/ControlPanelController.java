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
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.PropertyFileLoader;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttConnectionStatus;
import pl.baczkowicz.mqttspy.exceptions.ConfigurationException;
import pl.baczkowicz.mqttspy.exceptions.XMLException;
import pl.baczkowicz.mqttspy.ui.connections.ConnectionManager;
import pl.baczkowicz.mqttspy.ui.controlpanel.ControlPanelStatsUpdater;
import pl.baczkowicz.mqttspy.ui.controlpanel.ItemStatus;
import pl.baczkowicz.mqttspy.ui.controlpanel.GettingInvolvedTooltip;
import pl.baczkowicz.mqttspy.ui.events.EventManager;
import pl.baczkowicz.mqttspy.ui.events.observers.ConnectionStatusChangeObserver;
import pl.baczkowicz.mqttspy.ui.utils.ActionUtils;
import pl.baczkowicz.mqttspy.ui.utils.DialogUtils;
import pl.baczkowicz.mqttspy.ui.utils.StylingUtils;
import pl.baczkowicz.mqttspy.utils.ThreadingUtils;
import pl.baczkowicz.mqttspy.versions.VersionManager;
import pl.baczkowicz.mqttspy.versions.generated.MqttSpyVersions;
import pl.baczkowicz.mqttspy.versions.generated.ReleaseStatus;

/**
 * The controller looking after the control panel.
 */
public class ControlPanelController extends AnchorPane implements Initializable, ConnectionStatusChangeObserver
{
	private final static Logger logger = LoggerFactory.getLogger(ControlPanelController.class);

	private static final double MAX_CONNECTIONS_HEIGHT = 250;
	
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

	private Application application;

	private ConfigurationManager configurationManager;

	private MainController mainController;

	private EventManager eventManager;

	private ConnectionManager connectionManager;
	
	private ControlPanelStatsUpdater statsUpdater;
	
	private Map<MqttConnectionStatus, String> nextActionTitle = new HashMap<MqttConnectionStatus, String>();

	private GettingInvolvedTooltip gettingInvolvedTooltip;
	
	// ===============================
	// === Initialisation ============
	// ===============================
	
	public void initialize(URL location, ResourceBundle resources)
	{
		nextActionTitle.put(MqttConnectionStatus.NOT_CONNECTED, "Connect to");
		nextActionTitle.put(MqttConnectionStatus.CONNECTING, "Connecting to");
		nextActionTitle.put(MqttConnectionStatus.CONNECTED, "Disconnect from");
		nextActionTitle.put(MqttConnectionStatus.DISCONNECTED, "Connect to");
		nextActionTitle.put(MqttConnectionStatus.DISCONNECTING, "Disconnecting from");
	}
		
	public void init()
	{			
		try
		{
			this.versionManager = new VersionManager(configurationManager.getDefaultPropertyFile());
		}
		catch (XMLException e)
		{
			e.printStackTrace();
		}
		
		eventManager.registerConnectionStatusObserver(this, null);
		controlPanelItem1Controller.setConfigurationMananger(configurationManager);
		controlPanelItem2Controller.setConfigurationMananger(configurationManager);
		controlPanelItem3Controller.setConfigurationMananger(configurationManager);
		controlPanelItem4Controller.setConfigurationMananger(configurationManager);
		
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
		
		statsUpdater = new ControlPanelStatsUpdater(controlPanelItem4Controller, button, application);
		statsUpdater.show();
		gettingInvolvedTooltip = new GettingInvolvedTooltip();				  
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
	
	@Override
	public void onConnectionStatusChanged(final MqttAsyncConnection changedConnection)
	{
		refreshConnectionsStatus();		
	}
	
	public void refreshConnectionsStatus()
	{
		logger.trace("Refreshing connection status...");
		showConnections(controlPanelItem2Controller, button2);				
	}

	public void refreshConfigurationFileStatus()
	{
		showConfigurationFileStatus(controlPanelItem1Controller, button1);		
	}

	
	private void showConfigurationFileStatus(
			final ControlPanelItemController controller, final Button button)
	{
		if (configurationManager.getLoadedConfigurationFile() == null)
		{
			controller.setTitle("No default configuration file found.");
			controller.setDetails("Click here display all available options for resolving missing configuration file.");
			controller.setStatus(ItemStatus.WARN);
			
			button.setOnAction(new EventHandler<ActionEvent>()
			{			
				@Override
				public void handle(ActionEvent event)
				{
					if (DialogUtils.showDefaultConfigurationFileMissingChoice("Default configuration file not found", button.getScene().getWindow()))
					{
						mainController.loadConfigurationFileAndShowErrorWhenApplicable(ConfigurationManager.getDefaultConfigurationFile());
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
	
	private void showPending(final String statusText, final MqttConnectionStatus status, 
			final MqttAsyncConnection connection, final ConfiguredConnectionDetails connectionDetails, final Button connectionButton)
	{			
		connectionButton.getStyleClass().add(StylingUtils.getStyleForMqttConnectionStatus(status));	
		connectionButton.setOnAction(ActionUtils.createNextAction(status, connection, connectionManager));
		
		final HBox buttonBox = new HBox();			
		final ProgressIndicator buttonProgress = new ProgressIndicator();
		buttonProgress.setMaxSize(15, 15);
					
		buttonBox.getChildren().add(buttonProgress);
		buttonBox.getChildren().add(new Label(" " + statusText + " " + connectionDetails.getName()));

		connectionButton.setGraphic(buttonBox);
		connectionButton.setText(null);
	}
	
	private Button createConnectionButton(final ConfiguredConnectionDetails connectionDetails)
	{
		MqttAsyncConnection connection = null; 
		for (final MqttAsyncConnection openedConnection : connectionManager.getConnections())
		{					
			if (connectionDetails.getId() == openedConnection.getId())
			{
				connection = openedConnection;
			}
		}
		
		final Button connectionButton = new Button();
		connectionButton.setFocusTraversable(false);
		
		if (connection != null)
		{
			logger.trace("Button for " + connectionDetails.getName() + " " 
				+ connection.getConnectionStatus() + "/" + connection.isOpening() + "/" + connection.isOpened());
		}
		
		if (connection == null || (!connection.isOpened() && !connection.isOpening()))
		{
			final String buttonText = "Open " + connectionDetails.getName(); 
			connectionButton.getStyleClass().add(StylingUtils.getStyleForMqttConnectionStatus(null));	
			connectionButton.setOnAction(new EventHandler<ActionEvent>()
			{						
				@Override
				public void handle(ActionEvent event)
				{
					try
					{				
						connectionManager.openConnection(connectionDetails, mainController);
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
			showPending("Opening", null, connection, connectionDetails, connectionButton);
		}
		else if (connection.getConnectionStatus() == MqttConnectionStatus.CONNECTING)
		{
			showPending("Connecting to", connection.getConnectionStatus(), connection, connectionDetails, connectionButton);
		}
		else if (connection.getConnectionStatus() != null)
		{
			final String buttonText = nextActionTitle.get(connection.getConnectionStatus()) + " " + connectionDetails.getName(); 
			connectionButton.getStyleClass().add(StylingUtils.getStyleForMqttConnectionStatus(connection.getConnectionStatus()));	
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
		
		final int connections = configurationManager.getConnections().size();
		if (connections > 0)
		{
			controller.setTitle("You have " + connections + " " + "connection" + (connections > 1 ? "s" : "") + " configured.");
			controller.setDetails("Click here to edit your connections or on the relevant button to open, connect, reconnect or disconnect.");
			controller.setStatus(ItemStatus.OK);
			
			FlowPane buttons = new FlowPane();
			buttons.setVgap(4);
			buttons.setHgap(4);
			buttons.setMaxHeight(Double.MAX_VALUE);
			VBox.setVgrow(buttons, Priority.ALWAYS);
			
			for (final ConfiguredConnectionDetails connection : configurationManager.getConnections())
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
				application.getHostServices().showDocument(configurationManager.getDefaultPropertyFile().getProperty(PropertyFileLoader.DOWNLOAD_URL));			
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
					// Wait some time for the app to start properly
					ThreadingUtils.sleep(5000);					
					
					final MqttSpyVersions versions = versionManager.loadVersions();
					logger.debug("Retrieved version info = " + versions.toString());
					
					// If all OK
					Platform.runLater(new Runnable()
					{						
						@Override
						public void run()
						{
							showUpdateInfo(controller, button);							
						}
					});

				}
				catch (final XMLException e)
				{
					// If an error occurred
					Platform.runLater(new Runnable()
					{						
						@Override
						public void run()
						{
							controller.setStatus(ItemStatus.ERROR);
							controller.setShowProgress(false);
							controller.setTitle("Error occurred while getting version info. Please perform manual update.");
							logger.error("Cannot retrieve version info", e);
							
							controller.refresh();
						}
					});

				}
			}
		}).start();		
			
		controller.refresh();
	}
	
	public void showUpdateInfo(final ControlPanelItemController controller, final Button button)
	{
		controller.setShowProgress(false);
		if (versionManager.getVersions() != null)
		{
			boolean versionFound = false;
			
			for (final ReleaseStatus release : versionManager.getVersions().getReleaseStatuses().getReleaseStatus())
			{
				if (VersionManager.isInRange(configurationManager.getDefaultPropertyFile().getFullVersionNumber(), release))
				{					
					controller.setStatus(VersionManager.convertVersionStatus(release));
					controller.setTitle(release.getUpdateTitle());
					// TODO: might need to append version info
					controller.setDetails(release.getUpdateDetails());
					versionFound = true;
					break;
				}
			}
			
			if (!versionFound)
			{
				controller.setStatus(ItemStatus.INFO);
				controller.setTitle("Couldn't find any information about your version - please check manually.");
				controller.setDetails("Your version is " + configurationManager.getDefaultPropertyFile().getFullVersionName() + ".");
			}
		}	
		else
		{
			// Set the default state
			controller.setStatus(ItemStatus.WARN);
			controller.setTitle("Cannot check for updates - is your internet connection up?");
			controller.setDetails("Click here to go to the download page for mqtt-spy.");
		}
		
		controller.refresh();
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setApplication(final Application application)
	{
		this.application = application;
	}
	
	public void setConfigurationMananger(final ConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}
	
	public void setMainController(final MainController mainController)
	{
		this.mainController = mainController;
	}

	public void setEventManager(EventManager eventManager)
	{
		this.eventManager = eventManager;		
	}

	public void setConnectionManager(final ConnectionManager connectionManager)
	{
		this.connectionManager = connectionManager;		
	}
}
