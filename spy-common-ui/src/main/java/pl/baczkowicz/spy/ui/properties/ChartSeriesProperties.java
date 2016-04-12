/***********************************************************************************
 * 
 * Copyright (c) 2014 Kamil Baczkowicz
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
package pl.baczkowicz.spy.ui.properties;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import pl.baczkowicz.spy.ui.charts.ChartSeriesStatusEnum;
import pl.baczkowicz.spy.ui.charts.ChartSeriesTypeEnum;

/**
 * This represents a single row displayed in the chart series table.
 */
public class ChartSeriesProperties
{
	private int id;
	
	private SimpleObjectProperty<ChartSeriesStatusEnum> statusProperty;
	
	private SimpleStringProperty nameProperty;
	
	private SimpleStringProperty topicProperty;
	
	private SimpleObjectProperty<ChartSeriesTypeEnum> typeProperty;

	private SimpleStringProperty valueExpressionProperty;	
	
	private SimpleBooleanProperty visibleProperty;

	private String errorMessage;
	
	public ChartSeriesProperties(final int id, final String name, final String topic, final ChartSeriesTypeEnum type, final String valueExpression)
	{
		this.setId(id);
		this.nameProperty = new SimpleStringProperty(name);
		this.topicProperty = new SimpleStringProperty(topic);		
		this.typeProperty = new SimpleObjectProperty<ChartSeriesTypeEnum>(type);
		this.statusProperty = new SimpleObjectProperty<ChartSeriesStatusEnum>(ChartSeriesStatusEnum.NO_MESSAGES);
		this.valueExpressionProperty = new SimpleStringProperty(valueExpression);
		this.visibleProperty = new SimpleBooleanProperty(true);
	}
		
	public SimpleBooleanProperty visibleProperty()
	{
		return this.visibleProperty;
	}
	
	public SimpleObjectProperty<ChartSeriesTypeEnum> typeProperty()
	{
		return this.typeProperty;
	}
	
	public SimpleObjectProperty<ChartSeriesStatusEnum> statusProperty()
	{
		return this.statusProperty;
	}
	
	public SimpleStringProperty topicProperty()
	{
		return this.topicProperty;
	}
	
	public SimpleStringProperty nameProperty()
	{
		return this.nameProperty;
	}
	
	public SimpleStringProperty valueExpressionProperty()
	{
		return this.valueExpressionProperty;
	}

	public String getTopic()
	{
		return topicProperty.get();
	}

	public String getName()
	{
		return nameProperty.get();
	}

	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id)
	{
		this.id = id;
	}

	public void setErrorMessage(final String message)
	{
		this.errorMessage = message;
	}
	
	public String getErrorMessage()
	{
		return errorMessage;
	}
}
