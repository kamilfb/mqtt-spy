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
import java.util.Queue;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.MqttContent;
import pl.baczkowicz.mqttspy.events.EventManager;
import pl.baczkowicz.mqttspy.events.observers.MessageFormatChangeObserver;
import pl.baczkowicz.mqttspy.events.observers.MessageAddedObserver;
import pl.baczkowicz.mqttspy.events.queuable.ui.MqttSpyUIEvent;
import pl.baczkowicz.mqttspy.storage.BasicMessageStore;
import pl.baczkowicz.mqttspy.storage.ManagedMessageStoreWithFiltering;
import pl.baczkowicz.mqttspy.ui.properties.MqttContentProperties;
import pl.baczkowicz.mqttspy.ui.search.SearchOptions;

/**
 * Controller for the search pane.
 */
public class SearchPaneController implements Initializable, MessageFormatChangeObserver, MessageAddedObserver
{
	final static Logger logger = LoggerFactory.getLogger(SearchPaneController.class);
	
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
	
	private EventManager eventManager;
	
	private ManagedMessageStoreWithFiltering store; 
	
	private BasicMessageStore foundMessageStore;

	private Tab tab;

	private final ObservableList<MqttContentProperties> foundMessages = FXCollections.observableArrayList();

	private Queue<MqttSpyUIEvent> uiEventQueue;

	private int seachedCount;
	
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
	
	@FXML
	private void toggleAutoRefresh()
	{
		updateTabTitle();		
	}
	
	public void requestSearchFocus()
	{
		searchField.requestFocus();
	}
	
	private boolean matches(final String value, final String substring)
	{
		if (caseSensitiveCheckBox.isSelected())
		{
			return value.contains(substring);
		}
		
		return value.toLowerCase().contains(substring.toLowerCase());
	}
	
	private boolean processMessage(final MqttContent message)
	{
		seachedCount++;
		if (matches(message.getFormattedPayload(store.getFormatter()), searchField.getText()))
		{
			foundMessage(message);
			return true;
		}
		
		return false;
	}
	
	private void foundMessage(final MqttContent message)
	{
		foundMessages.add(0, new MqttContentProperties(message, store.getFormatter()));
		
		// If an old message has been deleted from the store, remove it from the list as well 
		if (foundMessageStore.storeMessage(message) != null)
		{
			foundMessages.remove(foundMessages.size() - 1);
		}
	}
	
	private void clearMessages()
	{
		seachedCount = 0;
		foundMessages.clear();
		foundMessageStore.clear();
	}
	
	@FXML
	private void search()
	{
		clearMessages();		
		
		for (int i = store.getMessages().size() - 1; i >= 0; i--)
		{
			processMessage(store.getMessages().get(i));
		}
		
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
		
		title.getChildren().add(new Label("Search for: \"" + searchField.getText() + "\""
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
			final boolean matchingSearch = processMessage(message); 
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

	public void init()
	{
		eventManager.registerFormatChangeObserver(this, store);
		
		foundMessageStore = new BasicMessageStore("search-" + store.getName(), 
				store.getMessageList().getPreferredSize(), store.getMessageList().getMaxSize(), uiEventQueue, eventManager);
		foundMessageStore.setFormatter(store.getFormatter());
		
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
	
	public void setUIQueue(final Queue<MqttSpyUIEvent> uiEventQueue)
	{
		this.uiEventQueue = uiEventQueue;
	}
}
