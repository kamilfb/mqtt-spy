/***********************************************************************************
 * 
 * Copyright (c) 2014 Kamil Baczkowicz
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
package pl.baczkowicz.mqttspy.scripts.io;

import java.io.IOException;

/**
 * Interface between a script and the mqttspy object, which can be used primarily for publishing messages.
 */
public interface IScriptIO
{
	/**
	 * Publishes a message with the given payload to the given topic (qos = 0; retained = false).
	 * 
	 * @param publicationTopic The publication topic
	 * @param payload The payload of the message
	 */
	void publish(final String publicationTopic, final String payload);

	/**
	 * Publishes a message with the given payload, qos and retained flag to the given topic.
	 * 
	 * @param publicationTopic The publication topic
	 * @param payload The payload of the message
	 * @param qos The quality of service to be used
	 * @param retained The retained flag
	 */
	void publish(final String publicationTopic, final String payload, final int qos, final boolean retained);
	
	/**
	 * Informs the java side the script is still alive.
	 */
	void touch();

	/**
	 * Sets a custom thread timeout for the script.
	 *  
	 * @param customTimeout Custom timeout in milliseconds (normally expected to be higher than the default)
	 */
	void setScriptTimeout(long customTimeout);

	/**
	 * Instantiates a class with the given package name and class name, e.g. by
	 * passing `com.test.MyClass`, the following object `com_test_MyClass`
	 * becomes available.
	 * 
	 * @param className The package name and class name (e.g. com.test.MyClass)
	 * 
	 * @return True if successfully initialised
	 */
	boolean instantiate(String className);

	/**
	 * Executes a system command.
	 * 
	 * @param command The command to execute
	 * 
	 * @return Result of the command
	 * 
	 * @throws IOException Thrown when a problem is encountered
	 * @throws InterruptedException Thrown when a the thread is interrupted
	 */
	String execute(String command) throws IOException, InterruptedException;	
}