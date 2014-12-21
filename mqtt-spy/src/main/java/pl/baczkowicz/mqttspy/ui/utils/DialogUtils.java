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

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import javafx.util.Pair;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.CustomDialogs;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogAction;
import org.controlsfx.dialog.Dialogs;

import pl.baczkowicz.mqttspy.common.generated.UserCredentials;
import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.ConfigurationUtils;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttConnectionStatus;
import pl.baczkowicz.mqttspy.stats.StatisticsManager;
import pl.baczkowicz.mqttspy.utils.MqttUtils;
import pl.baczkowicz.mqttspy.utils.ThreadingUtils;

/**
 * Utilities for creating all sorts of dialogs.
 */
@SuppressWarnings("deprecation")
public class DialogUtils
{
	/** Format of the stats label. */
	public static final String STATS_FORMAT = "load: " + getPeriodValues();
	
	/**
	 * Creates the list of all periods defined in the statistics manager.
	 * 
	 * @return List of all periods
	 */
	public static String getPeriodList()
	{
		final StringBuffer sb = new StringBuffer();
		
		final Iterator<Integer> iterator = StatisticsManager.periods.iterator();
		while (iterator.hasNext()) 
		{
			final int period = (int) iterator.next();
			if (period > 60)
			{
				sb.append((period / 60) + "m");
			}
			else
			{
				sb.append(period + "s");
			}
			
			if (iterator.hasNext())
			{
				sb.append(", ");
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Creates the stats format for all periods defined in the statistics manager.
	 * 
	 * @return Format for all periods
	 */
	public static String getPeriodValues()
	{
		final StringBuffer sb = new StringBuffer();
		
		final Iterator<Integer> iterator = StatisticsManager.periods.iterator();
		while (iterator.hasNext()) 
		{
			sb.append("%.1f");	
			iterator.next();
			
			if (iterator.hasNext())
			{
				sb.append("/");
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Shows an error dialog.
	 * 
	 * @param title Title of the dialog
	 * @param message Message to be displayed
	 */
	public static void showError(final String title, final String message)
	{
		Dialogs.create().owner(null).title(title).masthead(null).message(message).showError();
	}
	
	/**
	 * Shows a warning dialog with "Invalid value detected" title.
	 * 
	 * @param message The message to be displayed
	 */
	public static void showValidationWarning(final String message)
	{
		Dialogs.create().owner(null).title("Invalid value detected").masthead(null)
				.message(message + ".").showWarning();
	}

	/**
	 * Asks the user whether to save unsaved changes.
	 * 
	 * @param parameter The parameter that has changed
	 * 
	 * @return The user's response
	 */
	public static Action showApplyChangesQuestion(final String parameter)
	{
		return Dialogs.create().owner(null).title("Unsaved changes detected").masthead(null)
		.message("You've got unsaved changes for " + parameter + ". Do you want to save/apply them now?").showConfirm();		
	}
	
	/**
	 * Asks the user whether to delete the given element/parameter.
	 * 
	 * @param parameter The element to delete
	 * 
	 * @return The user's response
	 */
	public static Action showDeleteQuestion(final String parameter)
	{
		return Dialogs.create().owner(null).title("Deleting connection").masthead(null)
		.actions(Dialog.ACTION_YES, Dialog.ACTION_CANCEL)
		.message("Are you sure you want to delete connection '" + parameter + "'? This cannot be undone.").showConfirm();		
	}

	/**
	 * Asks the user to review/complete username and password information.
	 * 
	 * @param owner The window owner
	 * @param connectionName Name of the connection
	 * @param userCredentials Existing user credentials
	 * 
	 * @return True when confirmed by user
	 */
	public static boolean showUsernameAndPasswordDialog(final Object owner,
			String connectionName, final UserCredentials userCredentials)
	{
		final Pair<String, String> userInfo = new Pair<String, String>(
				userCredentials.getUsername(), 
				MqttUtils.decodePassword(userCredentials.getPassword()));
		
		final CustomDialogs dialog = new CustomDialogs();
		dialog.owner(owner);
		dialog.masthead("Enter MQTT user name and password:");
		dialog.title("User credentials for connection " + connectionName);
		Optional<Pair<String, String>> response = dialog.showLogin(userInfo, null);
		
		if (response.isPresent())
		{
			userCredentials.setUsername(response.get().getKey());			
			userCredentials.setPassword(MqttUtils.encodePassword(response.get().getValue()));
			return true;
		}
		
		return false;
	}

	/**
	 * Shows a dialog with "Invalid configuration file" title.
	 * 
	 * @param message The message to be shown 
	 */
	public static void showInvalidConfigurationFileDialog(final String message)
	{
		Dialogs.create().owner(null).title("Invalid configuration file").masthead(null)
				.message(message).showError();
	}

	/**
	 * Shows a dialog saying the given file is read-only.
	 * 
	 * @param absolutePath The path to the file
	 */
	public static void showReadOnlyWarning(final String absolutePath)
	{
		Dialogs.create()
				.owner(null)
				.title("Read-only configuration file")
				.masthead(null)
				.message(
						"The configuration file that has been loaded (" + absolutePath
								+ ") is read-only. Changes won't be saved. "
								+ "Please make the file writeable for any changes to be saved.")
				.showWarning();
	}
	
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
		final DialogAction createWithSample = new DialogAction("Create mqtt-spy configuration file with sample content");
		createWithSample.setLongText(System.getProperty("line.separator") + "This creates a configuration file " +  
                "in \"" + ConfigurationManager.DEFAULT_HOME_DIRECTORY + "\"" + 
                " called \"" + ConfigurationManager.DEFAULT_FILE_NAME + "\"" + 
                ", which will include sample connections to localhost and iot.eclipse.org.");
		
		 final DialogAction createEmpty = new DialogAction("Create empty mqtt-spy configuration file");
		 createEmpty.setLongText(
 				System.getProperty("line.separator") + "This creates a configuration file " +  
                 "in \"" + ConfigurationManager.DEFAULT_HOME_DIRECTORY + "\"" + 
                 " called \"" + ConfigurationManager.DEFAULT_FILE_NAME + "\" with no sample connections.");
		 
		 final DialogAction copyExisting = new DialogAction("Copy existing mqtt-spy configuration file");
		 copyExisting.setLongText(
				 System.getProperty("line.separator") + "This copies an existing configuration file (selected in the next step) " +  
                 "to \"" + ConfigurationManager.DEFAULT_HOME_DIRECTORY + "\"" + 
                 " and renames it to \"" + ConfigurationManager.DEFAULT_FILE_NAME + "\".");
		 
		 final DialogAction dontDoAnything = new DialogAction("Don't do anything");
		 dontDoAnything.setLongText(
				 System.getProperty("line.separator") + "You can still point mqtt-spy at your chosen configuration file " +  
                 "by using the \"--configuration=my_custom_path\"" + 
                 " command line parameter or open a configuration file from the main menu.");
		
		final List<DialogAction> links = Arrays.asList(createWithSample, createEmpty, copyExisting, dontDoAnything);
		
		final CustomDialogs dialog = new CustomDialogs();
		dialog
	      .owner(window)
	      .title(title)
	      .masthead(null)
	      .message("Please select one of the following options with regards to the mqtt-spy configuration file:");
		
		Action response = dialog.showCommandLinks(links.get(0), links, 650, 30, 110);
		boolean configurationFileCreated = false;
		
		if (response.textProperty().getValue().toLowerCase().contains("sample"))
		{
			configurationFileCreated = ConfigurationUtils.createDefaultConfigFromClassPath("sample");
		}
		else if (response.textProperty().getValue().toLowerCase().contains("empty"))
		{
			configurationFileCreated = ConfigurationUtils.createDefaultConfigFromClassPath("empty");
		}
		else if (response.textProperty().getValue().toLowerCase().contains("copy"))
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
	 * Shows a worker / progress dialog.
	 * 
	 * @param readAndProcess The task backing up the dialog
	 */
	public static void showWorkerDialog(final Task<?> readAndProcess)
	{
		Dialogs.create().showWorkerProgress(readAndProcess);
	}
}
