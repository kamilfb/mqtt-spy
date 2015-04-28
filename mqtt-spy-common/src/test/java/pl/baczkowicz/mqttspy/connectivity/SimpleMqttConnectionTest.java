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

package pl.baczkowicz.mqttspy.connectivity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Test;

import pl.baczkowicz.mqttspy.common.generated.MqttConnectionDetails;
import pl.baczkowicz.mqttspy.common.generated.ProtocolEnum;
import pl.baczkowicz.mqttspy.common.generated.SslModeEnum;
import pl.baczkowicz.mqttspy.common.generated.SslSettings;
import pl.baczkowicz.mqttspy.common.generated.UserCredentials;
import pl.baczkowicz.mqttspy.connectivity.reconnection.ReconnectionManager;
import pl.baczkowicz.mqttspy.exceptions.MqttSpyException;
import pl.baczkowicz.mqttspy.utils.ConversionUtils;

/**
 * Unit and integration tests for connections. Some of the tests assume
 * 'mosquitto' is available on the path, and is a version 1.4 or later.
 * 
 * Also, some tests assume availability of the network and 'test.mosquitto.org'.
 * 
 */
public class SimpleMqttConnectionTest
{
	private final ReconnectionManager reconnectionManager = new ReconnectionManager();
	
	private Process startMosquitto(final String configurationFile) throws IOException
	{
		String execStr = "mosquitto -c " + configurationFile;
        Process proc = Runtime.getRuntime().exec(execStr);
        System.out.println("Proc: " + proc);
        
        return proc;
	}
	
	private void stopProcess(final Process mosquitto)
	{
		System.out.println("Destroying");
        mosquitto.destroy();
        System.out.println("Destroyed");
	}
	
	private MqttCallback createTestCallback(final String connection)
	{
		return new MqttCallback()
		{			
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception
			{
				System.out.println("Got a message for " 
						+ connection 
						+ " on " + topic 
						+ " with content " 
						+ new String(message.getPayload()));			
			}
			
			@Override
			public void deliveryComplete(IMqttDeliveryToken arg0)
			{
				// Not used	
			}
			
			@Override
			public void connectionLost(Throwable arg0)
			{
				// Not used				
			}
		};
	}
	
	private MqttConnectionDetails createMqttConnectionDetails(final String brokerAddress, final UserCredentials uc, final SslSettings ssl)
	{
		return new MqttConnectionDetails(
				"test", 
				ProtocolEnum.MQTT_DEFAULT, 
				Arrays.asList(brokerAddress), 
				"mqtt-spy-test", 
				uc, 
				null, 
				true, 
				10, 
				10, 
				ssl, 
				null);
	}
	
	@Test
	public void testAnonConnection() throws IOException, MqttSpyException, InterruptedException
	{
		final Process mosquitto = startMosquitto("/home/kamil/Programming/Git/mqtt-spy-common/src/test/resources/mosquitto/mosquitto_allow_anon.conf");
		
		final MqttConnectionDetails connectionDetails = createMqttConnectionDetails("tcp://localhost:10001", null, null);
		
		final SimpleMqttConnection connection = new SimpleMqttConnection(reconnectionManager, 0, connectionDetails);
		connection.createClient(createTestCallback("tcp://localhost:10001"));
		assertTrue(connection.connect());
		System.out.println("Connected...");
		
		assertTrue(connection.subscribe("/mqtt-spy/test/", 0));
		System.out.println("Subscribed...");
		
		connection.publish("/mqtt-spy/test/", "message over TCP", 0, false);
		System.out.println("Published...");
		
		// Waiting for message to be received now...
		Thread.sleep(1000);
		
		connection.disconnect();
		System.out.println("Disconnected");
				
		stopProcess(mosquitto);
	}
	
