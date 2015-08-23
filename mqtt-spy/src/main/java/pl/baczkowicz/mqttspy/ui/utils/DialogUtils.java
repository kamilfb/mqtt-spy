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
package pl.baczkowicz.mqttspy.ui.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javafx.beans.binding.DoubleBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import javafx.util.Pair;

import org.controlsfx.dialog.CustomDialogs;

import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.ConfigurationUtils;
import pl.baczkowicz.mqttspy.ui.controls.DialogAction;

/**
 * Utilities for creating all sorts of dialogs.
 */
@SuppressWarnings("deprecation")
public class DialogUtils
{
	/**
	 * Shows the choice dialog when missing configuration file is detected.
	 * 
	 * @param title The title of the window
	 * @param window The parent
	 * 
	 * @return True when action performed / configuration file created
	 */
	public static boolean showDefaultConfigurationFileMissingChoice(final String title, final Window window)
	{	
		// TODO: use Java dialogs
		final DialogAction createWithSample = new DialogAction("Create mqtt-spy configuration file with sample content",
				System.getProperty("line.separator") + "This creates a configuration file " +  
                "in \"" + ConfigurationManager.DEFAULT_HOME_DIRECTORY + "\"" + 
                " called \"" + ConfigurationManager.DEFAULT_FILE_NAME + "\"" + 
                ", which will include sample connections to localhost and iot.eclipse.org.");
		
		 final DialogAction createEmpty = new DialogAction("Create empty mqtt-spy configuration file",
				 System.getProperty("line.separator") + "This creates a configuration file " +  
                 "in \"" + ConfigurationManager.DEFAULT_HOME_DIRECTORY + "\"" + 
                 " called \"" + ConfigurationManager.DEFAULT_FILE_NAME + "\" with no sample connections.");
		 
		 final DialogAction copyExisting = new DialogAction("Copy existing mqtt-spy configuration file",
				 System.getProperty("line.separator") + "This copies an existing configuration file (selected in the next step) " +  
                 "to \"" + ConfigurationManager.DEFAULT_HOME_DIRECTORY + "\"" + 
                 " and renames it to \"" + ConfigurationManager.DEFAULT_FILE_NAME + "\".");
		 
		 final DialogAction dontDoAnything = new DialogAction("Don't do anything",
				 System.getProperty("line.separator") + "You can still point mqtt-spy at your chosen configuration file " +  
                 "by using the \"--configuration=my_custom_path\"" + 
                 " command line parameter or open a configuration file from the main menu.");
		
		final List<DialogAction> links = Arrays.asList(createWithSample, createEmpty, copyExisting, dontDoAnything);
		
//		final CustomDialogs dialog = new CustomDialogs();
//		dialog
//	      .owner(window)
//	      .title(title)
//	      .masthead(null)
//	      .message("Please select one of the following options with regards to the mqtt-spy configuration file:");
		
		Optional<DialogAction> response = DialogUtils.showCommandLinks(title,
				"Please select one of the following options with regards to the mqtt-spy configuration file:",
				links.get(0), links, 650, 30, 110);
		boolean configurationFileCreated = false;
		
		if (!response.isPresent())
		{
			// Do nothing
		}
		else if (response.get().getHeading().toLowerCase().contains("sample"))
		{
			configurationFileCreated = ConfigurationUtils.createDefaultConfigFromClassPath("sample");
		}
		else if (response.get().getHeading().toLowerCase().contains("empty"))
		{
			configurationFileCreated = ConfigurationUtils.createDefaultConfigFromClassPath("empty");
		}
		else if (response.get().getHeading().toLowerCase().contains("copy"))
		{
			final FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select configuration file to copy");
			String extensions = "xml";
			fileChooser.setSelectedExtensionFilter(new ExtensionFilter("XML file", extensions));

			final File selectedFile = fileChooser.showOpenDialog(window);

			if (selectedFile != null)
			{
				configurationFileCreated = ConfigurationUtils.createDefaultConfigFromFile(selectedFile);
			}
		}
		else
		{
			// Do nothing
		}
		
		return configurationFileCreated;
	}	
	

