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

import java.io.File;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.PublicationDetails;
import pl.baczkowicz.mqttspy.common.generated.UserCredentials;
import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.TabbedSubscriptionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.connectivity.ConnectionIdGenerator;
import pl.baczkowicz.mqttspy.connectivity.RuntimeConnectionProperties;
import pl.baczkowicz.mqttspy.events.EventManager;
import pl.baczkowicz.mqttspy.exceptions.ConfigurationException;
import pl.baczkowicz.mqttspy.exceptions.MqttSpyUncaughtExceptionHandler;
import pl.baczkowicz.mqttspy.exceptions.XMLException;
import pl.baczkowicz.mqttspy.messages.ReceivedMqttMessage;
import pl.baczkowicz.mqttspy.stats.ConnectionStatsUpdater;
import pl.baczkowicz.mqttspy.stats.StatisticsManager;
import pl.baczkowicz.mqttspy.ui.connections.ConnectionManager;
import pl.baczkowicz.mqttspy.ui.messagelog.LogReaderTask;
import pl.baczkowicz.mqttspy.ui.messagelog.TaskWithProgressUpdater;
import pl.baczkowicz.mqttspy.ui.utils.ConnectivityUtils;
import pl.baczkowicz.mqttspy.ui.utils.DialogUtils;
import pl.baczkowicz.mqttspy.ui.utils.FxmlUtils;

/**
 * Controller for the main window.
 */
public class MainController
{
	private final static Logger logger = LoggerFactory.getLogger(MainController.class);
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private ControlPanelController controlPanelPaneController;
	
	@FXML
	private AnchorPane mainPane;

	@FXML
	private TabPane connectionTabs;

	@FXML
	private MenuItem openConfigFileMenu;
	
	@FXML
	private RadioMenuItem defaultPerspective;
	
	@FXML
	private RadioMenuItem detailedPerspective;
	
	@FXML
	private RadioMenuItem spyPerspective;
	
	@FXML
	private RadioMenuItem superSpyPerspective;

	private EditConnectionsController editConnectionsController;
	
	private Stage editConnectionsStage; 	

	private Application application;

	private final ConfigurationManager configurationManager;
	
	private Stage stage;

	private EventManager eventManager;
	
	private StatisticsManager statisticsManager;

	private ConnectionManager connectionManager;

	private Stage converterStage;
	
	public MainController() throws XMLException
	{
		Thread.setDefaultUncaughtExceptionHandler(new MqttSpyUncaughtExceptionHandler());
		 
		this.statisticsManager = new StatisticsManager();
		this.eventManager = new EventManager();		
		
		final ConnectionIdGenerator connectionIdGenerator = new ConnectionIdGenerator();
		this.configurationManager = new ConfigurationManager(eventManager, connectionIdGenerator);		
		this.connectionManager = new ConnectionManager(eventManager, statisticsManager, configurationManager);			
	}

	@FXML
	public void createNewConnection()
	{
		logger.trace("Creating new connection...");

		showEditConnectionsWindow(true);
	}

	@FXML
	public void editConnections()
	{
		showEditConnectionsWindow(false);
	}
	
	@FXML
	public void showConverter()
	{
		if (converterStage == null)
		{
			initialiseConverterWindow();
		}
		
		converterStage.show();
	}
	
	@FXML
	public void openMessageLog()
	{
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select message log file to open");
		String extensions = "messages";
		fileChooser.setSelectedExtensionFilter(new ExtensionFilter("Message log file", extensions));

		final File selectedFile = fileChooser.showOpenDialog(getParentWindow());

		if (selectedFile != null)
		{			
			final TaskWithProgressUpdater<List<ReceivedMqttMessage>> readAndProcess = new LogReaderTask(selectedFile, connectionManager, this);
			
			DialogUtils.showWorkerDialog(readAndProcess);
			
			new Thread(readAndProcess).start();			
		}
	}
	
	private void initialiseEditConnectionsWindow()
	{
		// This is a dirty way to reload connection settings :) possibly could be removed if all connections are closed before loading a new config file
		if (editConnectionsController != null)
		{
			eventManager.deregisterConnectionStatusObserver(editConnectionsController);
		}
		
		final FXMLLoader loader = FxmlUtils.createFXMLLoader(this, FxmlUtils.FXML_LOCATION + "EditConnectionsWindow.fxml");
		final AnchorPane connectionWindow = FxmlUtils.loadAnchorPane(loader);
		editConnectionsController = ((EditConnectionsController) loader.getController());		
		editConnectionsController.setMainController(this);
		editConnectionsController.setEventManager(eventManager);
		editConnectionsController.setConnectionManager(connectionManager);
		editConnectionsController.setConfigurationManager(configurationManager);
		editConnectionsController.init();
		
		Scene scene = new Scene(connectionWindow);
		scene.getStylesheets().addAll(mainPane.getScene().getStylesheets());		

		editConnectionsStage = new Stage();
		editConnectionsStage.setTitle("Connection list");		
		editConnectionsStage.initModality(Modality.WINDOW_MODAL);
		editConnectionsStage.initOwner(getParentWindow());
		editConnectionsStage.setScene(scene);
	}
	
