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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import pl.baczkowicz.mqttspy.connectivity.MqttContent;
import pl.baczkowicz.mqttspy.connectivity.MqttSubscription;
import pl.baczkowicz.mqttspy.events.EventManager;
import pl.baczkowicz.mqttspy.events.observers.MessageAddedObserver;
import pl.baczkowicz.mqttspy.storage.ManagedMessageStoreWithFiltering;
import pl.baczkowicz.mqttspy.ui.properties.MessageLimitProperties;
import pl.baczkowicz.mqttspy.ui.utils.DialogUtils;
import pl.baczkowicz.mqttspy.ui.utils.StylingUtils;

/**
 * Controller for stats pane.
 */
public class StatsPaneController implements Initializable, MessageAddedObserver
{
	/** Diagnostic logger. */
	// private final static Logger logger = LoggerFactory.getLogger(StatsPaneController.class);

	private static final int ONE_SECOND = 1000;

	@FXML
	private AnchorPane statsPane;
	
	@FXML
	private ComboBox<MessageLimitProperties> showRangeBox;
	
	@FXML
	private Button refreshButton;
	
	@FXML
	private CheckBox autoRefreshCheckBox;
	
	private ManagedMessageStoreWithFiltering store;

	private EventManager eventManager;
	
	private MqttSubscription subscription;

	private Set<String> topics;
	
	private Map<String, List<MqttContent>> chartData = new HashMap<>();
	
	private Map<String, Series<Number, Number>> topicToSeries = new HashMap<>();
	
	private LineChart<Number, Number> lineChart;
	
	private boolean warningShown;
	
	public void initialize(URL location, ResourceBundle resources)
	{

		// Axis and chart
		final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        
		final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        
        xAxis.setForceZeroInRange(false);
        xAxis.setTickLabelFormatter(new StringConverter<Number>()
		{
			@Override
			public String toString(Number object)
			{
				final Date date = new Date(object.longValue());
				return dateFormat.format(date);
			}
			
			@Override
			public Number fromString(String string)
			{
				return null;
			}
		});
        yAxis.setForceZeroInRange(false);
		lineChart = new LineChart<>(xAxis, yAxis);
	}		

