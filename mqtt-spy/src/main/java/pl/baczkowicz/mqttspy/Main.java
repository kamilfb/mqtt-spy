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
package pl.baczkowicz.mqttspy;

import java.io.File;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.ui.MainController;
import pl.baczkowicz.mqttspy.ui.utils.FxmlUtils;

/** 
 * The main class, loading the app.
 */
public class Main extends Application
{
	// TODO: this might need changing or moving to a property file.
	/** Initial and minimal scene/stage width. */	
	public final static int WIDTH = 800;

	// TODO: this might need changing or moving to a property file.
	/** Initial and minimal scene/stage height. */
	public final static int HEIGHT = 600;
	
	/** Name of the parameter supplied on the command line to indicate where to find the configuration file - optional. */
	private final static String CONFIGURATION_PARAMETER_NAME = "configuration";
	
	/** Name of the parameter supplied on the command line to indicate no configuration wanted - optional. */
	private final static String NO_CONFIGURATION_PARAMETER_NAME = "no-configuration";

	@Override
	/**
	 * Starts the application.
	 */
	public void start(final Stage primaryStage)
	{
		try
		{
			// Load the main window
			final URL resource = getClass().getResource(FxmlUtils.FXML_PACKAGE + FxmlUtils.FXML_LOCATION + "MainWindow.fxml");
			final FXMLLoader loader = new FXMLLoader(resource);

			// Get the associated pane
			AnchorPane pane = (AnchorPane) loader.load();
			
			// Set scene width, height and style
			final Scene scene = new Scene(pane, WIDTH, HEIGHT);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			// Get the associated controller
			final MainController mainController = (MainController) loader.getController();

			// Set the stage's properties
			primaryStage.setScene(scene);			
			
			// TODO: not sure we want those minimum values
			primaryStage.setMinWidth(WIDTH);
			primaryStage.setMinHeight(HEIGHT / 2);
			primaryStage.setHeight(HEIGHT);

			// Initialise resources in the main controller			
			mainController.setApplication(this);
			mainController.setStage(primaryStage);
			mainController.init();
			
			// Show the main window
			primaryStage.show();
			
			// Load the config file if specified
			final String noConfig = this.getParameters().getNamed().get(NO_CONFIGURATION_PARAMETER_NAME); 
			final String configurationFileLocation = this.getParameters().getNamed().get(CONFIGURATION_PARAMETER_NAME);
			
			if (noConfig != null)
			{
				// Do nothing - no config wanted
			}
			else if (configurationFileLocation != null)
			{
				mainController.loadConfigurationFileAndShowErrorWhenApplicable(new File(configurationFileLocation));				
			}
			else
			{
				// If no configuration parameter is specified, use the user's home directory and the default configuration file name
				mainController.loadDefaultConfigurationFile();						
			}
		}
		catch (Exception e)
		{
			LoggerFactory.getLogger(Main.class).error("Error while loading the main window", e);
		}
	}

	public static void main(String[] args)
	{
		launch(args);
	}
}
