package pl.baczkowicz.mqttspy.testcases;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.application.Platform;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.ScriptDetails;
import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.scripts.ScriptManager;
import pl.baczkowicz.mqttspy.scripts.ScriptRunningState;
import pl.baczkowicz.mqttspy.ui.properties.TestCaseProperties;
import pl.baczkowicz.mqttspy.ui.properties.TestCaseStepProperties;
import pl.baczkowicz.mqttspy.utils.FileUtils;

public class TestCaseManager
{	
	final static Logger logger = LoggerFactory.getLogger(TestCaseManager.class);
	
	private final String defaultTestCaseLocation = ConfigurationManager.getDefaultHomeDirectory() + "test_cases";

	private final ScriptManager scriptManager;

	public TestCaseManager(final ScriptManager scriptManager)	
	{
		this.scriptManager = scriptManager;
	}
	
	public List<TestCaseProperties> loadTestCases(final String testCaseLocation)
	{
		final List<TestCaseProperties> properties = new ArrayList<>();
		
		final List<File> scripts = FileUtils.getDirectoriesWithFile(testCaseLocation, "tc.*js");

		for (final File scriptFile : scripts)
		{
			logger.info("Adding " + scriptFile.getName() + " with parent " + scriptFile.getParent());
			
			final ScriptDetails scriptDetails = new ScriptDetails();					
			scriptDetails.setFile(scriptFile.getAbsolutePath());
			scriptDetails.setRepeat(false);
								
			final String scriptName = ScriptManager.getScriptName(scriptFile);
			
			final TestCase testCase = new TestCase();
					
			scriptManager.createFileBasedScript(testCase, scriptName, scriptFile, null, scriptDetails);
			
			try
			{	
				scriptManager.runScript(testCase, false);
				testCase.setInfo((TestCaseInfo) scriptManager.invokeFunction(testCase, "getInfo"));
//				testCase.getScriptEngine().eval(new FileReader(testCase.getScriptFile()));
//				final Invocable invocable = (Invocable) testCase.getScriptEngine();
//				
//				testCase.setInfo((TestCaseInfo) invocable.invokeFunction("getInfo"));
				
				int stepNumber = 1;
				for (final String step : testCase.getInfo().getSteps())
				{
					testCase.getSteps().add(new TestCaseStepProperties(
							String.valueOf(stepNumber), step, TestCaseStatus.NOT_RUN, ""));
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
			
			properties.add(new TestCaseProperties(testCase));
		}
		
		return properties;
	}
	
	public void runTestCase(final TestCaseProperties selected)
	{			
		final TestCase testCase = selected.getScript();
		
		testCase.setStatus(ScriptRunningState.RUNNING);
		selected.statusProperty().setValue(TestCaseStatus.IN_PROGRESS);
		
		// Clear last run for this test case
		for (final TestCaseStepProperties properties : testCase.getSteps())
		{
			properties.statusProperty().setValue(TestCaseStatus.NOT_RUN);
			properties.executionInfoProperty().setValue("");
		}
		
		new Thread(new Runnable()
		{			
			@Override
			public void run()
			{
				TestCaseStepResult lastResult = null;
				int step = 0;				
				
				while (step < testCase.getSteps().size() && testCase.getStatus().equals(ScriptRunningState.RUNNING))
				{
					final TestCaseStepProperties properties = testCase.getSteps().get(step);
					
					Platform.runLater(new Runnable()
					{							
						@Override
						public void run()
						{
							properties.statusProperty().setValue(TestCaseStatus.IN_PROGRESS);
						}
					});
					
					try
					{
						Thread.sleep(500);
					}
					catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
					
					try
					{
						final TestCaseStepResult result = (TestCaseStepResult) scriptManager.invokeFunction(
								testCase, "step" + properties.stepNumberProperty().getValue());
						lastResult = result;
						
						if (result == null)
						{
							// TODO
							continue;
						}
						
						Platform.runLater(new Runnable()
						{							
							@Override
							public void run()
							{
								properties.statusProperty().setValue(result.getStatus());
								properties.executionInfoProperty().setValue(result.getInfo());
							}
						});
						
						// If not in progress any more, move to next
						if (!TestCaseStatus.IN_PROGRESS.equals(result.getStatus()))
						{
							step++;
						}														
					}
					catch (NoSuchMethodException | ScriptException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					try
					{
						Thread.sleep(500);
					}
					catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		
				}		
				
				final TestCaseStepResult testCaseStatus = lastResult;
				Platform.runLater(new Runnable()
				{							
					@Override
					public void run()
					{
						if (testCase.getStatus().equals(ScriptRunningState.STOPPED))
						{
							selected.statusProperty().setValue(TestCaseStatus.SKIPPED);
						}
						else
						{
							selected.statusProperty().setValue(testCaseStatus.getStatus());
							testCase.setStatus(ScriptRunningState.FINISHED);
						}
					}
				});
				
			}
		}).start();		
	}

	public void stopTestCase(TestCaseProperties testCaseProperties)
	{
		final TestCase testCase = testCaseProperties.getScript();
		testCase.setStatus(ScriptRunningState.STOPPED);		
	}
}
