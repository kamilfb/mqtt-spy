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
import pl.baczkowicz.mqttspy.storage.MessageList;

public class UniqueContentOnlyFilter implements MessageFilter
{
	private boolean uniqueContentOnly;
	
	private int deleted = 0;
	
	@Override
	public boolean filter(final MqttContent message, final MessageList messageList)
	{
		if (!uniqueContentOnly || messageList.getMessages().size() == 0)
		{
			return false;			
		}
		
		final MqttContent lastMessage = messageList.getMessages().get(0);
		
		if (message.getFormattedPayload().equals(lastMessage.getFormattedPayload()) && message.getTopic().equals(lastMessage.getTopic()))
		{
			messageList.getMessages().remove(0);
			deleted++;
		}
		
		return false;
	}

	/**
	 * Sets the flag.
	 * 
	 * @return the uniqueContentOnly
	 */
	public boolean isUniqueContentOnly()
	{
		return uniqueContentOnly;
	}

	/**
	 * Gets the flag.
	 * 
	 * @param uniqueContentOnly the uniqueContentOnly to set
	 */
	public void setUniqueContentOnly(boolean uniqueContentOnly)
	{
		this.uniqueContentOnly = uniqueContentOnly;
		
		if (!uniqueContentOnly)
		{
			reset();
		}
	}

	@Override
	public void reset()
	{
		deleted = 0;		
	}

	@Override
	public boolean isActive()
	{
		return deleted > 0;
	}

}
