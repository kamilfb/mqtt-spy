var TestCaseStepResult = Java.type("pl.baczkowicz.mqttspy.testcases.TestCaseStepResult");
var TestCaseStatus = Java.type("pl.baczkowicz.mqttspy.testcases.TestCaseStatus");

var getInfo = function () 
{
    var TestCaseInfo = Java.type("pl.baczkowicz.mqttspy.testcases.TestCaseInfo");
	var info = new TestCaseInfo();
	info.setName("Sample test case 1");
	info.getSteps().add("Step 1");
	info.getSteps().add("Step 2");

	return info;
};

var step1 = function ()
{
	return new TestCaseStepResult(TestCaseStatus.PASSED, "All fine in step 1");
};

var step2 = function ()
{
	return new TestCaseStepResult(TestCaseStatus.PASSED, "All fine in step 2");
};

