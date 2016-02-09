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

package pl.baczkowicz.mqttspy.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.FileChooser.ExtensionFilter;
import pl.baczkowicz.mqttspy.Main;
import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.messages.BaseMqttMessage;
import pl.baczkowicz.mqttspy.stats.StatisticsManager;
import pl.baczkowicz.mqttspy.ui.connections.ConnectionManager;
import pl.baczkowicz.mqttspy.ui.events.ConfigurationLoadedEvent;
import pl.baczkowicz.mqttspy.ui.events.ConnectionStatusChangeEvent;
import pl.baczkowicz.mqttspy.ui.events.ConnectionsChangedEvent;
import pl.baczkowicz.mqttspy.ui.events.ShowNewSubscriptionWindowEvent;
import pl.baczkowicz.mqttspy.ui.events.LoadConfigurationFileEvent;
import pl.baczkowicz.mqttspy.ui.events.NewPerspectiveSelectedEvent;
import pl.baczkowicz.mqttspy.ui.events.ShowAboutWindowEvent;
import pl.baczkowicz.mqttspy.ui.events.ShowEditConnectionsWindowEvent;
import pl.baczkowicz.mqttspy.ui.events.ShowExternalWebPageEvent;
import pl.baczkowicz.mqttspy.ui.events.ShowFormattersWindowEvent;
import pl.baczkowicz.mqttspy.ui.events.ShowMessageLogEvent;
import pl.baczkowicz.mqttspy.ui.events.ShowTestCasesWindowEvent;
import pl.baczkowicz.mqttspy.ui.messagelog.LogReaderTask;
import pl.baczkowicz.mqttspy.ui.messagelog.TaskWithProgressUpdater;
import pl.baczkowicz.mqttspy.versions.VersionManager;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.exceptions.ConfigurationException;
import pl.baczkowicz.spy.ui.configuration.UiProperties;
import pl.baczkowicz.spy.ui.panes.PaneVisibilityStatus;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;
import pl.baczkowicz.spy.ui.panes.TitledPaneStatus;
import pl.baczkowicz.spy.ui.threading.SimpleRunLaterExecutor;
import pl.baczkowicz.spy.ui.utils.FxmlUtils;
import pl.baczkowicz.spy.ui.utils.ImageUtils;

public class ViewManager
{
	private final static Logger logger = LoggerFactory.getLogger(ViewManager.class);
	
	private ConfigurationManager configurationManager;

	private VersionManager versionManager;

	private Application application;
	
	private IKBus eventBus;

	private ObservableList<String> stylesheets;
	
	private ConnectionManager connectionManager;

	private StatisticsManager statisticsManager;
	
	private SpyPerspective selectedPerspective = SpyPerspective.DEFAULT;
	
	// Controllers and stages
	
	private Stage aboutStage;
	
	private Stage formattersStage;
	
	private Stage testCasesStage;	

	private Stage editConnectionsStage; 	
	
	private AboutController aboutController;

	private EditConnectionsController editConnectionsController;

	private MainController mainController;
	
	public void init()
	{
		eventBus.subscribe(this, this::showAbout, ShowAboutWindowEvent.class);
		eventBus.subscribe(this, this::showFormatters, ShowFormattersWindowEvent.class);
		eventBus.subscribe(this, this::showTestCases, ShowTestCasesWindowEvent.class);
		eventBus.subscribe(this, this::loadConfigurationFile, LoadConfigurationFileEvent.class, new SimpleRunLaterExecutor());
		eventBus.subscribe(this, this::showEditConnectionsWindow, ShowEditConnectionsWindowEvent.class);
		eventBus.subscribe(this, this::onNewSelectedPerspective, NewPerspectiveSelectedEvent.class);
		eventBus.subscribe(this, this::openMessageLog, ShowMessageLogEvent.class);
		eventBus.subscribe(this, this::showNewSubscriptionWindow, ShowNewSubscriptionWindowEvent.class);
	}

