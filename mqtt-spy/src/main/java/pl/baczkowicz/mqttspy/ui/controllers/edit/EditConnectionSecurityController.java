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
package pl.baczkowicz.mqttspy.ui.controllers.edit;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import pl.baczkowicz.mqttspy.common.generated.UserCredentials;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserAuthenticationOptions;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.ui.EditConnectionController;
import pl.baczkowicz.mqttspy.utils.MqttUtils;

/**
 * Controller for editing a single connection - security tab.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EditConnectionSecurityController extends AnchorPane implements Initializable, EditConnectionSubController
{
	/** The parent controller. */
	private EditConnectionController parent;

	// Security
	
	@FXML
	private CheckBox userAuthentication;
	
	@FXML
	private TextField username;
	
	@FXML
	private RadioButton predefinedUsername;
	
	@FXML
	private RadioButton askForUsername;
	
	@FXML
	private RadioButton askForPassword;
	
	@FXML
	private RadioButton predefinedPassword;
	
	@FXML
	private PasswordField password;
	
	// Other fields

	private final ChangeListener basicOnChangeListener = new ChangeListener()
	{
		@Override
		public void changed(ObservableValue observable, Object oldValue, Object newValue)
		{
			onChange();			
		}		
	};
	
	// ===============================
	// === Initialisation ============
	// ===============================

	public void initialize(URL location, ResourceBundle resources)
	{
		// Security
		userAuthentication.selectedProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				updateUserAuthentication();
				
				onChange();
			}		
		});
		username.textProperty().addListener(basicOnChangeListener);
		password.textProperty().addListener(basicOnChangeListener);
		askForUsername.selectedProperty().addListener(basicOnChangeListener);
		askForPassword.selectedProperty().addListener(basicOnChangeListener);
		predefinedUsername.selectedProperty().addListener(basicOnChangeListener);
		predefinedPassword.selectedProperty().addListener(basicOnChangeListener);
	}

	public void init()
	{
		// Nothing to do
	}

	// ===============================
	// === Logic =====================
	// ===============================

	public void onChange()
	{
		parent.onChange();
	}

	@Override
	public UserInterfaceMqttConnectionDetails readValues(final UserInterfaceMqttConnectionDetails connection)
	{
		if (userAuthentication.isSelected())
		{
			final UserAuthenticationOptions userAuthentication = new UserAuthenticationOptions();
						
			userAuthentication.setAskForUsername(askForUsername.isSelected());
			userAuthentication.setAskForPassword(askForPassword.isSelected());
			
			final UserCredentials userCredentials = new UserCredentials();
			userCredentials.setUsername(username.getText());
			userCredentials.setPassword(MqttUtils.encodePassword(password.getText()));			
			
			connection.setUserAuthentication(userAuthentication);
			connection.setUserCredentials(userCredentials);
		}
		
		return connection;
	}

	public void updateUserAuthentication()
	{
		if (userAuthentication.isSelected())
		{
			predefinedUsername.setDisable(false);
			predefinedPassword.setDisable(false);			
			askForUsername.setDisable(false);
			askForPassword.setDisable(false);
			
			if (askForUsername.isSelected())
			{
				username.setDisable(true);
			}
			else				
			{
				username.setDisable(false);
			}
			
			if (askForPassword.isSelected())
			{
				password.setDisable(true);
			}
			else				
			{
				password.setDisable(false);
			}
		}
		else
		{
			username.setDisable(true);			
			password.setDisable(true);
			predefinedUsername.setDisable(true);
			predefinedPassword.setDisable(true);
			askForUsername.setDisable(true);
			askForPassword.setDisable(true);
		}
	}
	
	@Override
	public void displayConnectionDetails(final ConfiguredConnectionDetails connection)
	{
		// Security
		userAuthentication.setSelected(connection.getUserAuthentication() != null && connection.getUserCredentials() != null);

		if (userAuthentication.isSelected())
		{			
			username.setText(connection.getUserCredentials().getUsername());			
			password.setText(MqttUtils.decodePassword(connection.getUserCredentials().getPassword()));	
			
			askForUsername.setSelected(connection.getUserAuthentication().isAskForUsername());
			askForPassword.setSelected(connection.getUserAuthentication().isAskForPassword());
			
			predefinedUsername.setSelected(!connection.getUserAuthentication().isAskForUsername());
			predefinedPassword.setSelected(!connection.getUserAuthentication().isAskForPassword());
		}
		else
		{
			username.setText("");
			password.setText("");
			
			predefinedUsername.setSelected(false);
			predefinedPassword.setSelected(false);
			
			askForUsername.setSelected(true);
			askForPassword.setSelected(true);
		}
		
		updateUserAuthentication();
	}		

	// ===============================
	// === Setters and getters =======
	// ===============================
	
	@Override
	public void setParent(final EditConnectionController controller)
	{
		this.parent = controller;
	}
}
