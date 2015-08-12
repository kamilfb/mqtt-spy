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
package pl.baczkowicz.mqttspy.ui.testcases;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import pl.baczkowicz.mqttspy.scripts.Script;
import pl.baczkowicz.mqttspy.testcases.TestCaseInfo;
import pl.baczkowicz.mqttspy.testcases.TestCaseStatus;
import pl.baczkowicz.mqttspy.ui.properties.TestCaseStepProperties;

public class InteractiveTestCase extends Script
{
	private TestCaseInfo info;
	
	private int currentStep;
	
	private String lastUpdated = "";
	
	private ObservableList<TestCaseStepProperties> steps = FXCollections.observableArrayList();
	
	private TestCaseStatus testCaseStatus;

	/**
	 * @return the info
	 */
	public TestCaseInfo getInfo()
	{
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(TestCaseInfo info)
	{
		this.info = info;
	}
	
	public ObservableList<TestCaseStepProperties> getSteps()
	{
		return this.steps;
	}

	/**
	 * @return the status
	 */
	public TestCaseStatus getTestCaseStatus()
	{
		return testCaseStatus;
	}

	/**
	 * @param status the status to set
	 */
	public void setTestCaseStatus(TestCaseStatus status)
	{
		this.testCaseStatus = status;
	}

	/**
	 * @return the currentStep
	 */
	public int getCurrentStep()
	{
		return currentStep;
	}

	/**
	 * @param currentStep the currentStep to set
	 */
	public void setCurrentStep(int currentStep)
	{
		this.currentStep = currentStep;
	}

	public String getLastUpdated()
	{
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated)
	{
		this.lastUpdated = lastUpdated;
	}
}