	/**
     * Show a dialog filled with provided command links. Command links are used instead of button bar and represent 
     * a set of available 'radio' buttons
	 * @param message 
	 * @param string 
     * @param defaultCommandLink command is set to be default. Null means no default
     * @param links list of command links presented in specified sequence 
     * @return action used to close dialog (it is either one of command links or CANCEL) 
     */
    public static Optional<DialogAction> showCommandLinks(final String title, final String message, DialogAction defaultCommandLink, List<DialogAction> links, 
    		final int minWidth, final int longMessageMinHeight, double maxHeight) 
    {
        final Dialog<DialogAction> dialog = new Dialog<DialogAction>();
        dialog.setTitle(title);
        dialog.getDialogPane().getScene().getStylesheets().add(DialogUtils.class.getResource(
        		"/pl/baczkowicz/mqttspy/application.css").toExternalForm());
        dialog.getDialogPane().getButtonTypes().clear();
        
        final ImageView image = new ImageView(DialogUtils.class.getResource("/images/dialog-information.png").toString());
        image.setFitHeight(55);
        image.setFitWidth(55);
     	dialog.setGraphic(image);
        dialog.setResizable(true);
        
        Label label = new Label(message);
		label.setAlignment(Pos.TOP_LEFT);
		label.setTextAlignment(TextAlignment.LEFT);
		label.setMaxWidth(Double.MAX_VALUE);
		label.setMaxHeight(Double.MAX_VALUE);
		label.setWrapText(true);
		label.getStyleClass().add("command-link-message");

        final int gapSize = 10;
        final List<Button> buttons = new ArrayList<>(links.size());
        
		GridPane content = new GridPane()
		{
			@Override
			protected double computePrefWidth(double height)
			{
				double pw = 0;

				for (int i = 0; i < buttons.size(); i++)
				{
					Button btn = buttons.get(i);
					pw = Math.min(pw, btn.prefWidth(-1));
				}
				return pw + gapSize;
			}

			@Override
			protected double computePrefHeight(double width)
			{
				double ph = 10;

				for (int i = 0; i < buttons.size(); i++)
				{
					Button btn = buttons.get(i);
					ph += btn.prefHeight(width) + gapSize;
				}
				return ph * 1.5;
			}
		};
		int row = 0;
		content.add(label, 0, row++);
		content.setMinWidth(minWidth);
        content.setHgap(gapSize);
        content.setVgap(gapSize);
        
		for (final DialogAction commandLink : links)
		{
			if (commandLink == null)
				continue;

			final Button button = buildCommandLinkButton(commandLink, longMessageMinHeight, maxHeight);
			button.setDefaultButton(commandLink == defaultCommandLink);
			button.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent ae)
				{
					dialog.setResultConverter(dialogButton -> 
					{					    
					    return commandLink;
					});
					dialog.close();
				}
			});

			GridPane.setHgrow(button, Priority.ALWAYS);
			GridPane.setVgrow(button, Priority.ALWAYS);
			content.add(button, 0, row++);
			buttons.add(button);
		}
        
        // last button gets some extra padding (hacky)
        GridPane.setMargin(buttons.get(buttons.size() - 1), new Insets(0,0,10,0));
        
        dialog.getDialogPane().setContent(content);
        //dlg.getActions().clear();
        
        return dialog.showAndWait();
    }
    


    private static Button buildCommandLinkButton(DialogAction commandLink, final int longMessageMinHeight, double maxHeight) 
    {
        // put the content inside a button
        final Button button = new Button();
        button.getStyleClass().addAll("command-link-button");
        button.setMaxHeight(maxHeight);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        
        final Label titleLabel = new Label(commandLink.getHeading() );
        titleLabel.minWidthProperty().bind(new DoubleBinding() {
            {
                bind(titleLabel.prefWidthProperty());
            }
            
            @Override protected double computeValue() {
                return titleLabel.getPrefWidth() + 400;
            }
        });
        titleLabel.getStyleClass().addAll("line-1");
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.TOP_LEFT);
        GridPane.setVgrow(titleLabel, Priority.NEVER);

        Label messageLabel = new Label(commandLink.getLongText() );
        messageLabel.setMinHeight(longMessageMinHeight);
        messageLabel.setPrefHeight(longMessageMinHeight + 10);
        //messageLabel.setMaxHeight(longMessageMaxHeight);
        messageLabel.getStyleClass().addAll("line-2");
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.TOP_LEFT);
        messageLabel.setMaxHeight(Double.MAX_VALUE);
        // GridPane.setVgrow(messageLabel, Priority.SOMETIMES);
        GridPane.setVgrow(messageLabel, Priority.ALWAYS);
        
        //Node graphic = null;
        final ImageView icon = new ImageView(CustomDialogs.class.getResource("/images/go-next-green.png").toString());
        icon.setFitHeight(20);
        icon.setFitWidth(20);
        Pane graphicContainer = new Pane(icon);
        graphicContainer.getStyleClass().add("graphic-container");
        GridPane.setValignment(graphicContainer, VPos.TOP);
        GridPane.setMargin(graphicContainer, new Insets(0,15,0,0));
        
        GridPane grid = new GridPane();
        grid.minWidthProperty().bind(titleLabel.prefWidthProperty());
        grid.setMaxHeight(Double.MAX_VALUE);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.getStyleClass().add("container");
        grid.add(graphicContainer, 0, 0, 1, 2);
        grid.add(titleLabel, 1, 0);
        grid.add(messageLabel, 1, 1);

        button.setGraphic(grid);
        button.minWidthProperty().bind(titleLabel.prefWidthProperty());
        
        return button;
    }
}
