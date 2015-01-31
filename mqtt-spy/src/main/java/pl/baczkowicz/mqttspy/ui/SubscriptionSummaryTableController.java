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
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Predicate;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.generated.TabbedSubscriptionDetails;
import pl.baczkowicz.mqttspy.events.EventManager;
import pl.baczkowicz.mqttspy.storage.ManagedMessageStoreWithFiltering;
import pl.baczkowicz.mqttspy.ui.properties.SubscriptionTopicSummaryProperties;
import pl.baczkowicz.mqttspy.ui.utils.StylingUtils;
import pl.baczkowicz.mqttspy.ui.utils.UiUtils;

/**
 * Controller for the subscription summary table.
 */
public class SubscriptionSummaryTableController implements Initializable
{
	final static Logger logger = LoggerFactory.getLogger(SubscriptionSummaryTableController.class);

	private ManagedMessageStoreWithFiltering store; 
	
	@FXML
	private TableView<SubscriptionTopicSummaryProperties> filterTable;

	@FXML
	private TableColumn<SubscriptionTopicSummaryProperties, Boolean> showColumn;

	@FXML
	private TableColumn<SubscriptionTopicSummaryProperties, String> topicColumn;
	
	@FXML
	private TableColumn<SubscriptionTopicSummaryProperties, String> contentColumn;

	@FXML
	private TableColumn<SubscriptionTopicSummaryProperties, Integer> messageCountColumn;

	@FXML
	private TableColumn<SubscriptionTopicSummaryProperties, String> lastReceivedColumn;

	private FilteredList<SubscriptionTopicSummaryProperties> filteredData;
	
	private ConnectionController connectionController;
	private EventManager eventManager;

	private Menu filteredTopicsMenu;

	private ObservableList<SubscriptionTopicSummaryProperties> nonFilteredData;
	
	private Set<String> shownTopics = new HashSet<>();
	
