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
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import pl.baczkowicz.mqttspy.common.generated.ProtocolEnum;
import pl.baczkowicz.mqttspy.common.generated.SslModeEnum;
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
	
	@FXML
	private ComboBox<SslModeEnum> modeCombo;
	
	@FXML
	private ComboBox<String> protocolCombo;
	
	@FXML
	private AnchorPane customSocketFactoryPane;
	
	@FXML
	private AnchorPane propertiesPane;
	
	@FXML
	private TextField clientPassword;
	
	@FXML
	private TextField clientKeyFile;
	
	@FXML
	private TextField clientAuthorityFile;
	
	@FXML
	private Label clientKeyPasswordLabel;
	
	@FXML
	private Label clientKeyFileLabel;
	
	@FXML
	private Label clientAuthorityFileLabel;
	
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
		final Map<SslModeEnum, String> modeEnumText = new HashMap<>();
		modeEnumText.put(SslModeEnum.DISABLED, "Disabled");
		modeEnumText.put(SslModeEnum.PROPERTIES, "SSL/TLS properties (using default socket factory)");
		modeEnumText.put(SslModeEnum.SERVER_ONLY, "Server authentication only (using custom socket factory)");
		modeEnumText.put(SslModeEnum.SERVER_AND_CLIENT, "Server and client authentication (using custom socket factory)");
				
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
		
		// SSL Protocol
		try
		{
			final SSLContext context = SSLContext.getDefault();
			final SSLSocketFactory sf = context.getSocketFactory();			
			final String[] values = context.getSupportedSSLParameters().getProtocols();
			
			protocolCombo.getSelectionModel().selectedIndexProperty().addListener(basicOnChangeListener);
			protocolCombo.getItems().addAll(values);			
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}		
		
		// SSL Mode
		modeCombo.getSelectionModel().selectedIndexProperty().addListener(basicOnChangeListener);
		modeCombo.setCellFactory(new Callback<ListView<SslModeEnum>, ListCell<SslModeEnum>>()
		{
			@Override
			public ListCell<SslModeEnum> call(ListView<SslModeEnum> l)
			{
				return new ListCell<SslModeEnum>()
				{
					@Override
					protected void updateItem(SslModeEnum item, boolean empty)
					{
						super.updateItem(item, empty);
						if (item == null || empty)
						{
							setText(null);
						}
						else
						{									
							setText(modeEnumText.get(item));
						}
					}
				};
			}
		});
		modeCombo.setConverter(new StringConverter<SslModeEnum>()
		{
			@Override
			public String toString(SslModeEnum item)
			{
				if (item == null)
				{
					return null;
				}
				else
				{
					return modeEnumText.get(item);
				}
			}

			@Override
			public SslModeEnum fromString(String id)
			{
				return null;
			}
		});
		
		for (SslModeEnum modeEnum : SslModeEnum.values())
		{
			modeCombo.getItems().add(modeEnum);
		}
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
		final boolean serverAndClient = SslModeEnum.SERVER_AND_CLIENT.equals(modeCombo.getSelectionModel().getSelectedItem());
		
		propertiesPane.setVisible(SslModeEnum.PROPERTIES.equals(modeCombo.getSelectionModel().getSelectedItem()));
		
		customSocketFactoryPane.setVisible(serverAndClient
				|| SslModeEnum.SERVER_ONLY.equals(modeCombo.getSelectionModel().getSelectedItem()));
		
		clientPassword.setVisible(serverAndClient);
		clientKeyFile.setVisible(serverAndClient);
		clientAuthorityFile.setVisible(serverAndClient);
		
		clientKeyPasswordLabel.setVisible(serverAndClient);
		clientKeyFileLabel.setVisible(serverAndClient);
		clientAuthorityFileLabel.setVisible(serverAndClient);
		
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
	
	@FXML
	private void addProperty()
	{
		
	}
	
	@FXML
	private void removeProperty()
	{
		
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
