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
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.ConfigurationUtils;
import pl.baczkowicz.mqttspy.events.EventManager;
import pl.baczkowicz.mqttspy.ui.MainController;
import pl.baczkowicz.mqttspy.ui.utils.FxmlUtils;
import pl.baczkowicz.mqttspy.utils.IdGenerator;

/** 
 * The main class, loading the app.
 */
public class Main extends Application
{
	/** Initial and minimal scene/stage width. */	
	public final static int DEFAULT_WIDTH = 800;

	/** Initial and minimal scene/stage height. */
	public final static int DEFAULT_HEIGHT = 600;
	
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
		final EventManager eventManager = new EventManager();			
		final IdGenerator connectionIdGenerator = new IdGenerator();
				
		try
		{
			final ConfigurationManager configurationManager = new ConfigurationManager(eventManager, connectionIdGenerator);			
			
			// Load the main window
			final URL resource = getClass().getResource(FxmlUtils.FXML_PACKAGE + FxmlUtils.FXML_LOCATION + "MainWindow.fxml");
			final FXMLLoader loader = new FXMLLoader(resource);

			// Get the associated pane
			AnchorPane pane = (AnchorPane) loader.load();
			
			final Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
			
			// Set scene width, height and style
			final double height = Math.min(ConfigurationUtils.getApplicationHeight(configurationManager), primaryScreenBounds.getHeight());			
			final double width = Math.min(ConfigurationUtils.getApplicationWidth(configurationManager), primaryScreenBounds.getWidth());
			
			final Scene scene = new Scene(pane, width, height);			
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			// Get the associated controller
			final MainController mainController = (MainController) loader.getController();
			mainController.setEventManager(eventManager);
			mainController.setConfigurationManager(configurationManager);
			mainController.setSelectedPerspective(ConfigurationUtils.getApplicationPerspective(configurationManager));
			mainController.getResizeMessagePaneMenu().setSelected(ConfigurationUtils.getResizeMessagePane(configurationManager));

			// Set the stage's properties
			primaryStage.setScene(scene);	
			primaryStage.setMaximized(ConfigurationUtils.getApplicationMaximized(configurationManager));
			
			// Initialise resources in the main controller			
			mainController.setApplication(this);
			mainController.setStage(primaryStage);
			mainController.setLastHeight(height);
			mainController.setLastWidth(width);
			mainController.init();
			final Image applicationIcon = new Image(getClass().getResourceAsStream("/images/mqtt-spy-logo.png"));
		    primaryStage.getIcons().add(applicationIcon);
			
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