	@Test
	public void testRejectingAnonConnection() throws IOException, MqttSpyException, InterruptedException
	{
		final Process mosquitto = startMosquitto("/home/kamil/Programming/Git/mqtt-spy-common/src/test/resources/mosquitto/mosquitto_specified_users.conf");
		
		final MqttConnectionDetails connectionDetails = createMqttConnectionDetails("tcp://localhost:10002", null, null);
		
		final SimpleMqttConnection connection = new SimpleMqttConnection(reconnectionManager, 0, connectionDetails);
		connection.createClient(createTestCallback("tcp://localhost:10002"));
		assertFalse(connection.connect());	
				
		stopProcess(mosquitto);
	}
	
	@Test
	public void testUserConnection() throws IOException, MqttSpyException, InterruptedException
	{
		final Process mosquitto = startMosquitto("/home/kamil/Programming/Git/mqtt-spy-common/src/test/resources/mosquitto/mosquitto_specified_users.conf");
		
		final MqttConnectionDetails connectionDetails = createMqttConnectionDetails(
				"tcp://localhost:10002", 
				new UserCredentials("nopassword", ""), 
				null);
		
		final SimpleMqttConnection connection = new SimpleMqttConnection(reconnectionManager, 0, connectionDetails);
		connection.createClient(createTestCallback("tcp://localhost:10002"));
		assertTrue(connection.connect());
		System.out.println("Connected...");
		
		assertTrue(connection.subscribe("/mqtt-spy/test/", 0));
		System.out.println("Subscribed...");
		
		connection.publish("/mqtt-spy/test/", "message over TCP", 0, false);
		System.out.println("Published...");
		
		// Waiting for message to be received now...
		Thread.sleep(1000);
		
		connection.disconnect();
		System.out.println("Disconnected");
				
		stopProcess(mosquitto);
	}
	
	@Test
	public void testUserConnectionWithPassword() throws IOException, MqttSpyException, InterruptedException
	{
		final Process mosquitto = startMosquitto("/home/kamil/Programming/Git/mqtt-spy-common/src/test/resources/mosquitto/mosquitto_specified_users.conf");
		
		final MqttConnectionDetails connectionDetails = createMqttConnectionDetails(
				"tcp://localhost:10002", 
				new UserCredentials("test1", ConversionUtils.stringToBase64("t1")), 
				null);
		
		final SimpleMqttConnection connection = new SimpleMqttConnection(reconnectionManager, 0, connectionDetails);
		connection.createClient(createTestCallback("tcp://localhost:10002"));
		assertTrue(connection.connect());
		System.out.println("Connected...");
		
		assertTrue(connection.subscribe("/mqtt-spy/test/", 0));
		System.out.println("Subscribed...");
		
		connection.publish("/mqtt-spy/test/", "message over TCP", 0, false);
		System.out.println("Published...");
		
		// Waiting for message to be received now...
		Thread.sleep(1000);
		
		connection.disconnect();
		System.out.println("Disconnected");
				
		stopProcess(mosquitto);
	}
	
	@Test
	public void testUserConnectionWithInvalidPassword() throws IOException, MqttSpyException, InterruptedException
	{
		final Process mosquitto = startMosquitto("/home/kamil/Programming/Git/mqtt-spy-common/src/test/resources/mosquitto/mosquitto_specified_users.conf");
		
		final MqttConnectionDetails connectionDetails = createMqttConnectionDetails("tcp://localhost:10002", new UserCredentials("test1", "blabla"), null);
		
		final SimpleMqttConnection connection = new SimpleMqttConnection(reconnectionManager, 0, connectionDetails);
		connection.createClient(createTestCallback("tcp://localhost:10002"));
		assertFalse(connection.connect());		
				
		stopProcess(mosquitto);
	}
	
