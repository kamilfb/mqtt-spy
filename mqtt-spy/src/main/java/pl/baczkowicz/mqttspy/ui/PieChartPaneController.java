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
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import pl.baczkowicz.mqttspy.ui.properties.SubscriptionTopicSummaryProperties;

/**
 * Controller for pie chart pane.
 */
public class PieChartPaneController implements Initializable, ListChangeListener<SubscriptionTopicSummaryProperties>
{
	private final static Logger logger = LoggerFactory.getLogger(PieChartPaneController.class);
	
	private static boolean lastAutoRefresh = true;

	@FXML
	private AnchorPane chartPane;
	
	@FXML
	private Label showRangeLabel;
		
	@FXML
	private Button refreshButton;
	
	@FXML
	private CheckBox autoRefreshCheckBox;	
		
	private PieChart pieChart;
	
	private ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
	
	private Map<String, PieChart.Data> pieChartDataMapping = new HashMap<>();

	private ObservableList<SubscriptionTopicSummaryProperties> observableList;

	public void initialize(URL location, ResourceBundle resources)
	{
		autoRefreshCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				lastAutoRefresh = autoRefreshCheckBox.isSelected();
				// Only refresh when auto-refresh enabled
				if (lastAutoRefresh)
				{
					refresh();				
				}
			}
		});		
	}		

	public void init()
	{						
		pieChart = new PieChart(pieChartData);
		chartPane.getChildren().add(pieChart);
		AnchorPane.setBottomAnchor(pieChart, 0.0);
		AnchorPane.setLeftAnchor(pieChart, 0.0);
		AnchorPane.setTopAnchor(pieChart, 45.0);
		AnchorPane.setRightAnchor(pieChart, 0.0);
						
		autoRefreshCheckBox.setSelected(lastAutoRefresh);
				
		observableList.addListener(this);
	}
	
	public void cleanup()
	{
		observableList.removeListener(this);
	}
	
	@Override
	public void onChanged(final ListChangeListener.Change<? extends SubscriptionTopicSummaryProperties> c)
	{
		logger.info("New data available");
		if (autoRefreshCheckBox.isSelected())
		{
			synchronized (pieChartData)
			{
				logger.info("New data available");
				refreshFromList(observableList);
			}
		}
	}
	
	@FXML
	private void refresh()
	{		
		refreshFromList(observableList);
	}
	
	private void refreshFromList(final Collection<SubscriptionTopicSummaryProperties> list)
	{
		synchronized (pieChartData)
		{
			for (final SubscriptionTopicSummaryProperties newValue : list)
			{
				final String topic = newValue.topicProperty().getValue();
				pieChartDataMapping.put(topic, new PieChart.Data(topic, newValue.countProperty().getValue()));
			}
			
			pieChartData.setAll(pieChartDataMapping.values());
		}
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setObservableList(final ObservableList<SubscriptionTopicSummaryProperties> observableList)
	{
		this.observableList = observableList;		
	}	
}
