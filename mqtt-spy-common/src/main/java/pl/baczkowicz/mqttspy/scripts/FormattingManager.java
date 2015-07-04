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
package pl.baczkowicz.mqttspy.scripts;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.FormatterDetails;
import pl.baczkowicz.mqttspy.storage.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.utils.FormattingUtils;

public class FormattingManager
{
	final static Logger logger = LoggerFactory.getLogger(FormattingManager.class);
	
	private ScriptBasedFormatter scriptFormatter;
	
	public FormattingManager(final ScriptManager scriptManager)
	{
		this.scriptFormatter = new ScriptBasedFormatter(scriptManager);
	}
	
	public void initialiseFormatter(final FormatterDetails formatter)
	{
		if (formatter == null)
		{
			return;
		}
		
		try
		{		
			scriptFormatter.addFormatter(formatter);
		}
		catch (ScriptException e)
		{
			logger.error("Couldn't load the formatter");
		}
	}
	
	public void formatMessage(final FormattedMqttMessage message, final FormatterDetails formatter)
	{
		if (formatter == null)
		{
			message.setFormattedPayload(message.getPayload());
		}		
		else if (!formatter.equals(message.getLastUsedFormatter()))
		{
			message.setLastUsedFormatter(formatter);
			
			if (FormattingUtils.isScriptBased(formatter))
			{
				message.setFormattedPayload(scriptFormatter.formatMessage(formatter, message));
			}
			else
			{
				// Use the raw payload to make sure any formatting/encoding that is applied is correct
				message.setFormattedPayload(FormattingUtils.checkAndFormatText(formatter, message.getRawMessage().getPayload()));
			}
		}
	}
}