	@Test
	public void testServerOnlyAuthenticationWithLocalMosquitto() throws MqttSpyException, InterruptedException, IOException
	{			
		final Process mosquitto = startMosquitto("/home/kamil/Programming/Git/mqtt-spy-common/src/test/resources/mosquitto/mosquitto_ssl_server_only.conf");
				
		final MqttConnectionDetails connectionDetails = createMqttConnectionDetails(
				"ssl://localhost:10010", 
				new UserCredentials("nopassword", ""),
				new SslSettings(SslModeEnum.SERVER_ONLY, "TLSv1", 
						"/home/kamil/Programming/Git/mqtt-spy-common/src/test/resources/mosquitto/ssl/ca.crt", 
						null, null, null, null));
		
		final SimpleMqttConnection connection = new SimpleMqttConnection(reconnectionManager, 0, connectionDetails);
		connection.createClient(createTestCallback("ssl://localhost:10010"));
		assertTrue(connection.connect());
		System.out.println("Connected...");
		
		assertTrue(connection.subscribe("/mqtt-spy/test/", 0));
		System.out.println("Subscribed...");
		
		connection.publish("/mqtt-spy/test/", "message over SSL", 0, false);
		System.out.println("Published...");
		
		// Waiting for message to be received now...
		Thread.sleep(1000);
		
		connection.disconnect();
		System.out.println("Disconnected");
		
		stopProcess(mosquitto);
	}
	
	@Test
	public void testServerAndClientAuthenticationWithLocalMosquitto() throws MqttSpyException, InterruptedException, IOException
	{			
		//final Process mosquitto = startMosquitto("/home/kamil/Programming/Git/mqtt-spy-common/src/test/resources/mosquitto/mosquitto_ssl_server_and_client.conf");
				
		final MqttConnectionDetails connectionDetails = createMqttConnectionDetails(
				"ssl://localhost:10011", 
				new UserCredentials("nopassword", ""),
				new SslSettings(SslModeEnum.SERVER_AND_CLIENT, "TLSv1.1", 
						"/home/kamil/Programming/Git/mqtt-spy-common/src/test/resources/mosquitto/ssl/ca.crt", 
						"/home/kamil/Programming/Git/mqtt-spy-common/src/test/resources/mosquitto/ssl/client.crt", 
						"/home/kamil/Programming/Git/mqtt-spy-common/src/test/resources/mosquitto/ssl/client.key", 
						"", null));
		
		final SimpleMqttConnection connection = new SimpleMqttConnection(reconnectionManager, 0, connectionDetails);
		connection.createClient(createTestCallback("ssl://localhost:10011"));
		assertTrue(connection.connect());
		System.out.println("Connected...");
		
		assertTrue(connection.subscribe("/mqtt-spy/test/", 0));
		System.out.println("Subscribed...");
		
		connection.publish("/mqtt-spy/test/", "message over SSL", 0, false);
		System.out.println("Published...");
		
		// Waiting for message to be received now...
		Thread.sleep(1000);
		
		connection.disconnect();
		System.out.println("Disconnected");
		
		//stopProcess(mosquitto);
	}
	
	@Test
	public void testServerOnlyAuthenticationWithLiveMosquitto() throws MqttSpyException, InterruptedException
	{				
		final MqttConnectionDetails connectionDetails = createMqttConnectionDetails("ssl://test.mosquitto.org", null, 
				new SslSettings(SslModeEnum.SERVER_ONLY, "TLSv1.2", 
						"/home/kamil/Programming/Git/mqtt-spy-common/src/test/resources/test_mosquitto_org/mosquitto.org.crt", 
						null, null, null, null));
		
		final SimpleMqttConnection connection = new SimpleMqttConnection(reconnectionManager, 0, connectionDetails);
		connection.createClient(createTestCallback("ssl://test.mosquitto.org"));
		assertTrue(connection.connect());
		System.out.println("Connected...");
		
		assertTrue(connection.subscribe("/mqtt-spy/test/", 0));
		System.out.println("Subscribed...");
		
		connection.publish("/mqtt-spy/test/", "message over SSL", 0, false);
		System.out.println("Published...");
		
		// Waiting for message to be received now...
		Thread.sleep(1000);
		
		connection.disconnect();
		System.out.println("Disconnected");
	}

}
