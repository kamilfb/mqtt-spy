package pl.baczkowicz.mqttspy.daemon;

import static org.junit.Assert.*;

import org.junit.Test;

import pl.baczkowicz.spy.testcases.TestCaseResult;
import pl.baczkowicz.spy.testcases.TestCaseStatus;

public class MqttSpyDaemonTest
{
	@Test
	public void testBasicConfiguration()
	{
		final MqttSpyDaemon daemon = new MqttSpyDaemon();
		
		assertTrue(daemon.start("src/test/resources/test_configurations/basic-configuration.xml"));
	}
	
	@Test
	public void testSslWithTestCasesConfiguration()
	{
		final MqttSpyDaemon daemon = new MqttSpyDaemon();
		
		assertTrue(daemon.start("src/test/resources/test_configurations/test-cases-iot-eclipse.xml"));
	}
	
	@Test
	public void testSslWithMosquittoConfiguration()
	{
		final MqttSpyDaemon daemon = new MqttSpyDaemon();
		
		assertTrue(daemon.start("src/test/resources/test_configurations/mosquitto-org.xml"));
		
		final TestCaseResult result = daemon.runTestCase("src/test/resources/test_cases/test3/tc3.js");
		assertTrue(result.getResult().equals(TestCaseStatus.PASSED));
		
		daemon.stop();
	}
}
