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
package pl.baczkowicz.mqttspy.testcases;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.ScriptDetails;
import pl.baczkowicz.mqttspy.scripts.ScriptManager;
import pl.baczkowicz.mqttspy.scripts.ScriptRunningState;
import pl.baczkowicz.mqttspy.utils.FileUtils;
import pl.baczkowicz.mqttspy.utils.TimeUtils;

public class TestCaseManager
{	
	public static String GET_INFO_METHOD = "getInfo";
	
	public static SimpleDateFormat testCaseFileSdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
	
	public static SimpleDateFormat testCasesFileSdf = new SimpleDateFormat("yyyyMMdd");

	private final static Logger logger = LoggerFactory.getLogger(TestCaseManager.class);
	
	private final ScriptManager scriptManager;
	
	private List<TestCase> testCases = new ArrayList<>();
			
	private int running = 0;

	public TestCaseManager(final ScriptManager scriptManager)	
	{
		this.scriptManager = scriptManager;
	}
	
	public TestCase addTestCase(final File scriptFile)
	{
		logger.info("Adding " + scriptFile.getName() + " with parent " + scriptFile.getParent());
		
		final ScriptDetails scriptDetails = new ScriptDetails();					
		scriptDetails.setFile(scriptFile.getAbsolutePath());
		scriptDetails.setRepeat(false);
							
		final String scriptName = ScriptManager.getScriptName(scriptFile);
		
		final TestCase testCase = new TestCase();
				
		scriptManager.createFileBasedScript(testCase, scriptName, scriptFile, scriptManager.getConnection(), scriptDetails);
		
		try
		{	
			scriptManager.runScript(testCase, false);
			testCase.setInfo((TestCaseInfo) scriptManager.invokeFunction(testCase, GET_INFO_METHOD));
			
			int stepNumber = 1;
			for (final String step : testCase.getInfo().getSteps())
			{
				testCase.getSteps().add(new TestCaseStep(String.valueOf(stepNumber), step, TestCaseStatus.NOT_RUN, ""));
				stepNumber++;
			}
			
			logger.info(testCase.getInfo().getName() + " " + Arrays.asList(testCase.getInfo().getSteps()));
		}
		catch (ScriptException | NoSuchMethodException e)
		{
			logger.error("Cannot read test case", e);
		}
		
		// Override name
		if (testCase.getInfo() != null && testCase.getInfo().getName() != null)
		{
			testCase.setName(testCase.getInfo().getName());
		}
		else
		{
			testCase.setName(scriptFile.getParentFile().getName());
		}
		
		testCases.add(testCase);
		return testCase;
	}
	
	public void loadTestCases(final String testCaseLocation)
	{
		final List<File> scripts = FileUtils.getDirectoriesWithFile(testCaseLocation, "tc.*js");

		for (final File scriptFile : scripts)
		{
			addTestCase(scriptFile);
		}
	}
	
	private TestCaseStepResult runTestCaseSteps(final TestCase testCase)
	{
		TestCaseStepResult lastResult = null;
		
		while (testCase.getCurrentStep() < testCase.getSteps().size() && testCase.getStatus().equals(ScriptRunningState.RUNNING))
		{
			final TestCaseStep step = testCase.getSteps().get(testCase.getCurrentStep());
			
			testCase.setLastUpdated(TimeUtils.DATE_WITH_SECONDS_SDF.format(new Date()));
			step.setStatus(TestCaseStatus.IN_PROGRESS);										
			
			try
			{
				final TestCaseStepResult result = (TestCaseStepResult) scriptManager.invokeFunction(testCase, "step" + step.getStepNumber());
				lastResult = result;
				
				if (result == null)
				{
					continue;
				}
				
				step.setStatus(result.getStatus());
				step.setExecutionInfo(result.getInfo());		
				
				// If not in progress any more, move to next
				if (!TestCaseStatus.IN_PROGRESS.equals(result.getStatus()))
				{
					testCase.setCurrentStep(testCase.getCurrentStep() + 1);
				}														
			}
			catch (NoSuchMethodException e)
			{
				step.setStatus(TestCaseStatus.ERROR);
				logger.error("Step execution error for step " + step.getStepNumber(), e);
			}
			catch (ScriptException e)
			{
				step.setStatus(TestCaseStatus.FAILED);
				logger.error("Step execution failure for step " + step.getStepNumber(), e);
			}
			
			testCase.getTestCaseResult().getStepResults().add(step);
			
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				break;
			}		
		}				
		
