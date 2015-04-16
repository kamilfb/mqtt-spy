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
package pl.baczkowicz.mqttspy.ui.properties;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import pl.baczkowicz.mqttspy.configuration.generated.FormatterDetails;
import pl.baczkowicz.mqttspy.storage.UiMqttMessage;

/**
 * Properties displayed in the subscription topic summary table.
 */
public class SubscriptionTopicSummaryProperties extends MqttContentProperties
{
	/** Whether to include this topic in the list of messages for browsing. */
	private BooleanProperty show;
	
	/** How many messages have been received for that topic - subject to clean-up. */
	private IntegerProperty count;

	/**
	 * Creates SubscriptionTopicSummaryProperties with the supplied parameters.
	 * 
	 * @param show Whether to include this topic in the list of messages for browsing
	 * @param count How many messages have been received for that topic - subject to clean-up
	 * @param message The last message
	 * @param format The formatter details
	 */
	public SubscriptionTopicSummaryProperties(final Boolean show, final Integer count, final UiMqttMessage message, final FormatterDetails format)
	{
		super(message, format);
		
		this.show = new SimpleBooleanProperty(show);	               
        this.count = new SimpleIntegerProperty(count);                    
	}

	/**
	 * 'Show' property.
	 * 
	 * @return 'Show' property as BooleanProperty
	 */
	public BooleanProperty showProperty()
	{
		return show;
	}

	/** 
	 * Count property.
	 *  
	 * @return Count property as IntegerProperty  
	 */	
	public IntegerProperty countProperty()
	{
		return count;
	}

	/**
	 * Setter for the count property.
	 * 
	 * @param count The new count value
	 */
	public void setCount(final Integer count)
	{
		this.count.set(count);
	}
}
