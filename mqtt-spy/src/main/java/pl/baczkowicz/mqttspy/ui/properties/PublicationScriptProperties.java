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

import java.util.Date;

import pl.baczkowicz.mqttspy.common.generated.ScriptDetails;
import pl.baczkowicz.mqttspy.scripts.Script;
import pl.baczkowicz.mqttspy.scripts.ScriptRunningState;
import pl.baczkowicz.mqttspy.scripts.ScriptTypeEnum;
import pl.baczkowicz.mqttspy.utils.TimeUtils;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * This represents a single row displayed in the scripts table.
 */
public class PublicationScriptProperties extends Script
{
	private SimpleObjectProperty<ScriptRunningState> statusProperty;
	
	private SimpleObjectProperty<ScriptTypeEnum> typeProperty;

	private SimpleStringProperty lastPublishedProperty;

	private SimpleLongProperty countProperty;
	
	private SimpleBooleanProperty repeatProperty;
	
	public PublicationScriptProperties()
	{
		super();
		
		this.statusProperty = new SimpleObjectProperty<ScriptRunningState>(ScriptRunningState.NOT_STARTED);		
		this.typeProperty = new SimpleObjectProperty<ScriptTypeEnum>(ScriptTypeEnum.PUBLICATION);
		this.lastPublishedProperty = new SimpleStringProperty("");
		this.countProperty = new SimpleLongProperty(0);
		this.repeatProperty = new SimpleBooleanProperty(false);
	}
	
	public SimpleObjectProperty<ScriptRunningState> statusProperty()
	{
		return this.statusProperty;
	}
	
	public SimpleObjectProperty<ScriptTypeEnum> typeProperty()
	{
		return this.typeProperty;
	}
	
	public SimpleStringProperty lastPublishedProperty()
	{
		return this.lastPublishedProperty;
	}
	
	public SimpleLongProperty countProperty()
	{
		return this.countProperty;
	}
	
	public SimpleBooleanProperty repeatProperty()
	{
		return this.repeatProperty;
	}
	
	public boolean isRepeat()
	{
		return this.repeatProperty.getValue();
	}
	
	public void setScriptDetails(final ScriptDetails scriptDetails)
	{
		super.setScriptDetails(scriptDetails);
		this.repeatProperty.set(scriptDetails.isRepeat());		
	}
	
	public void setMessagesPublished(final long messageCount)
	{
		super.setMessagesPublished(messageCount);
		this.countProperty.set(getMessagesPublished());
	}

	public void setLastPublished(final Date lastPublished)
	{
		super.setLastPublished(lastPublished);	
		this.lastPublishedProperty.set(TimeUtils.DATE_WITH_SECONDS_SDF.format(lastPublished));
	}
	
	public void setStatus(final ScriptRunningState state)
	{
		super.setStatus(state);
		this.statusProperty().set(getStatus());
	}
}
