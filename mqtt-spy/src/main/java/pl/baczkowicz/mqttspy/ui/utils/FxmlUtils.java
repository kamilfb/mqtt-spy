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
package pl.baczkowicz.mqttspy.ui.utils;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import pl.baczkowicz.mqttspy.exceptions.CriticalException;

/**
 * FXML-related utilities.
 */
public class FxmlUtils
{
	/** Package with all FXML files. */
	public final static String FXML_PACKAGE = "ui/";

	/** Folder with all FXML files. */
	public static final String FXML_LOCATION = "fxml/";

	/**
	 * Creates an FXML loader.
	 * 
	 * @param parent Parent object
	 * @param fxmlFile The FXML file to load
	 * 
	 * @return FXMLLoader
	 */
	public static FXMLLoader createFXMLLoader(final Object parent, final String fxmlFile)
	{
		return new FXMLLoader(parent.getClass().getResource(fxmlFile));
	}
	
	/**
	 * Loads an anchor pane using the supplied loader.
	 * 
	 * @param loader The FXML loader to be used
	 * 
	 * @return The loader AnchorPane
	 */
	public static AnchorPane loadAnchorPane(final FXMLLoader loader)
	{
		try
		{
			return (AnchorPane) loader.load();
		}
		catch (IOException e)
		{
			// TODO: log
			throw new CriticalException("Cannot load FXML", e);
		}
	}
}