	public void initialize(URL location, ResourceBundle resources)
	{				
		// Table
		showColumn.setCellValueFactory(new PropertyValueFactory<SubscriptionTopicSummaryProperties, Boolean>(
				"show"));
		showColumn
				.setCellFactory(new Callback<TableColumn<SubscriptionTopicSummaryProperties, Boolean>, TableCell<SubscriptionTopicSummaryProperties, Boolean>>()
		{
			public TableCell<SubscriptionTopicSummaryProperties, Boolean> call(
					TableColumn<SubscriptionTopicSummaryProperties, Boolean> param)
			{
				final CheckBoxTableCell<SubscriptionTopicSummaryProperties, Boolean> cell = new CheckBoxTableCell<SubscriptionTopicSummaryProperties, Boolean>()
				{
					@Override
					public void updateItem(final Boolean checked, boolean empty)
					{
						super.updateItem(checked, empty);
						if (!isEmpty() && checked != null && this.getTableRow() != null && this.getTableRow().getItem() != null && store != null)
						{
							changeShowProperty((SubscriptionTopicSummaryProperties) this.getTableRow().getItem(), checked);															
						}									
					}
				};
				cell.setAlignment(Pos.TOP_CENTER);
				
				return cell;
			}
		});

		topicColumn.setCellValueFactory(new PropertyValueFactory<SubscriptionTopicSummaryProperties, String>(
				"topic"));

		contentColumn
				.setCellValueFactory(new PropertyValueFactory<SubscriptionTopicSummaryProperties, String>(
						"lastReceivedPayload"));

		messageCountColumn.setCellValueFactory(new PropertyValueFactory<SubscriptionTopicSummaryProperties, Integer>("count"));
		messageCountColumn.setCellFactory(new Callback<TableColumn<SubscriptionTopicSummaryProperties, Integer>, TableCell<SubscriptionTopicSummaryProperties, Integer>>()
		{
			public TableCell<SubscriptionTopicSummaryProperties, Integer> call(
					TableColumn<SubscriptionTopicSummaryProperties, Integer> param)
			{
				final TableCell<SubscriptionTopicSummaryProperties, Integer> cell = new TableCell<SubscriptionTopicSummaryProperties, Integer>()
				{
					@Override
					public void updateItem(Integer item, boolean empty)
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

		lastReceivedColumn.setCellValueFactory(new PropertyValueFactory<SubscriptionTopicSummaryProperties, String>("lastReceivedTimestamp"));
		lastReceivedColumn.setCellFactory(new Callback<TableColumn<SubscriptionTopicSummaryProperties, String>, TableCell<SubscriptionTopicSummaryProperties, String>>()
		{
			public TableCell<SubscriptionTopicSummaryProperties, String> call(
					TableColumn<SubscriptionTopicSummaryProperties, String> param)
			{
				final TableCell<SubscriptionTopicSummaryProperties, String> cell = new TableCell<SubscriptionTopicSummaryProperties, String>()
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

		filterTable
		.setRowFactory(new Callback<TableView<SubscriptionTopicSummaryProperties>, TableRow<SubscriptionTopicSummaryProperties>>()
		{
			public TableRow<SubscriptionTopicSummaryProperties> call(
					TableView<SubscriptionTopicSummaryProperties> tableView)
			{
				final TableRow<SubscriptionTopicSummaryProperties> row = new TableRow<SubscriptionTopicSummaryProperties>()
				{
					@Override
					protected void updateItem(SubscriptionTopicSummaryProperties item, boolean empty)
					{
						super.updateItem(item, empty);
						if (!isEmpty() && item.getSubscription() != null)
						{
							this.setStyle(StylingUtils.createBgRGBString(item.getSubscription()
									.getColor(), getIndex() % 2 == 0 ? 0.8 : 0.6)
									+ " -fx-background-radius: 6; ");
						}
						else
						{
							this.setStyle(null);
						}
					}
				};

				return row;
			}
		});				
	}
	
	private void changeShowProperty(final SubscriptionTopicSummaryProperties item, final boolean checked)
	{
		logger.trace("[{}] Show property changed; topic = {}, show value = {}", store.getName(), item.topicProperty().getValue(), checked);
									
		if (store.getFilteredMessageStore().updateTopicFilter(item.topicProperty().getValue(), checked))
		{
			// Wouldn't get updated properly if this is in the same thread 
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					eventManager.navigateToFirst(store);	
					eventManager.notifyMessageListChanged(store.getMessageList());
				}											
			});
		}				
	}
	
	public void init()
	{		
		filterTable.setContextMenu(createTopicTableContextMenu());		
		
		nonFilteredData = store.getNonFilteredMessageList().getTopicSummary().getObservableMessagesPerTopic();
		
		// Create filtered data set
		filteredData = new FilteredList<>(nonFilteredData);
		
		// Create sortable data set
		final SortedList<SubscriptionTopicSummaryProperties> sortedData = new SortedList<>(filteredData);
		
		// Bind the sortable list with the table
		sortedData.comparatorProperty().bind(filterTable.comparatorProperty());
		
		// Set the data on the table
		filterTable.setItems(sortedData);
		
		filteredData.addListener(new ListChangeListener<SubscriptionTopicSummaryProperties>()
		{
			@Override
			public void onChanged(Change<? extends SubscriptionTopicSummaryProperties> c)
			{
				filteredTopicsMenu.setDisable(filteredData.size() == nonFilteredData.size());
			}
		});
	}
	
	public void refreshRowStyling()
	{
		// To refresh the styling, add and remove an invisible column
		final TableColumn<SubscriptionTopicSummaryProperties, String> column = new TableColumn<>();
		column.setMaxWidth(0);
		column.setPrefWidth(0);
		
		filterTable.getColumns().add(column);
		filterTable.getColumns().remove(column);        
	}
	
	public int getFilteredDataSize()
	{
		return filteredData.size();
	}
	
	public void updateTopicFilter(final String topicFilter)
	{
		synchronized (filteredData)
		{
			filteredData.setPredicate(new Predicate<SubscriptionTopicSummaryProperties>()
			{
				@Override
				public boolean test(final SubscriptionTopicSummaryProperties item)
				{
					// If filter text is empty, display all persons.
		            if (topicFilter == null || topicFilter.isEmpty()) 
		            {
		                return true;
		            }
		            
		            final String topic = item.topicProperty().getValue();
	
		            if (topic.toLowerCase().indexOf(topicFilter.toLowerCase()) != -1) 
		            {
		            	// Filter matches first name
		            	synchronized (shownTopics)
		            	{
		            		shownTopics.add(topic);
		            	}
		                return true; 
		            }
		            
		            // Does not match
		            synchronized (shownTopics)
		            {
		            	shownTopics.remove(topic);
		            }
		            return false; 
				}
			});						
		}
	}
	
	public ContextMenu createTopicTableContextMenu()
	{
		final ContextMenu contextMenu = new ContextMenu();
		
		// Copy topic
		final MenuItem copyTopicItem = new MenuItem("[Topic] Copy to clipboard");
		copyTopicItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					UiUtils.copyToClipboard(item.topicProperty().getValue());
				}
			}
		});
		contextMenu.getItems().add(copyTopicItem);
		
		// Subscribe to topic
		final MenuItem subscribeToTopicItem = new MenuItem("[Topic] Subscribe (and create tab)");
		subscribeToTopicItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					final TabbedSubscriptionDetails subscriptionDetails = new TabbedSubscriptionDetails();
					subscriptionDetails.setTopic(item.topicProperty().getValue());
					subscriptionDetails.setQos(0);
					
					connectionController.getNewSubscriptionPaneController().subscribe(subscriptionDetails, true);
				}
			}
		});
		contextMenu.getItems().add(subscribeToTopicItem);

		// Separator
		contextMenu.getItems().add(new SeparatorMenuItem());
		
		// Copy content
		final MenuItem copyContentItem = new MenuItem("[Content] Copy to clipboard");
		copyContentItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					UiUtils.copyToClipboard(item.lastReceivedPayloadProperty().getValue());
				}
			}
		});
		contextMenu.getItems().add(copyContentItem);
		
		// Separator
		contextMenu.getItems().add(new SeparatorMenuItem());
		
		// All filters
		final Menu allTopicsMenu = new Menu("[Browse] All topics");
		
		// Apply all filters
		final MenuItem selectAllTopicsItem = new MenuItem("[Browse] Select all topics");
		selectAllTopicsItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					store.setAllShowValues(true);
					eventManager.navigateToFirst(store);
				}
			}
		});
		
		allTopicsMenu.getItems().add(selectAllTopicsItem);		
		
		// Toggle all filters
		final MenuItem toggleAllTopicsItem = new MenuItem("[Browse] Toggle all topics");
		toggleAllTopicsItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					store.toggleAllShowValues();
					eventManager.navigateToFirst(store);
				}
			}
		});
		allTopicsMenu.getItems().add(toggleAllTopicsItem);
		
		// Remove all filters
		final MenuItem removeAllTopicsItem = new MenuItem("[Browse] Clear all selected topics");
		removeAllTopicsItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					store.setAllShowValues(false);
					eventManager.navigateToFirst(store);
				}
			}
		});
		allTopicsMenu.getItems().add(removeAllTopicsItem);
		contextMenu.getItems().add(allTopicsMenu);	
		
		// Filtered topics
		filteredTopicsMenu = new Menu("[Browse] Filtered topics");
		
		// Apply filtered filters
		final MenuItem selectFilteredTopicsItem = new MenuItem("[Browse] Select filtered topics");
		selectFilteredTopicsItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					store.setShowValues(true, shownTopics);
					eventManager.navigateToFirst(store);
				}
			}
		});		
		filteredTopicsMenu.getItems().add(selectFilteredTopicsItem);		
		
		// Toggle filtered filters
		final MenuItem toggleFilteredTopicsItem = new MenuItem("[Browse] Toggle filtered topics");
		toggleFilteredTopicsItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					store.toggleShowValues(shownTopics);
					eventManager.navigateToFirst(store);
				}
			}
		});
		filteredTopicsMenu.getItems().add(toggleFilteredTopicsItem);
		
		// Remove filtered filters
		final MenuItem removeFilteredTopicsItem = new MenuItem("[Browse] Clear selected topics");
		removeFilteredTopicsItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					store.setShowValues(false, shownTopics);
					eventManager.navigateToFirst(store);
				}
			}
		});
		filteredTopicsMenu.getItems().add(removeFilteredTopicsItem);
		contextMenu.getItems().add(filteredTopicsMenu);	
		
		// Only this topic
		final MenuItem selectOnlyThisItem = new MenuItem("[Browse] Select only this");
		selectOnlyThisItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					store.setAllShowValues(false);
					store.setShowValue(item.topicProperty().getValue(), true);
					eventManager.navigateToFirst(store);
				}
			}
		});
		contextMenu.getItems().add(selectOnlyThisItem);

		return contextMenu;
	}
		
	public void setEventManager(final EventManager eventManager)
	{
		this.eventManager = eventManager;
	}
	
	public void setStore(final ManagedMessageStoreWithFiltering store)
	{
		this.store = store;
	}
	
	public void setConnectionController(final ConnectionController connectionController)
	{
		this.connectionController = connectionController;
	}
}
