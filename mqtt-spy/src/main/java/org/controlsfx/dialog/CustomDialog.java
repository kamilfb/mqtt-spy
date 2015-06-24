/***********************************************************************************
 * 
 * Copyright (c) 2013-2015 ControlsFX, Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and BSD 3-Clause License which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 * The BSD 3-Clause License is aavailable at
 *    http://opensource.org/licenses/BSD-3-Clause
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Contributors:
 * 
 * 	  ControlsFX - initial implementation
 *    Kamil Baczkowicz - minor changes in the extended class, derivative work created from ControlsFX (http://fxexperience.com/controlsfx/)
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