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

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import pl.baczkowicz.mqttspy.testcases.TestCaseStatus;

/**
 * This represents a single row displayed in the test case table.
 */
public class TestCaseStepProperties
{
	/** The step number. */
	private SimpleStringProperty stepNumber;
	
	/** Description of the step. */	
	private SimpleStringProperty description;
	
	/** Step status. */	
	private SimpleObjectProperty<TestCaseStatus> status;
	
	/** Information about the execution. */	
	private SimpleStringProperty executionInfo;
		
	/**
	 * Creates a TestCaseStepProperties with the given parameters.
	 * 
	 * @param stepNumber
	 * @param description
	 * @param status
	 * @param info
	 */
	public TestCaseStepProperties(final String stepNumber, final String description, final TestCaseStatus status, final String info)
	{
		this.stepNumber = new SimpleStringProperty(stepNumber);
		this.description = new SimpleStringProperty(description);
		this.status = new SimpleObjectProperty<>(status);
		this.executionInfo = new SimpleStringProperty(info);
	}	

	/**
	 * The description property.
	 * 
	 * @return The description as SimpleStringProperty
	 */
	public SimpleStringProperty descriptionProperty()
	{
		return this.description;
	}
	
	/**
	 * The stepNumber property.
	 * 
	 * @return The stepNumber as SimpleStringProperty
	 */
	public SimpleStringProperty stepNumberProperty()
	{
		return this.stepNumber;
	}
	
	/**
	 * The executionInfo property.
	 * 
	 * @return The executionInfo as SimpleStringProperty
	 */
	public SimpleStringProperty executionInfoProperty()
	{
		return this.executionInfo;
	}
	
	/**
	 * The statusProperty property.
	 * 
	 * @return The statusProperty as SimpleStringProperty
	 */
	public SimpleObjectProperty<TestCaseStatus> statusProperty()
	{
		return this.status;
	}
}
