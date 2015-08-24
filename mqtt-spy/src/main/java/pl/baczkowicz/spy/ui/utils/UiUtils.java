/***********************************************************************************
 * 
 * Copyright (c) 2014 Kamil Baczkowicz
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

import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * General purpose utilities.
 */
public class UiUtils
{
	public static void copyToClipboard(final String value)
	{
		final ClipboardContent content = new ClipboardContent();
		content.putString(value);
		Clipboard.getSystemClipboard().setContent(content);
	}
	
	public static ImageView createImage(final String iconLocation, final int size)
	{
		final ImageView icon = new ImageView(iconLocation);
		icon.setFitHeight(size);
		icon.setFitWidth(size);
		return icon;
	}
	
	public static ImageView createImage(final String iconLocation, final int size, final String style)
	{
		final ImageView icon = new ImageView(iconLocation);
		icon.setFitHeight(size);
		icon.setFitWidth(size);
		icon.getStyleClass().add(style);
		return icon;
	}
}