	private void initialiseAboutWindow(final Window parentWindow)
	{
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("AboutWindow.fxml");
		final AnchorPane window = FxmlUtils.loadAnchorPane(loader);
		
		aboutController = ((AboutController) loader.getController());
		aboutController.setApplication(application);
		aboutController.setConfigurationManager(configurationManager);
		aboutController.setVersionManager(versionManager);
		aboutController.setEventBus(eventBus);
		
		aboutController.init();
		
		Scene scene = new Scene(window);
		scene.getStylesheets().addAll(stylesheets);		

		aboutStage = new Stage();
		aboutStage.setTitle("About mqtt-spy");		
		aboutStage.initModality(Modality.WINDOW_MODAL);
		aboutStage.initOwner(parentWindow);
		aboutStage.setScene(scene);
	}	

	private void initialiseTestCasesWindow(final Window parentWindow)
	{
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("TestCasesExecutionPane.fxml");
		final AnchorPane testCasesWindow = FxmlUtils.loadAnchorPane(loader);
		
		Scene scene = new Scene(testCasesWindow);
		scene.getStylesheets().addAll(stylesheets);		

		testCasesStage = new Stage();
		testCasesStage.setTitle("Test cases");		
		testCasesStage.initOwner(parentWindow);
		testCasesStage.setScene(scene);
		((TestCasesExecutionController) loader.getController()).init();
	}

	private void initialiseFormattersWindow(final Window parentWindow)
	{
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("FormattersWindow.fxml");
		final AnchorPane formattersWindow = FxmlUtils.loadAnchorPane(loader);
		
		final FormattersController formattersController = ((FormattersController) loader.getController());
		formattersController.setConfigurationManager(configurationManager);	
		formattersController.init();
		
		Scene scene = new Scene(formattersWindow);
		scene.getStylesheets().addAll(stylesheets);		

		formattersStage = new Stage();
		formattersStage.setTitle("Formatters");		
		formattersStage.initModality(Modality.WINDOW_MODAL);
		formattersStage.initOwner(parentWindow);
		formattersStage.setScene(scene);
	}
	
	private void initialiseEditConnectionsWindow(final Window parentWindow)
	{
		// This is a dirty way to reload connection settings :) possibly could be removed if all connections are closed before loading a new config file
		if (editConnectionsController != null)
		{
			eventBus.unsubscribeConsumer(editConnectionsController, ConnectionStatusChangeEvent.class);
		}
		
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("EditConnectionsWindow.fxml");
		final AnchorPane connectionWindow = FxmlUtils.loadAnchorPane(loader);
		editConnectionsController = ((EditConnectionsController) loader.getController());		
		editConnectionsController.setMainController(mainController);
		editConnectionsController.setEventBus(eventBus);
		editConnectionsController.setConnectionManager(connectionManager);
		editConnectionsController.setConfigurationManager(configurationManager);
		editConnectionsController.init();
		
		Scene scene = new Scene(connectionWindow);
		scene.getStylesheets().addAll(stylesheets);		

		editConnectionsStage = new Stage();
		editConnectionsStage.setTitle("Connection list");		
		editConnectionsStage.initModality(Modality.WINDOW_MODAL);
		editConnectionsStage.initOwner(parentWindow);
		editConnectionsStage.setScene(scene);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Scene createMainWindow(final Stage primaryStage) throws IOException
	{
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("MainWindow.fxml");
		
		// Get the associated pane
		AnchorPane pane = (AnchorPane) loader.load();
		
		final Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		
		// Set scene width, height and style
		final double height = Math.min(UiProperties.getApplicationHeight(configurationManager.getUiPropertyFile()), primaryScreenBounds.getHeight());			
		final double width = Math.min(UiProperties.getApplicationWidth(configurationManager.getUiPropertyFile()), primaryScreenBounds.getWidth());
		
		final Scene scene = new Scene(pane, width, height);			
		scene.getStylesheets().add(Main.class.getResource("application.css").toExternalForm());
		
		stylesheets = scene.getStylesheets();
		
		// Get the associated controller
		mainController = (MainController) loader.getController();
		
		mainController.setEventBus(eventBus);
		mainController.setConnectionManager(connectionManager);
		mainController.setStatisticsManager(statisticsManager);
		mainController.setVersionManager(versionManager);
		mainController.setViewManager(this);
		mainController.setConfigurationManager(configurationManager);
		mainController.updateSelectedPerspective(UiProperties.getApplicationPerspective(configurationManager.getUiPropertyFile()));
		mainController.getResizeMessagePaneMenu().setSelected(UiProperties.getResizeMessagePane(configurationManager.getUiPropertyFile()));
		
		// Set the stage's properties
		primaryStage.setScene(scene);	
		primaryStage.setMaximized(UiProperties.getApplicationMaximized(configurationManager.getUiPropertyFile()));			
					
		// Initialise resources in the main controller			
		mainController.setStage(primaryStage);
		mainController.setLastHeight(height);
		mainController.setLastWidth(width);
		mainController.init();
		
	    primaryStage.getIcons().add(ImageUtils.createIcon("mqtt-spy-logo").getImage());
	    
	    // Set up key shortcuts
		final KeyCombination newConnection = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
		final KeyCombination editConnections = new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN);		
		final KeyCombination newSubscription = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
		
		scene.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler()
		{
			@Override
			public void handle(Event event)
			{
				if (newConnection.match((KeyEvent) event))
				{
					mainController.createNewConnection();
				}				
				else if (editConnections.match((KeyEvent) event))
				{
					mainController.editConnections();
				}
				else if (newSubscription.match((KeyEvent) event))
				{
					final Tab selectedTab = mainController.getConnectionTabs().getSelectionModel().getSelectedItem();
					final ConnectionController controller = connectionManager.getControllerForTab(selectedTab);
					
					if (controller != null)
					{
						eventBus.publish(new ShowNewSubscriptionWindowEvent(controller, PaneVisibilityStatus.DETACHED));
					}
				}
			}
		});
		