		return lastResult;
	}
	
	private void runAllTestCaseMethods(final TestCase testCase)
	{
		running++;				
		testCase.setCurrentStep(0);		
		
		// Before
		if (!scriptManager.invokeBefore(testCase))
		{
			testCase.setStatus(ScriptRunningState.FAILED);					
		}
		
		// Test steps
		TestCaseStepResult lastResult = runTestCaseSteps(testCase);

		// After
		if (!scriptManager.invokeAfter(testCase))
		{
			testCase.setStatus(ScriptRunningState.FAILED);					
		}
		
		final TestCaseStepResult testCaseStatus = lastResult;
		
		if (testCase.getStatus().equals(ScriptRunningState.STOPPED))
		{
			testCase.setTestCaseStatus(TestCaseStatus.SKIPPED);
		}
		else
		{
			testCase.setTestCaseStatus(testCaseStatus.getStatus());
			testCase.setStatus(ScriptRunningState.FINISHED);
		}
		
		testCase.getTestCaseResult().setInfo(testCase.getInfo());
		testCase.getTestCaseResult().setResult(testCase.getTestCaseStatus());
		testCase.setLastUpdated(TimeUtils.DATE_WITH_SECONDS_SDF.format(new Date()));
		
		running--;
		
//		if (testCaseExecutionController.isAutoExportEnabled())
//		{
//			final String parentDir = selectedTestCase.getScript().getScriptFile().getParent() + System.getProperty("file.separator");
//			exportTestCaseResult(selectedTestCase, new File(parentDir + "result_" + testCaseFileSdf.format(new Date()) + "_" + testCaseStatus.getStatus() + ".csv"));
//		}
	}
	
	public void runTestCase(final TestCase testCase)
	{				
		testCase.setStatus(ScriptRunningState.RUNNING);
		testCase.setTestCaseStatus(TestCaseStatus.IN_PROGRESS);
		
		// Clear last run for this test case
		for (final TestCaseStep step : testCase.getSteps())
		{
			step.setStatus(TestCaseStatus.NOT_RUN);
			step.setExecutionInfo("");
		}
		
		runAllTestCaseMethods(testCase);	
	}

	public void stopTestCase(final TestCase testCase)
	{
		testCase.setStatus(ScriptRunningState.STOPPED);		
		
		final TestCaseStep step = testCase.getSteps().get(testCase.getCurrentStep());
		
		step.setStatus(TestCaseStatus.SKIPPED);
	}

	public int getTotalCount()
	{
		return testCases.size();
	}

	public List<TestCase> getTestCases()
	{
		return testCases;
	}

	// *** Export methods ***
	
	public void exportTestCaseResultAsCSV(final TestCase testCase, final File selectedFile)
	{
		logger.info("Saving test case results to " + selectedFile.getAbsolutePath());
		
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(selectedFile));
			
			out.write(
					//"Time, " + 
					"Step" + ", " + "\"" + 
					"Description" + "\"" + ", " + 
					"Status" + ", " + "\"" + 
					"Info" + "\"");
			out.newLine();
			
			for (TestCaseStep step : testCase.getSteps())
			{
				out.write(
						//step.
						step.getStepNumber() + ", " + "\"" + 
						step.getDescription() + "\"" + ", " + 
						step.getStatus() + ", " + "\"" + 
						step.getExecutionInfo() + "\"");
				out.newLine();
			}
						
			out.close();
		}
		catch (IOException e)
		{
			logger.error("Cannot write to file", e);
		}
	}
	
	public void exportTestCasesResultsAsCSV(final File selectedFile)
	{
		logger.info("Saving test cases results to " + selectedFile.getAbsolutePath());
		
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(selectedFile));
			
			out.write(
					"\"" + "Test case" + "\"" + ", " +
					"\"" + "Last updated" + "\"" + ", " + "\"" +
					"Status");
			out.newLine();
			
			for (TestCase testCase : getTestCases())
			{
				out.write(
						"\"" + testCase.getName() + "\"" + ", " + 
						"\"" + testCase.getLastUpdated() + "\"" + ", " +
						"\"" + testCase.getTestCaseStatus() + "\"" );
				out.newLine();
			}
						
			out.close();
		}
		catch (IOException e)
		{
			logger.error("Cannot write to file", e);
		}
	}
}
