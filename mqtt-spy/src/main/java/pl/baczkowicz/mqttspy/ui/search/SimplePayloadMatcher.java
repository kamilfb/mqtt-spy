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
package pl.baczkowicz.mqttspy.ui.search;

import pl.baczkowicz.mqttspy.connectivity.MqttContent;

public class SimplePayloadMatcher implements SearchMatcher
{	
	private final boolean caseSensitive;
	
	private String valueToFind;

	public SimplePayloadMatcher(final String valueToFind, final boolean caseSensitive)
	{
		this.valueToFind = valueToFind;
		this.caseSensitive = caseSensitive;
	}
	
	@Override
	public boolean matches(final MqttContent message)
	{
		return matches(message.getFormattedPayload());
	}

	private boolean matches(final String value)
	{
		if (caseSensitive)
		{
			return value.contains(valueToFind);
		}
		
		return value.toLowerCase().contains(valueToFind.toLowerCase());
	}
}