	public void init()
	{
		showRangeBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				refresh();
			}
		});
		
		showRangeBox.setCellFactory(new Callback<ListView<MessageLimitProperties>, ListCell<MessageLimitProperties>>()
				{
					@Override
					public ListCell<MessageLimitProperties> call(ListView<MessageLimitProperties> l)
					{
						return new ListCell<MessageLimitProperties>()
						{
							@Override
							protected void updateItem(MessageLimitProperties item, boolean empty)
							{
								super.updateItem(item, empty);
								if (item == null || empty)
								{
									setText(null);
								}
								else
								{									
									setText(item.getName());
								}
							}
						};
					}
				});
		showRangeBox.setConverter(new StringConverter<MessageLimitProperties>()
		{
			@Override
			public String toString(MessageLimitProperties item)
			{
				if (item == null)
				{
					return null;
				}
				else
				{
					return item.getName();
				}
			}

			@Override
			public MessageLimitProperties fromString(String id)
			{
				return null;
			}
		});
		showRangeBox.getItems().clear();
		showRangeBox.getItems().add(new MessageLimitProperties("All", 0, 0));
		showRangeBox.getItems().add(new MessageLimitProperties("10 messages", 10, 0));
		showRangeBox.getItems().add(new MessageLimitProperties("50 messages", 50, 0));
		showRangeBox.getItems().add(new MessageLimitProperties("100 messages", 100, 0));
		showRangeBox.getItems().add(new MessageLimitProperties("1000 messages", 1000, 0));
		showRangeBox.getItems().add(new MessageLimitProperties("1 minute", 0, 60 * ONE_SECOND));
		showRangeBox.getItems().add(new MessageLimitProperties("5 minutes", 0, 300 * ONE_SECOND));
		showRangeBox.getItems().add(new MessageLimitProperties("30 minutes", 0, 1800 * ONE_SECOND));
		showRangeBox.getItems().add(new MessageLimitProperties("1 hour", 0, 3600 * ONE_SECOND));
		showRangeBox.getItems().add(new MessageLimitProperties("24 hours", 0, 86400 * ONE_SECOND));
		
		
		if (subscription != null)
		{
			refreshButton.setStyle(StylingUtils.createBaseRGBString(subscription.getColor()));
		}
		
		statsPane.getChildren().add(lineChart);
		AnchorPane.setBottomAnchor(lineChart, 0.0);
		AnchorPane.setLeftAnchor(lineChart, 0.0);
		AnchorPane.setTopAnchor(lineChart, 45.0);
		AnchorPane.setRightAnchor(lineChart, 0.0);
		
		// This will perform a refresh
		showRangeBox.setValue(showRangeBox.getItems().get(2));
		eventManager.registerMessageAddedObserver(this, store.getMessageList());
	}
	
	public void cleanup()
	{
		eventManager.deregisterMessageAddedObserver(this);
	}
	
	private void divideMessagesByTopic(final Set<String> topics)
	{
		chartData.clear();
		for (final MqttContent message : store.getMessages())
		{
			final String topic = message.getTopic();
			if (topics.contains(topic))
			{
				if (chartData.get(topic) == null)
				{
					chartData.put(topic, new ArrayList<MqttContent>());
				}
				
				chartData.get(topic).add(message);
			}
		}
	}
	
	private void addMessageToSeries(final Series<Number, Number> series, final MqttContent message)
	{
		try
    	{
    		series.getData().add(new XYChart.Data(
    			message.getDate().getTime(), 
    			Double.valueOf(message.getFormattedPayload())));
    	}
    	catch (NumberFormatException e)
    	{
    		if (!warningShown)
    		{
    			DialogUtils.showWarning(
    					"Invalid content", 
    					"Message on topic \"" + message.getTopic() + "\" with payload \"" 
    					+ message.getFormattedPayload() 
    					+ "\" cannot be converted to a number - ignoring all invalid messages.");
    			warningShown = true;
    		}
    	}
	}
	
	@FXML
	private void refresh()
	{		
		divideMessagesByTopic(topics);
		lineChart.getData().clear();
		topicToSeries.clear();
		
		for (final String topic : topics)
		{
			final Series<Number, Number> series = new XYChart.Series<>();
			topicToSeries.put(topic, series);
	        series.setName(topic);
	        
	        final MessageLimitProperties limit = showRangeBox.getValue();
	        final int itemsAvailable = chartData.get(topic).size();
	        
	        // Limit by number
	        int startIndex = 0;	        
	        if (limit.getMessageLimit() > 0 && limit.getMessageLimit() < itemsAvailable)
	        {
	        	startIndex = itemsAvailable - limit.getMessageLimit(); 
	        }
	        
	        // Limit by time
	        final Date now = new Date();
	        
	        for (int i = startIndex; i < itemsAvailable; i++)
	        {
	        	final MqttContent message = chartData.get(topic).get(chartData.get(topic).size() - i - 1);
	        	
	        	if (limit.getTimeLimit() > 0 && (message.getDate().getTime() + limit.getTimeLimit() < now.getTime()))
	        	{
	        		continue;
	        	}
	        	
	        	addMessageToSeries(series, message);
	        }
	        
	        lineChart.getData().add(series);
		}
	}
	
	@Override
	public void onMessageAdded(final MqttContent message)
	{
		// TODO: is that ever deregistered?
		final String topic = message.getTopic();
		final MessageLimitProperties limit = showRangeBox.getValue();
		
		if (autoRefreshCheckBox.isSelected() && topics.contains(topic))
		{			
			// Apply message limit			
			if (limit.getMessageLimit() > 0 && chartData.get(topic).size() == limit.getMessageLimit())
			{
				chartData.get(topic).remove(0);
				topicToSeries.get(topic).getData().remove(0);		
			}
			
			// Apply time limit
			final Date now = new Date();
			if (limit.getTimeLimit() > 0)
			{
				MqttContent oldestMessage = chartData.get(topic).get(0);
				while (oldestMessage.getDate().getTime() + limit.getTimeLimit() < now.getTime())
				{
					chartData.get(topic).remove(0);
					topicToSeries.get(topic).getData().remove(0);
					
					if (chartData.get(topic).size() == 0)
					{
						break;
					}
					oldestMessage = chartData.get(topic).get(0);
				}				
			}
			
			// Add the new message
			chartData.get(topic).add(message);
			addMessageToSeries(topicToSeries.get(topic), message);
		}
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================

	public void setTopics(final Set<String> topics)
	{
		this.topics = topics;
	}
	
	public void setSubscription(MqttSubscription subscription)
	{
		this.subscription = subscription;		
	}
	
	public void setEventManager(final EventManager eventManager)
	{
		this.eventManager = eventManager;
	}
	
	public void setStore(final ManagedMessageStoreWithFiltering store)
	{
		this.store = store;		
	}
}
