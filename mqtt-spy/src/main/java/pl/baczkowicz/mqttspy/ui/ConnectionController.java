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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttConnectionStatus;
import pl.baczkowicz.mqttspy.connectivity.MqttSubscription;
import pl.baczkowicz.mqttspy.events.EventManager;
import pl.baczkowicz.mqttspy.events.observers.ConnectionStatusChangeObserver;
import pl.baczkowicz.mqttspy.stats.StatisticsManager;
import pl.baczkowicz.mqttspy.ui.connections.ConnectionManager;
import pl.baczkowicz.mqttspy.ui.panes.TabController;
import pl.baczkowicz.mqttspy.ui.panes.TabStatus;
import pl.baczkowicz.mqttspy.ui.panes.TitledPaneController;
import pl.baczkowicz.mqttspy.ui.utils.DialogUtils;
import pl.baczkowicz.mqttspy.ui.utils.StylingUtils;

/**
 * Controller looking after the connection tab.
 */
public class ConnectionController implements Initializable, ConnectionStatusChangeObserver, TabController
{
	private static final int MIN_COLLAPSED_PANE_HEIGHT = 26;
	
	private static final int SUBSCRIPTION_PANE_MIN_EXPANDED_HEIGHT = 64;

	private static final int SUBSCRIPTION_PANE_MIN_COLLAPSED_HEIGHT = MIN_COLLAPSED_PANE_HEIGHT;
	
	private static final int PUBLICATION_PANE_MIN_EXPANDED_HEIGHT = 96;	
	
	private static final int PUBLICATION_PANE_MIN_COLLAPSED_HEIGHT = MIN_COLLAPSED_PANE_HEIGHT;
	
	private static final int SCRIPTED_PUBLICATION_PANE_MIN_EXPANDED_HEIGHT = 136;	
	
	private static final int SCRIPTED_PUBLICATION_PANE_MIN_COLLAPSED_HEIGHT = MIN_COLLAPSED_PANE_HEIGHT;

	final static Logger logger = LoggerFactory.getLogger(ConnectionController.class);

	@FXML
	private AnchorPane connectionPane;
	
	@FXML
	private SplitPane splitPane;
	
	@FXML
	private AnchorPane newPublicationPane;
	
	@FXML
	private AnchorPane newSubscriptionPane;
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private NewPublicationController newPublicationPaneController;
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private PublicationScriptsController publicationScriptsPaneController;
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private NewSubscriptionController newSubscriptionPaneController;
	
	/** For convenience, this represents a controller for the subscriptions titled pane. */
	private SubscriptionsController subscriptionsController = new SubscriptionsController();

	@FXML
	private TitledPane publishMessageTitledPane;
	
	@FXML
	private TitledPane newSubscriptionTitledPane;

	@FXML
	private TitledPane scriptedPublicationsTitledPane;
	
	@FXML
	private TitledPane subscriptionsTitledPane;
	
	@FXML
	private TabPane subscriptionTabs;

	private MqttAsyncConnection connection;

	private Tab connectionTab;
	
	private Tooltip tooltip;

	private StatisticsManager statisticsManager;

	private ConnectionManager connectionManager;

	private EventManager eventManager;

	private Map<TitledPaneController, Boolean> panes = new LinkedHashMap<>();
	
	private Map<TitledPane, TitledPaneController> paneToController = new HashMap<>();

	private boolean detailedView;

	private boolean replayMode;

	private TabStatus tabStatus;

