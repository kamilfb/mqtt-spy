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
package pl.baczkowicz.mqttspy.xml;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;

import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.exceptions.XMLException;
import pl.baczkowicz.mqttspy.xml.XMLParser;

public class XMLParserTest
{
	private XMLParser parser;
	
	@Before
	public void setUp() throws Exception
	{
		parser = new XMLParser(
				ConfigurationManager.PACKAGE, 
				new String[] {ConfigurationManager.COMMON_SCHEMA, ConfigurationManager.SCHEMA});
	}

	@Test
	(expected = FileNotFoundException.class)
	public final void testLoadFromFile() throws XMLException, FileNotFoundException
	{
		parser.loadFromFile(new File("/invalidpath/test.txt"));
	}
}
