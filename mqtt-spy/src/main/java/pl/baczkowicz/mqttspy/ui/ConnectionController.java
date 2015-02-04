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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
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
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttConnectionStatus;
import pl.baczkowicz.mqttspy.connectivity.MqttSubscription;
import pl.baczkowicz.mqttspy.events.EventManager;
import pl.baczkowicz.mqttspy.events.observers.ConnectionStatusChangeObserver;
import pl.baczkowicz.mqttspy.stats.StatisticsManager;
import pl.baczkowicz.mqttspy.ui.connections.ConnectionManager;
import pl.baczkowicz.mqttspy.ui.panes.PaneVisibilityStatus;
import pl.baczkowicz.mqttspy.ui.panes.TabController;
import pl.baczkowicz.mqttspy.ui.panes.TabStatus;
import pl.baczkowicz.mqttspy.ui.panes.TitledPaneController;
import pl.baczkowicz.mqttspy.ui.panes.TitledPaneStatus;
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

	// private Map<TitledPaneController, Boolean> panes = new LinkedHashMap<>();
	
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
		
		// panes.put(subscriptionsController, true);
		subscriptionsController.getTitledPaneStatus().setVisibility(PaneVisibilityStatus.ATTACHED);
		paneToController.put(subscriptionsTitledPane, subscriptionsController);
		
		subscriptionsController.setTitledPane(subscriptionsTitledPane);
		
		if (!replayMode)
		{
			publishMessageTitledPane.expandedProperty().addListener(createChangeListener());		
			scriptedPublicationsTitledPane.expandedProperty().addListener(createChangeListener());		
			newSubscriptionTitledPane.expandedProperty().addListener(createChangeListener());
			
			scriptedPublicationsTitledPane.setExpanded(false);
			
			// panes.put(getNewPublicationPaneController(), true);
			// panes.put(getPublicationScriptsPaneController(), true);
			// panes.put(newSubscriptionPaneController, true);
			newPublicationPaneController.getTitledPaneStatus().setVisibility(PaneVisibilityStatus.ATTACHED);
			publicationScriptsPaneController.getTitledPaneStatus().setVisibility(PaneVisibilityStatus.ATTACHED);
			newSubscriptionPaneController.getTitledPaneStatus().setVisibility(PaneVisibilityStatus.ATTACHED);
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
		else
		{
			// If in replay more, remote the panes from the split pane altogether
			splitPane.getItems().remove(publishMessageTitledPane);
			splitPane.getItems().remove(scriptedPublicationsTitledPane);
			splitPane.getItems().remove(newSubscriptionTitledPane);
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
	
	public void setPaneVisiblity(final TitledPaneController pane, final PaneVisibilityStatus visibility)
	{
		// Ignore any layout requests when in replay mode
		if (!replayMode)
		{
			pane.getTitledPaneStatus().setRequestedVisibility(visibility);			
			updateVisiblePanes();
			updateMenus();
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
	
	public void showPanes(final PaneVisibilityStatus showManualPublications, final PaneVisibilityStatus showScriptedPublications, 
			final PaneVisibilityStatus showNewSubscription, final PaneVisibilityStatus showReceivedMessagesSummary)
	{
		// Ignore any layout requests when in replay mode
		if (!replayMode)
		{
			subscriptionsController.getTitledPaneStatus().setRequestedVisibility(showReceivedMessagesSummary);
			newPublicationPaneController.getTitledPaneStatus().setRequestedVisibility(showManualPublications);
			publicationScriptsPaneController.getTitledPaneStatus().setRequestedVisibility(showScriptedPublications);
			newSubscriptionPaneController.getTitledPaneStatus().setRequestedVisibility(showNewSubscription);							
			updateVisiblePanes();
			updateMenus();
		}
	}
	
	public void setReplayMode(final boolean value)
	{
		replayMode = value;
	}
	
	public void showReplayMode()
	{	
		connectionTab.getStyleClass().add("connection-replay");
				
		subscriptionsController.getTitledPaneStatus().setRequestedVisibility(PaneVisibilityStatus.ATTACHED);
		newPublicationPaneController.getTitledPaneStatus().setRequestedVisibility(PaneVisibilityStatus.NOT_VISIBLE);
		publicationScriptsPaneController.getTitledPaneStatus().setRequestedVisibility(PaneVisibilityStatus.NOT_VISIBLE);
		newSubscriptionPaneController.getTitledPaneStatus().setRequestedVisibility(PaneVisibilityStatus.NOT_VISIBLE);						
		updateVisiblePanes();
		
		subscriptionsTitledPane.setText("Logged messages");
	}
	
	private void updateMenus()
	{
		for (final TitledPaneController controller : paneToController.values())
		{
			controller.getTitledPaneStatus().updateMenu();
		}		
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
		for (final TitledPaneController controller : paneToController.values())
		{	
			final TitledPaneStatus status = controller.getTitledPaneStatus();
			
			// If no changes, go to next controller...
			if (status.getVisibility().equals(status.getRequestedVisibility()))
			{
				continue;
			}
			
			status.setVisibility(status.getRequestedVisibility());
			
			// If previous value was detached, close the detached window
			if (status.getPreviousVisibility().equals(PaneVisibilityStatus.DETACHED))
			{
				controller.getTitledPane().setCollapsible(true);
				controller.getTitledPane().setExpanded(status.isLastExpanded());
				if (status.getParentWhenDetached().isShowing())
				{
					status.getParentWhenDetached().close();
				}
			}
			// If previous value was attached, remove the pane
			else if (status.getPreviousVisibility().equals(PaneVisibilityStatus.ATTACHED))
			{
				// Remove from main window
				if (splitPane.getItems().contains(controller.getTitledPane()))
				{
					splitPane.getItems().remove(controller.getTitledPane());
				}
			}
			
			// If the pane should be detached
			if (status.getVisibility().equals(PaneVisibilityStatus.DETACHED))
			{				
				// Add to separate window
				final Stage stage = DialogUtils.createWindowWithPane(controller.getTitledPane(), splitPane.getScene(), 
						connection.getName(), 0);
				status.setParentWhenDetached(stage);
				status.setLastExpanded(controller.getTitledPane().isExpanded());
				stage.setOnCloseRequest(new EventHandler<WindowEvent>()
				{					
					@Override
					public void handle(WindowEvent event)
					{
						status.setRequestedVisibility(status.getPreviousVisibility());						
						updateVisiblePanes();
						updateMenus();
						updateMinHeights();
					}
				});
				
				controller.getTitledPane().setExpanded(true);
				controller.getTitledPane().setCollapsible(false);
				stage.show();
			}
			// If set to be shown
			else if (status.getVisibility().equals(PaneVisibilityStatus.ATTACHED))
			{
				// logger.info("Show; contains = " + splitPane.getItems().contains(controller.getTitledPane()));
				
				// Show
				if (!splitPane.getItems().contains(controller.getTitledPane()))
				{
					insertPane(controller);
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
