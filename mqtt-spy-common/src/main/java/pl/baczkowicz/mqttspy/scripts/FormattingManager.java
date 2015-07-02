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

import pl.baczkowicz.mqttspy.common.generated.FormatterDetails;
import pl.baczkowicz.mqttspy.storage.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.utils.FormattingUtils;

public class FormattingManager
{
	private ScriptBasedFormatter scriptFormatter;
	
	public FormattingManager(final ScriptManager scriptManager)
	{
		this.scriptFormatter = new ScriptBasedFormatter(scriptManager);
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
	
//	public String getFormattedPayload(final FormatterDetails formatter)
//	{
//		format(formatter);
//		
//		return formattedPayload;
//	}
}