	private ChangeListener<Boolean> createChangeListener()
	{
		return new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2)
			{
				updateMinHeights();
			}
		};
	}
	
	public void initialize(URL location, ResourceBundle resources)
	{		
		// Nothing to do here for now...
	}
	
	public void init()
	{
		subscriptionsTitledPane.expandedProperty().addListener(createChangeListener());
		
		panes.put(getSubscriptionsController(), true);
		paneToController.put(subscriptionsTitledPane, subscriptionsController);
		
		subscriptionsController.setTitledPane(subscriptionsTitledPane);
		
		if (!replayMode)
		{
			publishMessageTitledPane.expandedProperty().addListener(createChangeListener());		
			scriptedPublicationsTitledPane.expandedProperty().addListener(createChangeListener());		
			newSubscriptionTitledPane.expandedProperty().addListener(createChangeListener());
			
			scriptedPublicationsTitledPane.setExpanded(false);
			
			panes.put(getNewPublicationPaneController(), true);
			panes.put(getPublicationScriptsPaneController(), true);
			panes.put(newSubscriptionPaneController, true);
			paneToController.put(publishMessageTitledPane, newPublicationPaneController);
			paneToController.put(scriptedPublicationsTitledPane, publicationScriptsPaneController);
			paneToController.put(newSubscriptionTitledPane, newSubscriptionPaneController);
			
			newPublicationPaneController.setConnection(connection);
			newPublicationPaneController.setScriptManager(connection.getScriptManager());
			newPublicationPaneController.setEventManager(eventManager);
			newPublicationPaneController.setTitledPane(publishMessageTitledPane);
			newPublicationPaneController.init();
			
			newSubscriptionPaneController.setConnection(connection);
			newSubscriptionPaneController.setConnectionController(this);
			newSubscriptionPaneController.setConnectionManager(connectionManager);
			newSubscriptionPaneController.setTitledPane(newSubscriptionTitledPane);
			
			publicationScriptsPaneController.setConnection(connection);
			publicationScriptsPaneController.setEventManager(eventManager);
			publicationScriptsPaneController.setTitledPane(scriptedPublicationsTitledPane);
			publicationScriptsPaneController.init();
			
			tooltip = new Tooltip();
			connectionTab.setTooltip(tooltip);
		}
		
		updateMinHeights();
		// connectionPane.setMaxWidth(500);
		// subscriptionsTitledPane.setMaxWidth(500);
		// subscriptionTabs.setMaxWidth(500);
		// TODO: how not to resize the tab pane on too many tabs? All max sizes seems to be ignored...
	}
	
	public void setConnectionManager(final ConnectionManager connectionManager)
	{
		this.connectionManager = connectionManager;
	}
	
	public void updateMinHeights()
	{
		if (publishMessageTitledPane.isExpanded())
		{
			publishMessageTitledPane.setMinHeight(PUBLICATION_PANE_MIN_EXPANDED_HEIGHT);
		}
		else
		{
			publishMessageTitledPane.setMinHeight(PUBLICATION_PANE_MIN_COLLAPSED_HEIGHT);
		}
		
		if (scriptedPublicationsTitledPane.isExpanded())
		{
			scriptedPublicationsTitledPane.setMinHeight(SCRIPTED_PUBLICATION_PANE_MIN_EXPANDED_HEIGHT);
		}
		else
		{
			scriptedPublicationsTitledPane.setMinHeight(SCRIPTED_PUBLICATION_PANE_MIN_COLLAPSED_HEIGHT);
		}
		
		if (newSubscriptionTitledPane.isExpanded())
		{
			newSubscriptionTitledPane.setMinHeight(SUBSCRIPTION_PANE_MIN_EXPANDED_HEIGHT);
			newSubscriptionTitledPane.setMaxHeight(SUBSCRIPTION_PANE_MIN_EXPANDED_HEIGHT);
		}
		else
		{
			newSubscriptionTitledPane.setMinHeight(SUBSCRIPTION_PANE_MIN_COLLAPSED_HEIGHT);
			newSubscriptionTitledPane.setMaxHeight(SUBSCRIPTION_PANE_MIN_COLLAPSED_HEIGHT);
		}
	}

	public MqttAsyncConnection getConnection()
	{
		return connection;
	}

	public void setConnection(MqttAsyncConnection connection)
	{
		this.connection = connection;
	}

	public Tab getTab()
	{
		return connectionTab;
	}

	public void setTab(Tab tab)
	{
		this.connectionTab = tab;
	}

	public TabPane getSubscriptionTabs()
	{
		return subscriptionTabs;
	}
	
	public void showTabTile(final boolean pending)
	{
		if (pending)
		{
			final HBox title = new HBox();
			title.setAlignment(Pos.CENTER);
			final ProgressIndicator progressIndicator = new ProgressIndicator();
			progressIndicator.setMaxSize(15, 15);												
			title.getChildren().add(progressIndicator);
			title.getChildren().add(new Label(" " + connection.getName()));
			connectionTab.setGraphic(title);
			connectionTab.setText(null);
		}
		else
		{
			connectionTab.setGraphic(null);
			connectionTab.setText(connection.getName());
		}
	}
	
	public void onConnectionStatusChanged(final MqttAsyncConnection changedConnection)
	{
		final MqttConnectionStatus connectionStatus = changedConnection.getConnectionStatus();
		
		newSubscriptionPaneController.setConnected(false);
		getNewPublicationPaneController().setConnected(false);
		
		for (final MqttSubscription sub : connection.getSubscriptions().values())
		{
			sub.getSubscriptionController().updateContextMenu();
		}
		
		// If the context menu is available and has items in it
		if (connectionTab.getContextMenu() != null && connectionTab.getContextMenu().getItems().size() > 0)
		{
			// TODO: change that to the Specification pattern
			switch (connectionStatus)
			{
				case NOT_CONNECTED:
					connectionTab.getContextMenu().getItems().get(0).setDisable(false);
					connectionTab.getContextMenu().getItems().get(2).setDisable(true);										
					connectionTab.getContextMenu().getItems().get(3).setDisable(false);
					connectionTab.getContextMenu().getItems().get(5).setDisable(true);
					showTabTile(false);
					break;
				case CONNECTED:					
					connectionTab.getContextMenu().getItems().get(0).setDisable(true);
					connectionTab.getContextMenu().getItems().get(2).setDisable(false);
					connectionTab.getContextMenu().getItems().get(3).setDisable(false);
					connectionTab.getContextMenu().getItems().get(5).setDisable(false);
					newSubscriptionPaneController.setConnected(true);
					getNewPublicationPaneController().setConnected(true);
					showTabTile(false);
					break;
				case CONNECTING:
					connectionTab.getContextMenu().getItems().get(2).setDisable(true);
					connectionTab.getContextMenu().getItems().get(0).setDisable(true);					
					connectionTab.getContextMenu().getItems().get(3).setDisable(true);
					connectionTab.getContextMenu().getItems().get(5).setDisable(true);
					showTabTile(true);						
					break;
				case DISCONNECTED:
					connectionTab.getContextMenu().getItems().get(0).setDisable(false);
					connectionTab.getContextMenu().getItems().get(2).setDisable(true);										
					connectionTab.getContextMenu().getItems().get(3).setDisable(false);
					connectionTab.getContextMenu().getItems().get(5).setDisable(true);
					showTabTile(false);
					break;
				case DISCONNECTING:					
					connectionTab.getContextMenu().getItems().get(0).setDisable(true);
					connectionTab.getContextMenu().getItems().get(2).setDisable(true);
					connectionTab.getContextMenu().getItems().get(3).setDisable(false);
					connectionTab.getContextMenu().getItems().get(5).setDisable(true);
					showTabTile(false);
					break;
				default:
					break;
			}
		}

		if (connectionTab.getStyleClass().size() > 1)
		{
			connectionTab.getStyleClass().remove(1);
		}
		connectionTab.getStyleClass().add(StylingUtils.getStyleForMqttConnectionStatus(connectionStatus));
		
		DialogUtils.updateConnectionTooltip(connection, tooltip);
	}
	
	public void updateConnectionStats()
	{
		for (final SubscriptionController subscriptionController : connectionManager.getSubscriptionManager(connection).getSubscriptionControllers())
		{
			subscriptionController.updateSubscriptionStats();
		}
	}

	public StatisticsManager getStatisticsManager()
	{
		return statisticsManager;
	}

	public void setStatisticsManager(StatisticsManager statisticsManager)
	{
		this.statisticsManager = statisticsManager;
	}
	
	public NewSubscriptionController getNewSubscriptionPaneController()
	{
		return newSubscriptionPaneController;
	}

	public void setEventManager(final EventManager eventManager)
	{
		this.eventManager = eventManager;
	}
	
	public void togglePane(final TitledPaneController pane)
	{
		// Ignore any layout requests when in replay mode
		if (!replayMode)
		{
			panes.put(pane, !panes.get(pane));		
			updateVisiblePanes();
		}
	}
	
	public boolean getDetailedViewVisibility()
	{
		return detailedView;
	}
	
	public void setDetailedViewVisibility(final boolean visible)
	{
		detailedView = visible;
		newSubscriptionPaneController.setDetailedViewVisibility(visible);
		getNewPublicationPaneController().setDetailedViewVisibility(visible);
		
		for (final SubscriptionController subscriptionController : connectionManager.getSubscriptionManager(connection).getSubscriptionControllers())
		{
			subscriptionController.setDetailedViewVisibility(visible);
		}
	}
	
	public void toggleMessagePayloadSize(final boolean resize)
	{
		for (final SubscriptionController subscriptionController : connectionManager.getSubscriptionManager(connection).getSubscriptionControllers())
		{
			subscriptionController.toggleMessagePayloadSize(resize);
		}
	}
	
	public void toggleDetailedViewVisibility()
	{
		newSubscriptionPaneController.toggleDetailedViewVisibility();
		getNewPublicationPaneController().toggleDetailedViewVisibility();
		
		for (final SubscriptionController subscriptionController : connectionManager.getSubscriptionManager(connection).getSubscriptionControllers())
		{
			subscriptionController.toggleDetailedViewVisibility();
		}
	}
	
	public void showPanes(boolean showManualPublications, boolean showScriptedPublications, boolean showNewSubscription, boolean showReceivedMessagesSummary)
	{
		// Ignore any layout requests when in replay mode
		if (!replayMode)
		{
			panes.put(newPublicationPaneController, showManualPublications);
			panes.put(publicationScriptsPaneController, showScriptedPublications);
			panes.put(newSubscriptionPaneController, showNewSubscription);
			panes.put(subscriptionsController, showReceivedMessagesSummary);
						
			updateVisiblePanes();
		}
	}
	
	public void setReplayMode(final boolean value)
	{
		replayMode = value;
	}
	
	public void showReplayMode()
	{	
		connectionTab.getStyleClass().add("connection-replay");
		
		panes.put(newPublicationPaneController, false);
		panes.put(publicationScriptsPaneController, false);
		panes.put(newSubscriptionPaneController, false);
		panes.put(subscriptionsController, true);
		
		subscriptionsTitledPane.setText("Logged messages");
		
		updateVisiblePanes();
	}
	
	private void insertPane(final TitledPaneController controller)
	{
		int insertIndex = splitPane.getItems().size();
		
		for (int i = 0; i < splitPane.getItems().size(); i++)
		{
			final Node pane = splitPane.getItems().get(i);
			
			if (paneToController.get(pane).getTitledPaneStatus().getDisplayIndex() 
					> controller.getTitledPaneStatus().getDisplayIndex())
			{
				insertIndex = i;
				break;
			}
		}
		
		// logger.info("Inserting at " + insertIndex + "; " + controller);
		splitPane.getItems().add(insertIndex, controller.getTitledPane());
	}
	
	private void updateVisiblePanes()
	{	
		for (final TitledPaneController controller : panes.keySet())
		{			
			// logger.info(controller + "/" + controller.getTitledPane() + " is " + panes.get(controller));
			
			// If set to be shown
			if (panes.get(controller))
			{
				// logger.info("Show; contains = " + splitPane.getItems().contains(controller.getTitledPane()));
				
				// Show
				if (!splitPane.getItems().contains(controller.getTitledPane()))
				{
					insertPane(controller);
				}
			}
			else
			{
				// logger.info("Hide; contains = " + splitPane.getItems().contains(controller.getTitledPane()));

				// Don't show
				if (splitPane.getItems().contains(controller.getTitledPane()))
				{
					splitPane.getItems().remove(controller.getTitledPane());
				}
			}
		}
	}
	
	public TabStatus getTabStatus()
	{		
		return tabStatus;
	}	

	/**
	 * Sets the pane status.
	 * 
	 * @param paneStatus the paneStatus to set
	 */
	public void setTabStatus(TabStatus paneStatus)
	{
		this.tabStatus = paneStatus;
	}

	@Override
	public void refreshStatus()
	{
		if (connection != null)
		{
			onConnectionStatusChanged(connection);
		}
	}

	/**
	 * Gets the new publication pane controller.
	 * 
	 * @return the newPublicationPaneController
	 */
	public NewPublicationController getNewPublicationPaneController()
	{
		return newPublicationPaneController;
	}

	/**
	 * Gets the subscriptions controller.
	 * 
	 * @return the subscriptionsController
	 */
	public SubscriptionsController getSubscriptionsController()
	{
		return subscriptionsController;
	}

	/**
	 * Gets the publication scripts controller.
	 * 
	 * @return the publicationScriptsPaneController
	 */
	public PublicationScriptsController getPublicationScriptsPaneController()
	{
		return publicationScriptsPaneController;
	}
}
