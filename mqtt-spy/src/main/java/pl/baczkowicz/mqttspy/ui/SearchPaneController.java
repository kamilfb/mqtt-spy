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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttContent;
import pl.baczkowicz.mqttspy.events.EventManager;
import pl.baczkowicz.mqttspy.events.observers.MessageAddedObserver;
import pl.baczkowicz.mqttspy.events.observers.MessageFormatChangeObserver;
import pl.baczkowicz.mqttspy.scripts.Script;
import pl.baczkowicz.mqttspy.scripts.ScriptManager;
import pl.baczkowicz.mqttspy.storage.FilteredMessageStore;
import pl.baczkowicz.mqttspy.storage.ManagedMessageStoreWithFiltering;
import pl.baczkowicz.mqttspy.ui.properties.MqttContentProperties;
import pl.baczkowicz.mqttspy.ui.search.InlineScriptMatcher;
import pl.baczkowicz.mqttspy.ui.search.ScriptMatcher;
import pl.baczkowicz.mqttspy.ui.search.SearchMatcher;
import pl.baczkowicz.mqttspy.ui.search.SearchOptions;
import pl.baczkowicz.mqttspy.ui.search.SimplePayloadMatcher;
import pl.baczkowicz.mqttspy.ui.search.UniqueContentOnlyFilter;

/**
 * Controller for the search pane.
 */
