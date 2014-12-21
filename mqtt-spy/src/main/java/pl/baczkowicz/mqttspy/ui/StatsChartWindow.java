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

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Very experimental stuff. Controller for the statistics charts.
 */
public class StatsChartWindow extends AnchorPane
{
	private XYChart.Series<Number, Number> hourDataSeries;
	private XYChart.Series<Number, Number> minuteDataSeries;
	private NumberAxis xAxis;
	private Timeline animation;

	private double hours = 0;
	private double minutes = 0;
	private double timeInHours = 0;
	private double prevY = 10;
	private double y = 10;

	private void init(Stage primaryStage)
	{
		Group root = new Group();
		primaryStage.setScene(new Scene(root));
		root.getChildren().add(createChart());
		
		// Create timeline to add new data every second
		animation = new Timeline();
		animation.getKeyFrames().add(
				new KeyFrame(Duration.millis(1000 / 60), new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent actionEvent)
					{
						// 6 minutes data per frame
						for (int count = 0; count < 6; count++)
						{
							nextTime();
							plotTime();
						}
					}
				}));
		animation.setCycleCount(Animation.INDEFINITE);
	}

	protected LineChart<Number, Number> createChart()
	{
		// TODO: change range
		xAxis = new NumberAxis(0, 24, 3);
		// final NumberAxis yAxis = new NumberAxis(0, 100, 10);
		final NumberAxis yAxis = new NumberAxis();
		yAxis.setLowerBound(0);
		final LineChart<Number, Number> lc = new LineChart<Number, Number>(xAxis, yAxis);
		
		// Setup chart
		lc.setId("lineStockDemo");
		lc.setCreateSymbols(false);
		// lc.setAnimated(false);
		// lc.setLegendVisible(false);
		lc.setTitle("Published and received messages (per connection per second)");
		xAxis.setLabel("Time");
		xAxis.setForceZeroInRange(false);
		yAxis.setLabel("Messages per second");
		yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, "", null));

		// Add starting data
		hourDataSeries = new XYChart.Series<Number, Number>();
		hourDataSeries.setName("[published]");
		minuteDataSeries = new XYChart.Series<Number, Number>();
		minuteDataSeries.setName("[received]");
		
		// Create some starting data
		hourDataSeries.getData().add(new XYChart.Data<Number, Number>(timeInHours, prevY));
		minuteDataSeries.getData().add(new XYChart.Data<Number, Number>(timeInHours, prevY));
		for (double m = 0; m < (60); m++)
		{
			nextTime();
			plotTime();
		}
		lc.getData().add(minuteDataSeries);
		lc.getData().add(hourDataSeries);
		return lc;
	}

	private void nextTime()
	{
		if (minutes == 59)
		{
			hours++;
			minutes = 0;
		}
		else
		{
			minutes++;
		}
		timeInHours = hours + ((1d / 60d) * minutes);
	}

	private void plotTime()
	{
		if ((timeInHours % 1) == 0)
		{
			// change of hour
			double oldY = y;
			y = prevY - 10 + (Math.random() * 20);
			prevY = oldY;
			while (y < 10 || y > 90)
				y = y - 10 + (Math.random() * 20);
			hourDataSeries.getData().add(new XYChart.Data<Number, Number>(timeInHours, prevY));
		
			// after 1 hours delete old data
			if (timeInHours > 25)
			{
				hourDataSeries.getData().remove(0);
			}
			// every hour after 24 move range 1 hour
			if (timeInHours > 24)
			{
				xAxis.setLowerBound(xAxis.getLowerBound() + 1);
				xAxis.setUpperBound(xAxis.getUpperBound() + 1);
			}
		}
		double min = (timeInHours % 1);
		
		double randomPickVariance = Math.random();
		if (randomPickVariance < 0.3)
		{
			double minY = prevY + ((y - prevY) * min) - 4 + (Math.random() * 8);
			minuteDataSeries.getData().add(new XYChart.Data<Number, Number>(timeInHours, minY));
		}
		else if (randomPickVariance < 0.7)
		{
			double minY = prevY + ((y - prevY) * min) - 6 + (Math.random() * 12);
			minuteDataSeries.getData().add(new XYChart.Data<Number, Number>(timeInHours, minY));
		}
		else if (randomPickVariance < 0.95)
		{
			double minY = prevY + ((y - prevY) * min) - 10 + (Math.random() * 20);
			minuteDataSeries.getData().add(new XYChart.Data<Number, Number>(timeInHours, minY));
		}
		else
		{
			double minY = prevY + ((y - prevY) * min) - 15 + (Math.random() * 30);
			minuteDataSeries.getData().add(new XYChart.Data<Number, Number>(timeInHours, minY));
		}
		
		// After 1 hour delete old data
		if (timeInHours > 25)
		{
			minuteDataSeries.getData().remove(0);
		}
	}

	public void play()
	{
		animation.play();
	}

	public void stop()
	{
		animation.pause();
	}

	public void start(Stage primaryStage)
	{
		init(primaryStage);
		primaryStage.show();
		play();
	}
}
