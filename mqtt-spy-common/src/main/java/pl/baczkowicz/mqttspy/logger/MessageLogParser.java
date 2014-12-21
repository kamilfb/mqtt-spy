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
package pl.baczkowicz.mqttspy.logger;

import pl.baczkowicz.mqttspy.common.generated.LoggedMqttMessage;
import pl.baczkowicz.mqttspy.exceptions.XMLException;
import pl.baczkowicz.mqttspy.xml.XMLParser;

/**
 * Parser for the mqtt-spy message log.
 */
public class MessageLogParser extends XMLParser
{
	/**
	 * Instantiates the MessageLogParser.
	 * 
	 * @throws XMLException Thrown when cannot instantiate the marshaller/unmarshaller.
	 */
	public MessageLogParser() throws XMLException
	{
		super(LoggedMqttMessage.class);
	}
	
	/**
	 * Turns the given XML message represented as a string into a LoggedMqttMessage object.
	 *  
	 * @param xmlMessage The XML message to turn into an object
	 * 
	 * @return LoggedMqttMessage object representing the given XML
	 * 
	 * @throws XMLException Thrown if cannot do the conversion
	 */
	public LoggedMqttMessage parse(final String xmlMessage) throws XMLException
	{
		return (LoggedMqttMessage) super.unmarshal(xmlMessage, LoggedMqttMessage.class);
	}
}
