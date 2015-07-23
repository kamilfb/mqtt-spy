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
package pl.baczkowicz.mqttspy.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.MessageLog;
import pl.baczkowicz.mqttspy.common.generated.ProtocolEnum;
import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.ConfigurationUtils;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.exceptions.ConfigurationException;
import pl.baczkowicz.mqttspy.ui.connections.ConnectionManager;
import pl.baczkowicz.mqttspy.ui.controllers.edit.EditConnectionConnectivityController;
import pl.baczkowicz.mqttspy.ui.controllers.edit.EditConnectionLastWillController;
import pl.baczkowicz.mqttspy.ui.controllers.edit.EditConnectionMessageLogController;
import pl.baczkowicz.mqttspy.ui.controllers.edit.EditConnectionOtherController;
import pl.baczkowicz.mqttspy.ui.controllers.edit.EditConnectionPublicationsController;
import pl.baczkowicz.mqttspy.ui.controllers.edit.EditConnectionSecurityController;
import pl.baczkowicz.mqttspy.ui.controllers.edit.EditConnectionSubscriptionsController;
import pl.baczkowicz.mqttspy.ui.utils.ConnectivityUtils;
import pl.baczkowicz.mqttspy.ui.utils.DialogUtils;
import pl.baczkowicz.mqttspy.utils.ConnectionUtils;

/**
 * Controller for editing a single connection.
 */
@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
public class EditConnectionController extends AnchorPane implements Initializable
{
	final static Logger logger = LoggerFactory.getLogger(EditConnectionController.class);
	
	@FXML
	private TextField connectionNameText;
	
	@FXML
	private ComboBox<ProtocolEnum> protocolCombo;
	
	// Action buttons
	
	@FXML
	private Button connectButton;
	
	@FXML
	private Button cancelButton;
	
	@FXML
	private Button saveButton;
	
	@FXML
	private Button undoButton;
	
	// Controllers
	
	@FXML
	private EditConnectionConnectivityController editConnectionConnectivityController;
	
	@FXML
	private EditConnectionLastWillController editConnectionLastWillController;
	
	@FXML
	private EditConnectionMessageLogController editConnectionMessageLogController;
	
	@FXML
	private EditConnectionOtherController editConnectionOtherController;
	
	@FXML
	private EditConnectionPublicationsController editConnectionPublicationsController;
	
	@FXML
	private EditConnectionSecurityController editConnectionSecurityController;
	
	@FXML
	private EditConnectionSubscriptionsController editConnectionSubscriptionsController;
	
	// Other fields

	private String lastGeneratedConnectionName = "";
	
	private MainController mainController;

	private ConfiguredConnectionDetails editedConnectionDetails;

	private boolean recordModifications;
    
	private ConfigurationManager configurationManager;

	private EditConnectionsController editConnectionsController;

	private boolean openNewMode;

	private MqttAsyncConnection existingConnection;

	private int noModificationsLock;

	private ConnectionManager connectionManager;

