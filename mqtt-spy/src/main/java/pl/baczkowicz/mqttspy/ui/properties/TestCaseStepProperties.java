/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
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
package pl.baczkowicz.mqttspy.ui.properties;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import pl.baczkowicz.mqttspy.testcases.TestCaseStatus;
import pl.baczkowicz.mqttspy.testcases.TestCaseStep;

/**
 * This represents a single row displayed in the test case table.
 */
public class TestCaseStepProperties extends TestCaseStep
{
	/** The step number. */
	private SimpleStringProperty stepNumberProperty;
	
	/** Description of the step. */	
	private SimpleStringProperty descriptionProperty;
	
	/** Step status. */	
	private SimpleObjectProperty<TestCaseStatus> statusProperty;
	
	/** Information about the execution. */	
	private SimpleStringProperty executionInfoProperty;
		
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
		super(stepNumber, description, status, info);
		this.stepNumberProperty = new SimpleStringProperty(stepNumber);
		this.descriptionProperty = new SimpleStringProperty(description);
		this.statusProperty = new SimpleObjectProperty<>(status);
		this.executionInfoProperty = new SimpleStringProperty(info);
	}	

	/**
	 * The description property.
	 * 
	 * @return The description as SimpleStringProperty
	 */
	public SimpleStringProperty descriptionProperty()
	{
		return this.descriptionProperty;
	}
	
	/**
	 * The stepNumber property.
	 * 
	 * @return The stepNumber as SimpleStringProperty
	 */
	public SimpleStringProperty stepNumberProperty()
	{
		return this.stepNumberProperty;
	}
	
	/**
	 * The executionInfo property.
	 * 
	 * @return The executionInfo as SimpleStringProperty
	 */
	public SimpleStringProperty executionInfoProperty()
	{
		return this.executionInfoProperty;
	}
	
	/**
	 * The statusProperty property.
	 * 
	 * @return The statusProperty as SimpleStringProperty
	 */
	public SimpleObjectProperty<TestCaseStatus> statusProperty()
	{
		return this.statusProperty;
	}
	
	// *** Overrides ***
	
	@Override
	public void setExecutionInfo(final String info)
	{
		super.setExecutionInfo(info);		
		executionInfoProperty().setValue(info);
	}
	
	@Override
	public void setStatus(final TestCaseStatus status)
	{
		super.setStatus(status);
		statusProperty().setValue(status);	
	}
}