	private void initialiseConverterWindow()
	{
		final FXMLLoader loader = FxmlUtils.createFXMLLoader(this, FxmlUtils.FXML_LOCATION + "ConverterWindow.fxml");
		final AnchorPane converterWindow = FxmlUtils.loadAnchorPane(loader);
		
		Scene scene = new Scene(converterWindow);
		scene.getStylesheets().addAll(mainPane.getScene().getStylesheets());		

		converterStage = new Stage();
		converterStage.setTitle("Converter");		
		converterStage.initOwner(getParentWindow());
		converterStage.setScene(scene);
	}
	
	private void showEditConnectionsWindow(final boolean createNew)
	{
		if (editConnectionsController  == null)
		{
			initialiseEditConnectionsWindow();
		}
		
		if (createNew)
		{
			editConnectionsController.newConnection();
		}

		editConnectionsController.updateSelected();
		editConnectionsStage.showAndWait();		
		controlPanelPaneController.refreshConnectionsStatus();
	}
	
	@FXML
	public void exit()
	{
		// This is triggered by the user
		connectionManager.disconnectAll();
		
		statisticsManager.saveStats();
		System.exit(0);
	}

	public void init()
	{
		statisticsManager.loadStats();
		getParentWindow().setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			public void handle(WindowEvent t)
			{
				exit();
			}
		});

		// Clear any test tabs
		stage.setTitle("mqtt-spy");
		
		controlPanelPaneController.setMainController(this);
		controlPanelPaneController.setConfigurationMananger(configurationManager);
		controlPanelPaneController.setApplication(application);
		controlPanelPaneController.setEventManager(eventManager);
		controlPanelPaneController.setConnectionManager(connectionManager);
		controlPanelPaneController.init();	
		
		// TODO: experimental code
		// final StatsChartWindow statsWindow = new StatsChartWindow();		
		// Scene scene = new Scene(statsWindow);
		// scene.getStylesheets().addAll(mainPane.getScene().getStylesheets());		
		// statsWindow.start(new Stage());
		
		new Thread(new ConnectionStatsUpdater(connectionManager)).start();
	}	
	
	public TabPane getConnectionTabs()
	{
		return connectionTabs;
	}

	public void addConnectionTab(Tab tab)
	{
		connectionTabs.getTabs().add(tab);
	}

	private Window getParentWindow()
	{
		return mainPane.getScene().getWindow();
	}

	@FXML
	public void openConfigurationFile()
	{
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select configuration file to open");
		String extensions = "xml";
		fileChooser.setSelectedExtensionFilter(new ExtensionFilter("XML file", extensions));

		final File selectedFile = fileChooser.showOpenDialog(getParentWindow());

		if (selectedFile != null)
		{
			loadConfigurationFileAndShowErrorWhenApplicable(selectedFile);			
		}
	}
	
	public void loadConfigurationFileAndShowErrorWhenApplicable(final File selectedFile)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				loadConfigurationFile(selectedFile);				
			}
			
		});		
	}
	
	
	private void clear()
	{
		connectionManager.disconnectAndCloseAll();
		
		// Only re-initialise if it has been initialised already
		if (editConnectionsController  != null)
		{
			initialiseEditConnectionsWindow();
		}	
	}
	
	private void loadConfigurationFile(final File selectedFile)
	{
		logger.info("Loading configuration file from " + selectedFile.getAbsolutePath());
		
		if (configurationManager.loadConfiguration(selectedFile))
		{
			clear();
			controlPanelPaneController.refreshConnectionsStatus();
			
			// Process the connection settings		
			for (final ConfiguredConnectionDetails connection : configurationManager.getConnections())
			{
				if (connection.isAutoOpen() != null && connection.isAutoOpen())
				{					
					try
					{
						openConnection(connection);
					}
					catch (ConfigurationException e)
					{
						// TODO: show warning dialog for invalid
						logger.error("Cannot open conection {}", connection.getName(), e);
					}
				}
			}			
		}
		
		controlPanelPaneController.refreshConfigurationFileStatus();		
	}	
	
	private boolean completeUserAuthenticationCredentials(final UserInterfaceMqttConnectionDetails connectionDetails)
	{
		if (connectionDetails.getUserAuthentication() != null)
		{
			// Copy so that we don't store it in the connection and don't save those values
			final UserCredentials userCredentials = new UserCredentials();
			connectionDetails.getUserCredentials().copyTo(userCredentials);
			
			// Check if ask for username or password, and then override existing values if confirmed
			if (connectionDetails.getUserAuthentication().isAskForPassword() || connectionDetails.getUserAuthentication().isAskForUsername())
			{
				// Password is decoded and encoded in this utility method
				if (!DialogUtils.showUsernameAndPasswordDialog(stage, connectionDetails.getName(), userCredentials))
				{
					return true;
				}
			}
			
			// Settings user credentials so they can be validated and passed onto the MQTT client library			
			connectionDetails.setUserCredentials(userCredentials);
		}
		
		return false;
	}
	
	public void openConnection(final ConfiguredConnectionDetails configuredConnectionDetails) throws ConfigurationException
	{
		// Note: this is not a complete ConfiguredConnectionDetails copy but ConnectionDetails copy - any user credentials entered won't be stored in config
		final ConfiguredConnectionDetails connectionDetails = new ConfiguredConnectionDetails();
		configuredConnectionDetails.copyTo(connectionDetails);
		connectionDetails.setId(configuredConnectionDetails.getId());			
		
		final boolean cancelled = completeUserAuthenticationCredentials(connectionDetails);		
		
		if (!cancelled)
		{
			final String validationResult = ConnectivityUtils.validateConnectionDetails(connectionDetails, true);
			if (validationResult != null)
			{
				DialogUtils.showValidationWarning(validationResult);
			}
			else
			{
				final MainController mainController = this;
				final RuntimeConnectionProperties connectionProperties = new RuntimeConnectionProperties(connectionDetails);
				new Thread(new Runnable()
				{					
					@Override
					public void run()
					{
						connectionManager.loadConnectionTab(mainController, mainController, connectionProperties);					
					}
				}).start();											
			}
		}
	}
	
	public void populateConnectionPanes(final UserInterfaceMqttConnectionDetails connectionDetails, final ConnectionController connectionController)
	{
		for (final PublicationDetails publicationDetails : connectionDetails.getPublication())
		{
			// Add it to the list of pre-defined topics
			connectionController.newPublicationPaneController.recordPublicationTopic(publicationDetails.getTopic());
		}
		
		for (final TabbedSubscriptionDetails subscriptionDetails : connectionDetails.getSubscription())
		{
			// Check if we should create a tab for the subscription
			if (subscriptionDetails.isCreateTab())
			{
				connectionController.newSubscriptionPaneController.subscribe(subscriptionDetails, connectionDetails.isAutoSubscribe());
			}
			
			// Add it to the list of pre-defined topics
			connectionController.newSubscriptionPaneController.recordSubscriptionTopic(subscriptionDetails.getTopic());
		}
	}

	public void loadDefaultConfigurationFile()
	{		
		final File defaultConfigurationFile = ConfigurationManager.getDefaultConfigurationFile();
		
		logger.info("Default configuration file present (" + defaultConfigurationFile.getAbsolutePath() + ") = " + defaultConfigurationFile.exists());
		
		if (defaultConfigurationFile.exists())
		{
			loadConfigurationFileAndShowErrorWhenApplicable(defaultConfigurationFile);
		}
	}
	
	@FXML
	private void showPerspective()
	{
		for (final ConnectionController connectionController : connectionManager.getConnectionControllers())
		{
			showPerspective(connectionController);
		}
	}
	
	public void showPerspective(final ConnectionController connectionController)
	{
		if (spyPerspective.isSelected())
		{
			connectionController.showPanes(false, false, true, true);		
			connectionController.setDetailedViewVisibility(false);
		}
		else if (superSpyPerspective.isSelected())
		{
			connectionController.showPanes(false, false, true, true);
			connectionController.setDetailedViewVisibility(true);
		}		
		else if (detailedPerspective.isSelected())
		{
			connectionController.showPanes(true, true, true, true);
			connectionController.setDetailedViewVisibility(true);
		}
		else
		{
			connectionController.showPanes(true, true, true, true);
			connectionController.setDetailedViewVisibility(false);
		}
	}
	
	@FXML
	private void restoreConfiguration()
	{
		if (DialogUtils.showDefaultConfigurationFileMissingChoice("Restore defaults", mainPane.getScene().getWindow()))
		{
			loadConfigurationFileAndShowErrorWhenApplicable(ConfigurationManager.getDefaultConfigurationFile());			
		}
	}
	
	@FXML
	private void openProjectWebsite()
	{
		application.getHostServices().showDocument("https://code.google.com/p/mqtt-spy/");
	}
	
	@FXML
	private void openFundraisingPage()
	{
		application.getHostServices().showDocument("http://fundraise.unicef.org.uk/MyPage/mqtt-spy");
	}

	@FXML
	private void overviewWiki()
	{
		application.getHostServices().showDocument("https://code.google.com/p/mqtt-spy/wiki/Overview");		
	}
	
	@FXML
	private void changelogWiki()
	{
		application.getHostServices().showDocument("https://code.google.com/p/mqtt-spy/wiki/Changelog");
	}
	
	@FXML
	private void scriptingWiki()
	{
		application.getHostServices().showDocument("https://code.google.com/p/mqtt-spy/wiki/Scripting");
	}
	
	@FXML
	private void loggingWiki()
	{
		application.getHostServices().showDocument("https://code.google.com/p/mqtt-spy/wiki/Logging");
	}

	public void setApplication(Application application)
	{
		this.application = application;
	}

	public void setStage(Stage primaryStage)
	{
		this.stage = primaryStage;		
	}
}
