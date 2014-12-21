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
 *    Kamil Baczkowicz - extended class, based on ControlsFx code
 *    
 */
package org.controlsfx.dialog;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.text.TextAlignment;
import javafx.stage.Window;

import org.controlsfx.tools.Utils;

@SuppressWarnings("deprecation")
/**
 * This is an extended version of the ControlsFx Dialog class to add required functionality.
 */
public class CustomDialog extends Dialog
{
	public CustomDialog(Object owner, String title)
	{
		super(owner, title);
		Window window = Utils.getWindow(owner);
		this.getStylesheets().addAll(window.getScene().getStylesheets());
	}
	
	/**
	 * Sets content with no limit on max width.
	 * 
	 * @param contentText The content text to set
	 */
	public final void setContentWithNoMaxWidth(String contentText)
	{
		if (contentText == null)
			return;

		Label label = new Label(contentText);
		label.setAlignment(Pos.TOP_LEFT);
		label.setTextAlignment(TextAlignment.LEFT);
		label.setMaxWidth(Double.MAX_VALUE);
		label.setMaxHeight(Double.MAX_VALUE);

		label.setWrapText(true);

		setContent(label);
	}
}