	private boolean emptyConnectionList;
	
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
		connectionNameText.textProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				onChange();
			}		
		});
		
		protocolCombo.getSelectionModel().selectedIndexProperty().addListener(basicOnChangeListener);
		protocolCombo.setCellFactory(new Callback<ListView<ProtocolEnum>, ListCell<ProtocolEnum>>()
		{
			@Override
			public ListCell<ProtocolEnum> call(ListView<ProtocolEnum> l)
			{
				return new ListCell<ProtocolEnum>()
				{
					@Override
					protected void updateItem(ProtocolEnum item, boolean empty)
					{
						super.updateItem(item, empty);
						if (item == null || empty)
						{
							setText(null);
						}
						else
						{									
							setText(item.value());
						}
					}
				};
			}
		});
		protocolCombo.setConverter(new StringConverter<ProtocolEnum>()
		{
			@Override
			public String toString(ProtocolEnum item)
			{
				if (item == null)
				{
					return null;
				}
				else
				{
					return item.value();
				}
			}

			@Override
			public ProtocolEnum fromString(String id)
			{
				return null;
			}
		});
		
		for (ProtocolEnum protocolEnum : ProtocolEnum.values())
		{
			protocolCombo.getItems().add(protocolEnum);
		}
		
		editConnectionConnectivityController.setParent(this);
		editConnectionLastWillController.setParent(this);
		editConnectionMessageLogController.setParent(this);
		editConnectionOtherController.setParent(this);
		editConnectionPublicationsController.setParent(this);
		editConnectionSecurityController.setParent(this);
		editConnectionSubscriptionsController.setParent(this);
	}

	public void init()
	{
		editConnectionOtherController.setConfigurationManager(configurationManager);
		
		editConnectionConnectivityController.init();
		editConnectionLastWillController.init();
		editConnectionMessageLogController.init();
		editConnectionOtherController.init();
		editConnectionPublicationsController.init();
		editConnectionSecurityController.init();
		editConnectionSubscriptionsController.init();
	}

	// ===============================
	// === FXML ======================
	// ===============================

	@FXML
	private void addTimestamp()
	{
		editConnectionConnectivityController.updateClientId(true);
	}
	
	@FXML
	private void undo()
	{
		editedConnectionDetails.undo();
		editConnectionsController.listConnections();
		
		// Note: listing connections should display the existing one
		
		updateButtons();
	}
	
	
	@FXML
	private void save()
	{
		editedConnectionDetails.apply();
		editConnectionsController.listConnections();
				
		updateButtons();
		
		logger.debug("Saving connection " + getConnectionName().getText());
		if (configurationManager.saveConfiguration())
		{
			DialogUtils.showTooltip(saveButton, "Changes for connection " + editedConnectionDetails.getName() + " have been saved.");
		}
	}	
	
	@FXML
	private void cancel()
	{
		// Get a handle to the stage
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		
		// Close the window
		stage.close();
	}
	
	@FXML
	public void createConnection() throws ConfigurationException
	{
		readAndDetectChanges();
		final String validationResult = ConnectivityUtils.validateConnectionDetails(editedConnectionDetails, false);
		
		if (validationResult != null)
		{
			DialogUtils.showValidationWarning(validationResult);
		}
		else
		{					
			if (editedConnectionDetails.isModified())
			{	
				Action response = DialogUtils.showApplyChangesQuestion("connection " + editedConnectionDetails.getName()); 
				if (response == Dialog.ACTION_YES)
				{
					save();
				}
				else if (response == Dialog.ACTION_NO)
				{
					// Do nothing
				}
				else
				{
					return;
				}
			}
			
			if (!openNewMode)
			{
				connectionManager.disconnectAndCloseTab(existingConnection);
			}
			
			logger.info("Opening connection " + getConnectionName().getText());
	
			// Get a handle to the stage
			Stage stage = (Stage) connectButton.getScene().getWindow();
	
			// Close the window
			stage.close();
	 
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{						
						connectionManager.openConnection(editedConnectionDetails, getMainController());
					}
					catch (ConfigurationException e)
					{
						// TODO: show warning dialog for invalid
						logger.error("Cannot open conection {}", editedConnectionDetails.getName(), e);
					}					
				}				
			});
			
		}
	}

	// ===============================
	// === Logic =====================
	// ===============================


	public void updateConnectionName()
	{
		if (connectionNameText.getText().isEmpty()
				|| lastGeneratedConnectionName.equals(connectionNameText.getText()))
		{
			final String newName = ConnectionUtils.composeConnectionName(
					editConnectionConnectivityController.getClientIdText().getText(), 
					editConnectionConnectivityController.getBrokerAddressText().getText());
			connectionNameText.setText(newName);
			lastGeneratedConnectionName = newName;
		}
	}
	
	public void onChange()
	{
		if (recordModifications && !emptyConnectionList)
		{					
			if (readAndDetectChanges())
			{
				updateButtons();
				editConnectionConnectivityController.updateClientId(false);
				editConnectionConnectivityController.updateClientIdLength();
				updateConnectionName();		
				editConnectionConnectivityController.updateReconnection();
				editConnectionSecurityController.updateUserAuthentication();
				editConnectionsController.listConnections();
			}
		}				
	}

	private UserInterfaceMqttConnectionDetails readValues()
	{
		final UserInterfaceMqttConnectionDetails connection = new UserInterfaceMqttConnectionDetails();
		connection.setMessageLog(new MessageLog());
		
		// Populate the default for the values we don't display / are not used
		ConfigurationUtils.populateConnectionDefaults(connection);
		
		connection.setName(connectionNameText.getText());
		connection.setProtocol(protocolCombo.getSelectionModel().getSelectedItem());
		
		editConnectionConnectivityController.readValues(connection);
		editConnectionOtherController.readValues(connection);
		editConnectionSecurityController.readValues(connection);
		editConnectionMessageLogController.readValues(connection);
		editConnectionPublicationsController.readValues(connection);
		editConnectionSubscriptionsController.readValues(connection);
		editConnectionLastWillController.readValues(connection);			
		
		return connection;
	}
	
	private boolean readAndDetectChanges()
	{
		final UserInterfaceMqttConnectionDetails connection = readValues();
		boolean changed = !connection.equals(editedConnectionDetails.getSavedValues());
			
		logger.debug("Values read. Changed = " + changed);
		editedConnectionDetails.setModified(changed);
		editedConnectionDetails.setConnectionDetails(connection);
		
		return changed;
	}

	public void editConnection(final ConfiguredConnectionDetails connectionDetails)
	{	
		synchronized (this)
		{
			this.editedConnectionDetails = connectionDetails;
			
			// Set 'open connection' button mode
			openNewMode = true;
			existingConnection = null;
			connectButton.setText("Open connection");
			
			logger.debug("Editing connection id={} name={}", editedConnectionDetails.getId(),
					editedConnectionDetails.getName());
			for (final MqttAsyncConnection mqttConnection : connectionManager.getConnections())
			{
				if (connectionDetails.getId() == mqttConnection.getProperties().getConfiguredProperties().getId() && mqttConnection.isOpened())
				{
					openNewMode = false;
					existingConnection = mqttConnection;
					connectButton.setText("Close and re-open existing connection");
					break;
				}				
			}
			
			if (editedConnectionDetails.getName().equals(
					ConnectionUtils.composeConnectionName(editedConnectionDetails.getClientID(), editedConnectionDetails.getServerURI())))
			{
				lastGeneratedConnectionName = editedConnectionDetails.getName();
			}
			else
			{
				lastGeneratedConnectionName = "";
			}
			
			displayConnectionDetails(editedConnectionDetails);		
			editConnectionConnectivityController.updateClientIdLength();
			updateConnectionName();
						
			updateButtons();
		}
	}
	
	private void updateButtons()
	{
		if (editedConnectionDetails != null && editedConnectionDetails.isModified())
		{
			saveButton.setDisable(false);
			undoButton.setDisable(false);
		}
		else
		{
			saveButton.setDisable(true);
			undoButton.setDisable(true);
		}
	}
	
	private void displayConnectionDetails(final ConfiguredConnectionDetails connection)
	{
		ConfigurationUtils.populateConnectionDefaults(connection);
		
		connectionNameText.setText(connection.getName());
		protocolCombo.getSelectionModel().select(connection.getProtocol());
		
		editConnectionConnectivityController.displayConnectionDetails(connection);
		editConnectionSecurityController.displayConnectionDetails(connection);
		editConnectionMessageLogController.displayConnectionDetails(connection);
		editConnectionOtherController.displayConnectionDetails(connection);
		editConnectionPublicationsController.displayConnectionDetails(connection);
		editConnectionSubscriptionsController.displayConnectionDetails(connection);
		editConnectionLastWillController.displayConnectionDetails(connection);
		
		connection.setBeingCreated(false);
	}		

	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setMainController(MainController mainController)
	{
		this.mainController = mainController;
	}

	public void setConfigurationManager(final ConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}

	public void setEditConnectionsController(EditConnectionsController editConnectionsController)
	{
		this.editConnectionsController = editConnectionsController;		
	}
	
	public void setRecordModifications(boolean recordModifications)
	{
		if (!recordModifications)
		{
			logger.trace("Modifications suspended...");
			noModificationsLock++;
			this.recordModifications = recordModifications;
		}
		else
		{ 
			noModificationsLock--;
			// Only allow modifications once the parent caller removes the lock
			if (noModificationsLock == 0)
			{
				logger.trace("Modifications restored...");
				this.recordModifications = recordModifications;
			}
		}
	}
	
	public boolean isRecordModifications()	
	{
		return recordModifications;
	}
	
	public void setEmptyConnectionListMode(boolean emptyConnectionList)
	{
		this.emptyConnectionList = emptyConnectionList;
		connectButton.setDisable(emptyConnectionList);
		updateButtons();
	}

	public void setConnectionManager(final ConnectionManager connectionManager)
	{
		this.connectionManager = connectionManager;
	}

	public TextField getConnectionName()
	{
		return connectionNameText;
	}
	
	public ConfiguredConnectionDetails getEditedConnectionDetails()
	{
		return editedConnectionDetails;
	}

	/**
	 * @return the mainController
	 */
	public MainController getMainController()
	{
		return mainController;
	}
}
