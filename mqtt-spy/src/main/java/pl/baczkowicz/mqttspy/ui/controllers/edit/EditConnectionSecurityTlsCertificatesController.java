/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
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
package pl.baczkowicz.mqttspy.ui.controllers.edit;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import pl.baczkowicz.mqttspy.common.generated.SecureSocketSettings;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.ui.EditConnectionController;
import pl.baczkowicz.spy.common.generated.SecureSocketModeEnum;

/**
 * Controller for editing a single connection - security tab - certificates pane.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EditConnectionSecurityTlsCertificatesController extends AnchorPane implements Initializable
{
	/** The parent controller. */
	private EditConnectionController parent;
	
	@FXML
	private AnchorPane tlsCertificatesPane;

	// Certificates
	
	@FXML
	private TextField certificateAuthorityFile;
	
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
	
	@FXML
	private Label clientKeyPemLabel;
	
	@FXML
	private CheckBox clientKeyPemFormatted;
	
	@FXML
	private Button editCaCrtFileButton;
	
	@FXML
	private Button editClientCrtFileButton;
	
	@FXML
	private Button editClientKeyFileButton;
	
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
		// Certificates
		certificateAuthorityFile.textProperty().addListener(basicOnChangeListener);
		clientAuthorityFile.textProperty().addListener(basicOnChangeListener);
		clientKeyFile.textProperty().addListener(basicOnChangeListener);
		clientPassword.textProperty().addListener(basicOnChangeListener);
		clientKeyPemFormatted.selectedProperty().addListener(basicOnChangeListener);
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
	
	public void updateSSL(final SecureSocketModeEnum mode)
	{
		final boolean certificates = SecureSocketModeEnum.SERVER_ONLY.equals(mode)	|| SecureSocketModeEnum.SERVER_AND_CLIENT.equals(mode);
				
		if (certificates)
		{
			final boolean serverAndClient = SecureSocketModeEnum.SERVER_AND_CLIENT.equals(mode);		
		
			clientPassword.setVisible(serverAndClient);
			clientKeyFile.setVisible(serverAndClient);
			clientAuthorityFile.setVisible(serverAndClient);
			
			clientKeyPasswordLabel.setVisible(serverAndClient);
			clientKeyFileLabel.setVisible(serverAndClient);
			clientAuthorityFileLabel.setVisible(serverAndClient);
			clientKeyPemLabel.setVisible(serverAndClient);
			clientKeyPemFormatted.setVisible(serverAndClient);
			
			editClientCrtFileButton.setVisible(serverAndClient);
			editClientKeyFileButton.setVisible(serverAndClient);
		}
	}

	public void readAndSetValues(final SecureSocketModeEnum mode, final UserInterfaceMqttConnectionDetails connection)
	{
		if (mode == null || SecureSocketModeEnum.DISABLED.equals(mode))
		{
			connection.setSSL(null);
		}		
		else
		{
			final SecureSocketSettings sslSettings = connection.getSSL();
						
			final boolean certificates = SecureSocketModeEnum.SERVER_ONLY.equals(mode)
					|| SecureSocketModeEnum.SERVER_AND_CLIENT.equals(mode);
			
			if (certificates)
			{			
				sslSettings.setCertificateAuthorityFile(certificateAuthorityFile.getText());
				sslSettings.setClientCertificateFile(clientAuthorityFile.getText());
				sslSettings.setClientKeyFile(clientKeyFile.getText());
				sslSettings.setClientKeyPassword(clientPassword.getText());				
				sslSettings.setClientKeyPEM(clientKeyPemFormatted.isSelected());
			}			
			
			connection.setSSL(sslSettings);
		}
	}

	public void displayConnectionDetails(final ConfiguredConnectionDetails connection)
	{
		if (connection.getSSL() != null)
		{	
			// Certificates
			certificateAuthorityFile.setText(connection.getSSL().getCertificateAuthorityFile());
			clientAuthorityFile.setText(connection.getSSL().getClientCertificateFile());
			clientKeyFile.setText(connection.getSSL().getClientKeyFile());
			clientPassword.setText(connection.getSSL().getClientKeyPassword());	
			clientKeyPemFormatted.setSelected(Boolean.TRUE.equals(connection.getSSL().isClientKeyPEM()));
		}
	}
	
	@FXML
	private void editCaCrtFile()
	{
		// TODO
	}
	
	@FXML
	private void editClientCrtFile()
	{
		// TODO
	}
	
	@FXML
	private void editClientKeyFile()
	{
		// TODO
	}

	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setParent(final EditConnectionController controller)
	{
		this.parent = controller;
	}

	/**
	 * @return the tlsCertificatesPane
	 */
	public AnchorPane getTlsCertificatesPane()
	{
		return tlsCertificatesPane;
	}

	/**
	 * @param tlsCertificatesPane the tlsCertificatesPane to set
	 */
	public void setTlsCertificatesPane(AnchorPane tlsCertificatesPane)
	{
		this.tlsCertificatesPane = tlsCertificatesPane;
	}
}
