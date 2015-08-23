/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 * The Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */
package pl.baczkowicz.spy.ui.utils;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttConnectionStatus;
import pl.baczkowicz.spy.utils.ThreadingUtils;

public class TooltipUtils
{
	/**
	 * Updates the given connection tooltip with connection information.
	 * 
	 * @param connection The connection to which the tooltip refers
	 * @param tooltip The tooltip to be updated
	 */
	public static void updateConnectionTooltip(final MqttAsyncConnection connection, final Tooltip tooltip)
	{
		final StringBuffer sb = new StringBuffer();
		sb.append("Status: " + connection.getConnectionStatus().toString().toLowerCase());
		
		if (MqttConnectionStatus.CONNECTED.equals(connection.getConnectionStatus()))
		{
			sb.append(" (" + connection.getLastSuccessfulyConnectionAttempt() + ")");
			
			sb.append(System.getProperty("line.separator"));
			final String sslStatus = connection.getProperties().getSSL() != null ? "on" : "off";
			final String userAuthStatus = connection.getProperties().getUserCredentials() != null ? "on" : "off";
			sb.append("Security: TLS/SSL is " +  sslStatus + "; user authentication is " + userAuthStatus);
		}
		
		if (connection.getConnectionAttempts() > 1)
		{
			sb.append(System.getProperty("line.separator"));
			sb.append("Connection attempts: " + connection.getConnectionAttempts());
		}
				
		if (connection.getDisconnectionReason() != null && !connection.getDisconnectionReason().isEmpty())
		{
			sb.append(System.getProperty("line.separator"));
			sb.append("Last error: " + connection.getDisconnectionReason().toLowerCase());
		}	
		
		tooltip.setText(sb.toString());
	}
	
	/**
	 * Shows the given tooltip for 5 seconds.
	 * 
	 * @param button The button to be used as the parent
	 * @param message The message to be shown in the tooltip
	 */
	public static void showTooltip(final Button button, final String message)
	{
		final Tooltip tooltip = new Tooltip(message);
		button.setTooltip(tooltip);
		tooltip.setAutoHide(true);
		tooltip.setAutoFix(true);
		Point2D p = button.localToScene(0.0, 0.0);	    
		tooltip.show(button.getScene().getWindow(), 
				p.getX() + button.getScene().getX() + button.getScene().getWindow().getX() - 50, 
		        p.getY() + button.getScene().getY() + button.getScene().getWindow().getY() - 50);
		
		new Thread(new Runnable()
		{
			@Override
			public void run()			
			{
				ThreadingUtils.sleep(5000);
				
				Platform.runLater(new Runnable()
				{
					@Override
					public void run()
					{
						button.setTooltip(null);
						tooltip.hide();
					}				
				});
			}		
		}).start();
	}
}