		mainController.getNewConnectionMenu().setAccelerator(newConnection);
		mainController.getEditConnectionsMenu().setAccelerator(editConnections);
		
		return scene;
	}
	
	public void showNewSubscriptionWindow(final ShowNewSubscriptionWindowEvent event)
	{
		final ConnectionController connectionController = event.getConnectionController();
		final TitledPaneStatus paneStatus = connectionController.getNewSubscriptionPaneStatus();
		
		connectionController.setPaneVisiblity(paneStatus, event.getStatus());
		
		if (event.getStatus().equals(PaneVisibilityStatus.DETACHED))
		{
			paneStatus.getParentWhenDetached().setWidth(600);
			connectionController.getNewSubscriptionPaneController().requestFocus();
		}
	}
	
	public void showAbout(final ShowAboutWindowEvent event)
	{
		if (aboutStage == null)
		{
			initialiseAboutWindow(event.getParent());
		}
		
		aboutController.reloadVersionInfo();
		aboutStage.show();		
	}
	
	
	public void showFormatters(final ShowFormattersWindowEvent event)
	{
		if (formattersStage == null || !event.getParent().equals(formattersStage.getScene().getWindow()))
		{
			initialiseFormattersWindow(event.getParent());
		}
		
		if (event.isShowAndWait())
		{
			formattersStage.initOwner(event.getParent());
			formattersStage.showAndWait();
			
			// Note: removed because we now check for the parent window, and recreate if necessary
			// formattersStage = null;
		}
		else
		{
			formattersStage.show();
		}
	}
	
	public void showTestCases(final ShowTestCasesWindowEvent event)
	{
		if (testCasesStage == null)
		{
			initialiseTestCasesWindow(event.getParent());
		}
		
		testCasesStage.show();
	}
	
	public void showEditConnectionsWindow(final ShowEditConnectionsWindowEvent event)
	{
		if (editConnectionsController  == null)
		{
			initialiseEditConnectionsWindow(event.getParent());
		}
		
		if (event.isCreateNew())
		{
			editConnectionsController.newMqttConnection();
		}

		editConnectionsController.updateUIForSelectedItem();
		editConnectionsController.setPerspective(selectedPerspective);
		editConnectionsStage.showAndWait();		
		eventBus.publish(new ConnectionsChangedEvent());
	}
	
	public void onNewSelectedPerspective(final NewPerspectiveSelectedEvent event)
	{
		selectedPerspective = event.getPerspective();
		
		for (final ConnectionController connectionController : connectionManager.getConnectionControllers())
		{
			showPerspective(connectionController);
		}
		
		logger.debug("Selected perspective = " + selectedPerspective.toString());
	}
	
	public void showPerspective(final ConnectionController connectionController)
	{
		switch (selectedPerspective)
		{
			case BASIC:
				connectionController.showPanes(PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.ATTACHED);
				connectionController.setDetailedViewVisibility(true);
				break;
			case DETAILED:
				connectionController.showPanes(PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.ATTACHED);
				connectionController.setDetailedViewVisibility(true);
				break;
			case SPY:
				connectionController.showPanes(PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.ATTACHED);		
				connectionController.setDetailedViewVisibility(false);
				break;
			case SUPER_SPY:
				connectionController.showPanes(PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.ATTACHED);
				connectionController.setDetailedViewVisibility(true);
				break;
			default:
				connectionController.showPanes(PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.ATTACHED);
				connectionController.setDetailedViewVisibility(false);
				break;		
		}
	}
	
	public void openMessageLog(final ShowMessageLogEvent event)
	{
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select message audit log file to open");
		String extensions = "messages";
		fileChooser.setSelectedExtensionFilter(new ExtensionFilter("Message audit log file", extensions));

		final File selectedFile = fileChooser.showOpenDialog(event.getParent());

		if (selectedFile != null)
		{			
			final TaskWithProgressUpdater<List<BaseMqttMessage>> readAndProcess = new LogReaderTask(selectedFile, connectionManager, mainController);
			
			pl.baczkowicz.spy.ui.utils.DialogFactory.createWorkerDialog(readAndProcess);
			
			new Thread(readAndProcess).start();			
		}
	}
	
	public void clear()
	{
		connectionManager.disconnectAndCloseAll();
		
		// Only re-initialise if it has been initialised already
		if (editConnectionsController != null)
		{
			initialiseEditConnectionsWindow(mainController.getStage().getScene().getWindow());
		}	
	}		

	public void loadConfigurationFile(final LoadConfigurationFileEvent event)
	{
		logger.info("Loading configuration file from " + event.getFile().getAbsolutePath());
		
		if (configurationManager.loadConfiguration(event.getFile()))
		{
			clear();
			// controlPanelPaneController.refreshConnectionsStatus();
			eventBus.publish(new ConnectionsChangedEvent());
			
			// Process the connection settings		
			for (final ConfiguredConnectionDetails connection : configurationManager.getConnections())
			{
				if (connection.isAutoOpen() != null && connection.isAutoOpen())
				{					
					try
					{
						connectionManager.openConnection(connection, mainController);
					}
					catch (ConfigurationException e)
					{
						// TODO: show warning dialog for invalid
						logger.error("Cannot open conection {}", connection.getName(), e);
					}
				}
			}			
		}
		
		eventBus.publish(new ConfigurationLoadedEvent());
		//controlPanelPaneController.refreshConfigurationFileStatus();		
	}	
	
	public void loadDefaultConfigurationFile()
	{		
		final File defaultConfigurationFile = ConfigurationManager.getDefaultConfigurationFile();
		
		logger.info("Default configuration file present (" + defaultConfigurationFile.getAbsolutePath() + ") = " + defaultConfigurationFile.exists());
		
		if (defaultConfigurationFile.exists())
		{
			eventBus.publish(new LoadConfigurationFileEvent(defaultConfigurationFile));
			// loadConfigurationFileOnRunLater(defaultConfigurationFile);
		}
		else
		{
			configurationManager.initialiseConfiguration();
		}
	}
	
	public void showExternalWebPage(final ShowExternalWebPageEvent event)
	{
		application.getHostServices().showDocument(event.getWebpage());
	}
	
	// ************

	/**
	 * Sets the configuration manager.
	 * 
	 * @param configurationManager the configurationManager to set
	 */
	public void setConfigurationManager(ConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}
	
	/**
	 * Sets the event bus.
	 *  
	 * @param eventBus the eventBus to set
	 */
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}

//	public void setStylesheets(final ObservableList<String> stylesheets)
//	{
//		this.stylesheets = stylesheets;		
//	}

	public void setVersionManager(final VersionManager versionManager)
	{
		this.versionManager = versionManager;		
	}
	
	public void setApplication(Application application)
	{
		this.application = application;
	}

	public void setConnectionManager(final ConnectionManager connectionManager)
	{
		this.connectionManager = connectionManager;		
	}

	public void setStatisticsManager(final StatisticsManager statisticsManager)
	{
		this.statisticsManager = statisticsManager;		
	}
	
	public MainController getMainController()	
	{
		return mainController;
	}

	public SpyPerspective getPerspective()
	{
		return selectedPerspective;
	}
}
