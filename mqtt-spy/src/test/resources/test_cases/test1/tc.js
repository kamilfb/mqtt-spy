var TestCaseStepResult = Java.type("pl.baczkowicz.mqttspy.testcases.TestCaseStepResult");
var TestCaseStatus = Java.type("pl.baczkowicz.mqttspy.testcases.TestCaseStatus");

// This is to demonstrate updating a global variable
var count = 0;

var getInfo = function () 
{
    var TestCaseInfo = Java.type("pl.baczkowicz.mqttspy.testcases.TestCaseInfo");
	var info = new TestCaseInfo();
	info.setName("Sample test case 1");
	info.getSteps().add("Step 1");
	info.getSteps().add("Step 2");
	info.getSteps().add("Step 3");
	info.getSteps().add("Step 4");

	return info;
};

var step1 = function ()
{
	// Make sure any variables are reset if the test case is re-run
	count = 1;
	return new TestCaseStepResult(TestCaseStatus.PASSED, "All fine in step 1 " + "[" + count + "]");
};

var step2 = function ()
{
	count = count + 1;	
	if (count < 5)
	{
		return new TestCaseStepResult(TestCaseStatus.IN_PROGRESS, "Still waiting... " + "[" + count + "]");
	}
	return new TestCaseStepResult(TestCaseStatus.ACTIONED, "All fine in step 2 " + "[" + count + "]");
};

var step3 = function ()
{
	count = count + 1;
	return new TestCaseStepResult(TestCaseStatus.SKIPPED, "Step 3 skipped " + "[" + count + "]");
};

var step4 = function ()
{
	count = count + 1;
	return new TestCaseStepResult(TestCaseStatus.FAILED, "Step 4 failed " + "[" + count + "]");
};