public class SearchPaneController implements Initializable, MessageFormatChangeObserver, MessageAddedObserver
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(SearchPaneController.class);
	
	private final static int MAX_SEARCH_VALUE_CHARACTERS = 15;
	
	@FXML
	private TextField searchField;
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private MessageController messagePaneController;
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private MessageListTableController messageListTablePaneController;
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private MessageNavigationController messageNavigationPaneController;
	
	@FXML
	private CheckBox autoRefreshCheckBox;
	
	@FXML
	private CheckBox caseSensitiveCheckBox;
	
	@FXML
	private ToggleGroup searchMethod;
	
	@FXML
	private Menu searchWithScriptsMenu;
	
	@FXML
	private RadioMenuItem defaultSearch;
	
	@FXML
	private RadioMenuItem inlineScriptSearch;
	
	@FXML
	private Label textLabel;
	
	@FXML 
	private SplitPane splitPane;
	
	private EventManager eventManager;
	
	private ManagedMessageStoreWithFiltering store; 
	
	private FilteredMessageStore foundMessageStore;

	private Tab tab;

	private final ObservableList<MqttContentProperties> foundMessages = FXCollections.observableArrayList();

	// private Queue<MqttSpyUIEvent> uiEventQueue;

	private int seachedCount;

	private MqttAsyncConnection connection;

	private ScriptManager scriptManager;

	private UniqueContentOnlyFilter uniqueContentOnlyFilter;
	
	public void initialize(URL location, ResourceBundle resources)
	{
		searchField.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() 
		{
	        @Override
	        public void handle(KeyEvent keyEvent) 
	        {
	        	switch (keyEvent.getCode())
	        	{
		        	case ENTER:
		        	{
		        		search();
		        		break;
		        	}		        	
		        	default:
		        		break;
	        	}
	        }
	    });
	}
	
	public void init()
	{
		eventManager.registerFormatChangeObserver(this, store);
		
//		foundMessageStore = new BasicMessageStore("search-" + store.getName(), 
//				store.getMessageList().getPreferredSize(), store.getMessageList().getMaxSize(), uiEventQueue, eventManager);
//		foundMessageStore.setFormatter(store.getFormatter());
		foundMessageStore = new FilteredMessageStore(store.getMessageList(), store.getMessageList().getPreferredSize(), store.getMessageList().getMaxSize(), 
				"search-" + store.getName(), store.getFormatter());
		
		uniqueContentOnlyFilter = new UniqueContentOnlyFilter();
		uniqueContentOnlyFilter.setUniqueContentOnly(messageNavigationPaneController.getUniqueOnlyMenu().isSelected());
		foundMessageStore.addMessageFilter(uniqueContentOnlyFilter);
		messageNavigationPaneController.getUniqueOnlyMenu().setOnAction(new EventHandler<ActionEvent>()
		{			
			@Override
			public void handle(ActionEvent event)
			{
				uniqueContentOnlyFilter.setUniqueContentOnly(messageNavigationPaneController.getUniqueOnlyMenu().isSelected());
				//store.getFilteredMessageStore().runFilter(uniqueContentOnlyFilter);
				search();
				eventManager.navigateToFirst(foundMessageStore);				
			}
		});
		
		messageListTablePaneController.setItems(foundMessages);
		messageListTablePaneController.setStore(foundMessageStore);
		messageListTablePaneController.setEventManager(eventManager);
		messageListTablePaneController.init();
		eventManager.registerChangeMessageIndexObserver(messageListTablePaneController, foundMessageStore);
		
		messagePaneController.setStore(foundMessageStore);
		messagePaneController.init();		
		// The search pane's message browser wants to know about changing indices and format
		eventManager.registerChangeMessageIndexObserver(messagePaneController, foundMessageStore);
		eventManager.registerFormatChangeObserver(messagePaneController, foundMessageStore);
		
		messageNavigationPaneController.setStore(foundMessageStore);		
		messageNavigationPaneController.setEventManager(eventManager);
		messageNavigationPaneController.init();		
		// The search pane's message browser wants to know about show first, index change and update index events 
		eventManager.registerChangeMessageIndexObserver(messageNavigationPaneController, foundMessageStore);
		eventManager.registerChangeMessageIndexFirstObserver(messageNavigationPaneController, foundMessageStore);
		eventManager.registerIncrementMessageIndexObserver(messageNavigationPaneController, foundMessageStore);
		
		scriptManager = new ScriptManager(null, null, connection);
		refreshList();
	}
	
	private void refreshList()
	{
		// If in offline mode
		if (connection == null)
		{
			searchWithScriptsMenu.setDisable(true);
		}
		else if (connection.getProperties() == null)
		{
			logger.warn("Connection's properties are null");
		}
		else if (connection.getProperties().getConfiguredProperties() == null)
		{
			logger.warn("Connection's configured properties are null");
		}
		else
		{		
			final String directory = connection.getProperties().getConfiguredProperties().getSearchScripts();
			
			if (directory != null && !directory.isEmpty())
			{
				scriptManager.addScripts(directory);
				onScriptListChange();	
			}
		}
	}
	
	public void onScriptListChange()
	{
		final Collection<Script> scripts = scriptManager.getScripts().values();
		
		final List<Script> pubScripts = new ArrayList<>();
		
		for (final Script script : scripts)
		{
			pubScripts.add(script);
		}
		
		NewPublicationController.updateScriptList(pubScripts, searchWithScriptsMenu, searchMethod, "Search with '%s' script", new EventHandler<ActionEvent>()
		{			
			@Override
			public void handle(ActionEvent event)
			{				
				onScriptSearch(((Script) searchMethod.getSelectedToggle().getUserData()).getName());				
			}
		});
	}
	
	@FXML
	private void toggleAutoRefresh()
	{
		updateTabTitle();		
	}
	
	@FXML
	private void onMessagePayloadSearch()
	{
		textLabel.setText("Text to find");
		searchField.setText("");
		searchField.setPromptText("type some text and press Enter to search");
		searchField.setDisable(false);
		caseSensitiveCheckBox.setVisible(true);
	}
	
	private void onScriptSearch(final String scriptName)
	{
		textLabel.setText("Search with script");
		searchField.setText(scriptName);
		searchField.setDisable(true);
		caseSensitiveCheckBox.setVisible(false);
	}
	
	@FXML
	private void onInlineScriptSearch()
	{
		textLabel.setText("Inline script");
		searchField.setText("");
		searchField.setPromptText("type inline JavaScript and press Enter to search");
		searchField.setDisable(false);
		caseSensitiveCheckBox.setVisible(false);
	}
	
	public void requestSearchFocus()
	{
		searchField.requestFocus();
	}
	
	private void processMessages(final List<MqttContent> messages)
	{
		final SearchMatcher matcher = getSearchMatcher();
		
		final int firstIndex = store.getMessages().size() - 1;
		
		for (int i = firstIndex; i >= 0; i--)
		{
			processMessage(store.getMessages().get(i), matcher);
			
			if (firstIndex == i && !matcher.isValid())
			{
				break;
			}
		}		
	}
	
	private SearchMatcher getSearchMatcher()
	{
		if (defaultSearch.isSelected())
		{
			return new SimplePayloadMatcher(searchField.getText(), caseSensitiveCheckBox.isSelected());		
		}
		else if (inlineScriptSearch.isSelected())
		{
			return new InlineScriptMatcher(scriptManager, searchField.getText());
		}
		else
		{
			final Script script = ((Script) searchMethod.getSelectedToggle().getUserData());
			return new ScriptMatcher(scriptManager, script);
		}
	}
	
	private boolean processMessage(final MqttContent message, final SearchMatcher matcher)
	{
		seachedCount++;
		
		boolean found = matcher.matches(message);
		
		if (found)
		{
			messageFound(message);
			return true;
		}
		
		return false;
	}
	
	private void messageFound(final MqttContent message)
	{
		// TODO: filter the found messages too?		
		foundMessages.add(0, new MqttContentProperties(message, store.getFormatter()));
		
		// All the message shouldn't be filtered out
		if (!uniqueContentOnlyFilter.filter(message, foundMessageStore.getMessageList()))
		{
			// If an old message has been deleted from the store, remove it from the list as well 
			if (foundMessageStore.storeMessage(message) != null)
			{
				foundMessages.remove(foundMessages.size() - 1);
			}
		}
		
		//messageNavigationPaneController.setFilterActive(uniqueContentOnlyFilter.isActive());
		// messageNavigationPaneController.updateFilter
	}
	
	private void clearMessages()
	{
		seachedCount = 0;
		foundMessages.clear();
		foundMessageStore.clear();
		uniqueContentOnlyFilter.reset();
	}
	
	@FXML
	private void search()
	{
		clearMessages();		
		
		processMessages(store.getMessages());		
		
		updateTabTitle();	
		messagePaneController.setSearchOptions(new SearchOptions(searchField.getText(), caseSensitiveCheckBox.isSelected()));
		
		eventManager.navigateToFirst(foundMessageStore);
	}
	
	private void updateTabTitle()
	{
		final HBox title = new HBox();
		title.setAlignment(Pos.CENTER);
				
		if (isAutoRefresh())
		{
			final ProgressIndicator progressIndicator = new ProgressIndicator();
			progressIndicator.setMaxSize(15, 15);
			title.getChildren().add(progressIndicator);
			title.getChildren().add(new Label(" "));
		}
		
		// If the search value is too long, show only a substring
		final String searchValue = searchField.getText().length() > MAX_SEARCH_VALUE_CHARACTERS ? 
				searchField.getText().substring(0, MAX_SEARCH_VALUE_CHARACTERS) + "..." : searchField.getText();
		
		title.getChildren().add(new Label("Search: \"" + searchValue + "\""
				+ " [" + foundMessages.size() + " found / " + seachedCount + " searched]"));
		
		tab.setText(null);
		tab.setGraphic(title);		
	}

	@Override
	public void onFormatChange()
	{
		foundMessageStore.setFormatter(store.getFormatter());
		eventManager.notifyFormatChanged(foundMessageStore);
	}

	public void onMessageAdded(final MqttContent message)
	{
		if (autoRefreshCheckBox.isSelected())
		{
			final boolean matchingSearch = processMessage(message, getSearchMatcher()); 
			if (matchingSearch)														
			{
				if (messageNavigationPaneController.showLatest())
				{
					eventManager.navigateToFirst(foundMessageStore);
				}
				else
				{
					eventManager.incrementMessageIndex(foundMessageStore);
				}
			}
			
			updateTabTitle();
		}
	}
	
	public void cleanup()
	{
		disableAutoSearch();
		
		// TODO: need to check this
		eventManager.deregisterFormatChangeObserver(this);
	}

	public void disableAutoSearch()
	{
		autoRefreshCheckBox.setSelected(false);			
		updateTabTitle();
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================

	public void setEventManager(final EventManager eventManager)
	{
		this.eventManager = eventManager;
	}
	
	public void setTab(Tab tab)
	{
		this.tab = tab;
	}
	
	public boolean isAutoRefresh()
	{
		return autoRefreshCheckBox.isSelected();
	}

	public void setStore(final ManagedMessageStoreWithFiltering store)
	{
		this.store = store;
		eventManager.registerMessageAddedObserver(this, store.getMessageList());
	}
	
	// public void setUIQueue(final Queue<MqttSpyUIEvent> uiEventQueue)
	// {
	// this.uiEventQueue = uiEventQueue;
	// }

	public void setConnection(MqttAsyncConnection connection)
	{
		this.connection = connection;		
	}
}
