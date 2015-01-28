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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.events.EventManager;
import pl.baczkowicz.mqttspy.events.observers.ScriptStateChangeObserver;
import pl.baczkowicz.mqttspy.scripts.InteractiveScriptManager;
import pl.baczkowicz.mqttspy.scripts.ScriptRunningState;
import pl.baczkowicz.mqttspy.scripts.ScriptTypeEnum;
import pl.baczkowicz.mqttspy.ui.panes.TitledPaneController;
import pl.baczkowicz.mqttspy.ui.panes.TitledPaneStatus;
import pl.baczkowicz.mqttspy.ui.properties.PublicationScriptProperties;

/**
 * Controller for publications scripts pane.
 */
public class PublicationScriptsController implements Initializable, ScriptStateChangeObserver, TitledPaneController
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(PublicationScriptsController.class);
	
	@FXML
	private TableView<PublicationScriptProperties> scriptTable;
	
    @FXML
    private TableColumn<PublicationScriptProperties, String> nameColumn;

    @FXML
    private TableColumn<PublicationScriptProperties, ScriptTypeEnum> typeColumn;
    
    @FXML
    private TableColumn<PublicationScriptProperties, Boolean> repeatColumn;
        
    @FXML
    private TableColumn<PublicationScriptProperties, ScriptRunningState> runningStatusColumn;
    
    @FXML
    private TableColumn<PublicationScriptProperties, String> lastPublishedColumn;
    
    @FXML
    private TableColumn<PublicationScriptProperties, Long> messageCountColumn;
		
	private MqttAsyncConnection connection;

	private InteractiveScriptManager scriptManager;

	private EventManager eventManager;

	private Map<ScriptTypeEnum, ContextMenu> contextMenus = new HashMap<>();
	
	/** Created pane status with index 1 (the second pane). */
	private final TitledPaneStatus paneStatus = new TitledPaneStatus(1);

	private TitledPane pane;

	public void initialize(URL location, ResourceBundle resources)
	{
		// Table
		nameColumn.setCellValueFactory(new PropertyValueFactory<PublicationScriptProperties, String>("name"));
		
		typeColumn.setCellValueFactory(new PropertyValueFactory<PublicationScriptProperties, ScriptTypeEnum>("type"));
		typeColumn
			.setCellFactory(new Callback<TableColumn<PublicationScriptProperties, ScriptTypeEnum>, TableCell<PublicationScriptProperties, ScriptTypeEnum>>()
		{
			public TableCell<PublicationScriptProperties, ScriptTypeEnum> call(
					TableColumn<PublicationScriptProperties, ScriptTypeEnum> param)
			{
				final TableCell<PublicationScriptProperties, ScriptTypeEnum> cell = new TableCell<PublicationScriptProperties, ScriptTypeEnum>()				
				{
					@Override
					public void updateItem(ScriptTypeEnum item, boolean empty)
					{
						super.updateItem(item, empty);
						
						if (!isEmpty())
						{
							setText(item.toString());
						}
						else
						{
							setText(null);
							setGraphic(null);
						}
					}
				};
				cell.setAlignment(Pos.TOP_CENTER);
				
				return cell;
			}
		});

		repeatColumn.setCellValueFactory(new PropertyValueFactory<PublicationScriptProperties, Boolean>("repeat"));
		repeatColumn
		.setCellFactory(new Callback<TableColumn<PublicationScriptProperties, Boolean>, TableCell<PublicationScriptProperties, Boolean>>()
		{
			public TableCell<PublicationScriptProperties, Boolean> call(
					TableColumn<PublicationScriptProperties, Boolean> param)
			{
				final CheckBoxTableCell<PublicationScriptProperties, Boolean> cell = new CheckBoxTableCell<PublicationScriptProperties, Boolean>()
				{
					@Override
					public void updateItem(final Boolean checked, boolean empty)
					{
						super.updateItem(checked, empty);
						if (!isEmpty() && checked != null && this.getTableRow() != null && this.getTableRow().getItem() != null)
						{
							final PublicationScriptProperties item = (PublicationScriptProperties) this.getTableRow().getItem();
							
							// Anything but subscription scripts can be repeated
							if (!ScriptTypeEnum.SUBSCRIPTION.equals(item.typeProperty().getValue()))
							{	
								this.setDisable(false);
								if (logger.isTraceEnabled())
								{
									logger.trace("Setting repeat for {} to {}", item.getScript().getName(), checked);
								}
								
								item.setRepeat(checked);
							}
							else
							{
								this.setDisable(true);
							}
						}									
					}
				};
				cell.setAlignment(Pos.TOP_CENTER);
				
				return cell;
			}
		});
		
		messageCountColumn.setCellValueFactory(new PropertyValueFactory<PublicationScriptProperties, Long>("count"));
		messageCountColumn
		.setCellFactory(new Callback<TableColumn<PublicationScriptProperties, Long>, TableCell<PublicationScriptProperties, Long>>()
		{
			public TableCell<PublicationScriptProperties, Long> call(
					TableColumn<PublicationScriptProperties, Long> param)
			{
				final TableCell<PublicationScriptProperties, Long> cell = new TableCell<PublicationScriptProperties, Long>()
				{
					@Override
					public void updateItem(Long item, boolean empty)
					{
						super.updateItem(item, empty);
						if (!isEmpty())
						{
							setText(item.toString());
						}
						else
						{
							setText(null);
						}
					}
				};
				cell.setAlignment(Pos.TOP_CENTER);
				
				return cell;
			}
		});
		
		runningStatusColumn.setCellValueFactory(new PropertyValueFactory<PublicationScriptProperties, ScriptRunningState>("status"));
		runningStatusColumn
			.setCellFactory(new Callback<TableColumn<PublicationScriptProperties, ScriptRunningState>, TableCell<PublicationScriptProperties, ScriptRunningState>>()
		{
			public TableCell<PublicationScriptProperties, ScriptRunningState> call(
					TableColumn<PublicationScriptProperties, ScriptRunningState> param)
			{
				final TableCell<PublicationScriptProperties, ScriptRunningState> cell = new TableCell<PublicationScriptProperties, ScriptRunningState>()				
				{
					@Override
					public void updateItem(ScriptRunningState item, boolean empty)
					{
						super.updateItem(item, empty);
						
						if (!isEmpty())
						{
							setText(item.toString());
						}
						else
						{
							setText(null);
							setGraphic(null);
						}
					}
				};
				cell.setAlignment(Pos.TOP_CENTER);
				
				return cell;
			}
		});

		lastPublishedColumn.setCellValueFactory(new PropertyValueFactory<PublicationScriptProperties, String>("lastPublished"));
		lastPublishedColumn.setCellFactory(new Callback<TableColumn<PublicationScriptProperties, String>, TableCell<PublicationScriptProperties, String>>()
		{
			public TableCell<PublicationScriptProperties, String> call(
					TableColumn<PublicationScriptProperties, String> param)
			{
				final TableCell<PublicationScriptProperties, String> cell = new TableCell<PublicationScriptProperties, String>()
				{
					@Override
					public void updateItem(String item, boolean empty)
					{
						super.updateItem(item, empty);
						if (!isEmpty())
						{
							setText(item.toString());
						}
						else
						{
							setText(null);
						}
					}
				};
				cell.setAlignment(Pos.TOP_CENTER);
				
				return cell;
			}
		});
		
		scriptTable
			.setRowFactory(new Callback<TableView<PublicationScriptProperties>, TableRow<PublicationScriptProperties>>()
			{
				public TableRow<PublicationScriptProperties> call(
						TableView<PublicationScriptProperties> tableView)
				{
					final TableRow<PublicationScriptProperties> row = new TableRow<PublicationScriptProperties>()
					{
						@Override
						protected void updateItem(PublicationScriptProperties item, boolean empty)
						{
							super.updateItem(item, empty);
							if (!isEmpty() && item != null)
							{
								final ContextMenu rowMenu = contextMenus.get(item.typeProperty().getValue());

								this.setContextMenu(rowMenu);
							}
						}
					};
	
					return row;
				}
			});		
		
		// paneStatus.setVisibility(PaneVisibilityStatus.NOT_VISIBLE);
	}
	
	public void init()
	{
		scriptManager = connection.getScriptManager();
		eventManager.registerScriptStateChangeObserver(this, null);
		refreshList();
		scriptTable.setItems(scriptManager.getObservableScriptList());
		
		// Note: subscription scripts don't have context menus because they can't be started/stopped manually - for future, consider enabled/disabled
		contextMenus.put(ScriptTypeEnum.PUBLICATION, createDirectoryTypeScriptTableContextMenu());		
		contextMenus.put(ScriptTypeEnum.BACKGROUND, createDirectoryTypeScriptTableContextMenu());
	}
	
	private void refreshList()
	{
		scriptManager.addScripts(connection.getProperties().getConfiguredProperties().getBackgroundScript(), ScriptTypeEnum.BACKGROUND);		
		scriptManager.addScripts(connection.getProperties().getConfiguredProperties().getPublicationScripts(), ScriptTypeEnum.PUBLICATION);		
		scriptManager.addSubscriptionScripts(connection.getProperties().getConfiguredProperties().getSubscription());
		
		// TODO: move this to script manager?
		eventManager.notifyScriptListChange(connection);
	}

	public void setConnection(MqttAsyncConnection connection)
	{
		this.connection = connection;
	}
	
	public void startScript(final PublicationScriptProperties item)
	{
		scriptManager.runScript(item.getScript(), true);
	}
	
	public void stopScript(final File file)
	{
		scriptManager.stopScriptFile(file);
	}
	
	public void setEventManager(final EventManager eventManager)
	{
		this.eventManager = eventManager;
	}
	
	public ContextMenu createDirectoryTypeScriptTableContextMenu()
	{
		final ContextMenu contextMenu = new ContextMenu();
		
		// Start script
		final MenuItem startScriptItem = new MenuItem("[Script] Start");
		startScriptItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final PublicationScriptProperties item = scriptTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					startScript(item);
				}
			}
		});
		contextMenu.getItems().add(startScriptItem);
		
		// Stop script
		final MenuItem stopScriptItem = new MenuItem("[Script] Stop");
		stopScriptItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final PublicationScriptProperties item = scriptTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					stopScript(item.getScript().getScriptFile());
				}
			}
		});
		contextMenu.getItems().add(stopScriptItem);

		// Separator
		contextMenu.getItems().add(new SeparatorMenuItem());
		
		// Refresh list
		final MenuItem refreshListItem = new MenuItem("[Scripts] Refresh list");
		refreshListItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				refreshList();
			}
		});
		contextMenu.getItems().add(refreshListItem);

		return contextMenu;
	}

	@Override
	public void onScriptStateChange(String scriptName, ScriptRunningState state)
	{
		// TODO: update the context menu - but this requires context menu per row
	}

	@Override
	public TitledPane getTitledPane()
	{
		return pane;
	}

	@Override
	public void setTitledPane(TitledPane pane)
	{
		this.pane = pane;
	}

	@Override
	public TitledPaneStatus getTitledPaneStatus()
	{
		// TODO Auto-generated method stub
		return paneStatus;
	}
}
