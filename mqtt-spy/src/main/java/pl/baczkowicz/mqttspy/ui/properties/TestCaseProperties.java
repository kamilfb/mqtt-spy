/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
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

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import pl.baczkowicz.mqttspy.scripts.ScriptChangeObserver;
import pl.baczkowicz.mqttspy.scripts.ScriptTypeEnum;
import pl.baczkowicz.mqttspy.testcases.TestCase;
import pl.baczkowicz.mqttspy.testcases.TestCaseStatus;

/**
 * This represents a single row displayed in the test cases table.
 */
public class TestCaseProperties implements ScriptChangeObserver
{
	private SimpleObjectProperty<TestCaseStatus> statusProperty;
	
	private SimpleObjectProperty<ScriptTypeEnum> typeProperty;

	private SimpleStringProperty lastUpdatedProperty;

	private SimpleLongProperty countProperty;
		
	private TestCase script;
	
	public TestCaseProperties(final TestCase script)
	{
		this.script = script;
		
		this.statusProperty = new SimpleObjectProperty<TestCaseStatus>(TestCaseStatus.NOT_RUN);		
		this.typeProperty = new SimpleObjectProperty<ScriptTypeEnum>(ScriptTypeEnum.PUBLICATION);
		this.lastUpdatedProperty = new SimpleStringProperty("");
		this.countProperty = new SimpleLongProperty(0);

		update();
	}
	
	public void update()
	{
		// 
	}
	
	public SimpleObjectProperty<TestCaseStatus> statusProperty()
	{
		return this.statusProperty;
	}
	
	public SimpleObjectProperty<ScriptTypeEnum> typeProperty()
	{
		return this.typeProperty;
	}
	
	public SimpleStringProperty lastUpdatedProperty()
	{
		return this.lastUpdatedProperty;
	}
	
	public SimpleLongProperty countProperty()
	{
		return this.countProperty;
	}
	
	/**
	 * Gets the script name.
	 * 
	 * @return Name of the script
	 */
	public String getName()
	{
		return script.getName();
	}
	
	public TestCase getScript()
	{
		return script;
	}

	@Override
	public void onChange()
	{
		update();		
	}
}
