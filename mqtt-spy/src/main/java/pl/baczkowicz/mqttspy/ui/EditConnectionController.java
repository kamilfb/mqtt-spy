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
package pl.baczkowicz.mqttspy.ui;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.BaseMqttMessage;
import pl.baczkowicz.mqttspy.common.generated.PublicationDetails;
import pl.baczkowicz.mqttspy.common.generated.ReconnectionSettings;
import pl.baczkowicz.mqttspy.common.generated.ScriptDetails;
import pl.baczkowicz.mqttspy.common.generated.UserCredentials;
import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.ConfigurationUtils;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.ConversionMethod;
import pl.baczkowicz.mqttspy.configuration.generated.FormatterDetails;
import pl.baczkowicz.mqttspy.configuration.generated.TabbedSubscriptionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserAuthenticationOptions;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.exceptions.ConfigurationException;
import pl.baczkowicz.mqttspy.ui.connections.ConnectionManager;
import pl.baczkowicz.mqttspy.ui.properties.BackgroundScriptProperties;
import pl.baczkowicz.mqttspy.ui.properties.BaseTopicProperty;
import pl.baczkowicz.mqttspy.ui.properties.SubscriptionTopicProperties;
import pl.baczkowicz.mqttspy.ui.utils.ConnectivityUtils;
import pl.baczkowicz.mqttspy.ui.utils.DialogUtils;
import pl.baczkowicz.mqttspy.ui.utils.FormattingUtils;
import pl.baczkowicz.mqttspy.ui.utils.KeyboardUtils;
import pl.baczkowicz.mqttspy.utils.ConnectionUtils;
import pl.baczkowicz.mqttspy.utils.MqttUtils;

/**
 * Controller for editing a single connection.
 */
@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
public class EditConnectionController extends AnchorPane implements Initializable
{
	final static Logger logger = LoggerFactory.getLogger(EditConnectionController.class);

	// Connectivity
	
	@FXML
	private TextField brokerAddressText;
	
	@FXML
	private CheckBox reconnect;
		
	@FXML
	private TextField reconnectionInterval;
	
	@FXML
	private CheckBox resubscribe;

	@FXML
	private TextField clientIdText;

	@FXML
	private TextField connectionNameText;

	@FXML
	private Button addTimestampButton;
	
	@FXML
	private Label lengthLabel;
	
	@FXML
	private TextField connectionTimeout;
	
	@FXML
	private TextField keepAlive;
	
	@FXML
	private CheckBox cleanSession;

	// UI & Formatting
		
	@FXML
	private CheckBox autoOpen;
	
	@FXML
	private CheckBox autoConnect;
	
	@FXML
	private CheckBox autoSubscribe;
		
	@FXML
	private TextField maxMessagesStored;
	
	@FXML
	private TextField minMessagesPerTopicStored;
	
	@FXML
	private ComboBox<FormatterDetails> formatter;
	
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
	
	// Action buttons
	
	@FXML
	private Button connectButton;
	
	@FXML
	private Button cancelButton;
	
	@FXML
	private Button saveButton;
	
	@FXML
	private Button undoButton;
	
	@FXML
	private Button removePublicationButton;
	
	@FXML
	private Button removeSubscriptionButton;
	
	@FXML
	private Button removeScriptButton;
	
	
	// Pubs / subs
	
	@FXML
	private TextField publicationScriptsText;
	
	@FXML
	private TextField searchScriptsText;
	
	// Tables
	
	@FXML
	private TableView<BaseTopicProperty> publicationsTable;
	
	@FXML
	private TableView<SubscriptionTopicProperties> subscriptionsTable;
	
	@FXML
	private TableView<BackgroundScriptProperties> backgroundPublicationScriptsTable;
	
	@FXML
	private TableColumn<BaseTopicProperty, String> publicationTopicColumn;
	
	@FXML
	private TableColumn<SubscriptionTopicProperties, String> subscriptionTopicColumn;
	
	@FXML
	private TableColumn<SubscriptionTopicProperties, String> scriptColumn;
	
	@FXML
	private TableColumn<SubscriptionTopicProperties, Integer> qosSubscriptionColumn;
	
	@FXML
	private TableColumn<SubscriptionTopicProperties, Boolean> createTabSubscriptionColumn;
	
	// Background publication scripts
	@FXML
	private TableColumn<BackgroundScriptProperties, String> publicationScriptColumn;
	
	@FXML
	private TableColumn<BackgroundScriptProperties, Boolean> publicationAutoStartColumn;
	
	@FXML
	private TableColumn<BackgroundScriptProperties, Boolean> publicationRepeatColumn;
	
	// LWT
	
	@FXML
	private CheckBox lastWillAndTestament;
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private NewPublicationController lastWillAndTestamentMessageController;
	
	// Other fields

	private String lastGeneratedConnectionName = "";
	
	private MainController mainController;

	private ConfiguredConnectionDetails editedConnectionDetails;

	private boolean recordModifications;
    
	private ConfigurationManager configurationManager;

	private EditConnectionsController editConnectionsController;

	private final ChangeListener basicOnChangeListener = new ChangeListener()
	{
		@Override
		public void changed(ObservableValue observable, Object oldValue, Object newValue)
		{
			onChange();			
		}		
	};

	private boolean openNewMode;

	private MqttAsyncConnection existingConnection;

	private int noModificationsLock;

	private ConnectionManager connectionManager;

	private boolean emptyConnectionList;
	
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
		
		brokerAddressText.textProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				updateConnectionName();
				
				onChange();
			}		
		});
		
		clientIdText.textProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				updateConnectionName();
				
				onChange();
			}		
		});
		
		cleanSession.selectedProperty().addListener(basicOnChangeListener);
		
		connectionTimeout.textProperty().addListener(basicOnChangeListener);
		connectionTimeout.addEventFilter(KeyEvent.KEY_TYPED, KeyboardUtils.nonNumericKeyConsumer);
		keepAlive.textProperty().addListener(basicOnChangeListener);
		keepAlive.addEventFilter(KeyEvent.KEY_TYPED, KeyboardUtils.nonNumericKeyConsumer);
		
		reconnect.selectedProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				updateReconnection();
				
				onChange();
			}		
		});
		reconnectionInterval.textProperty().addListener(basicOnChangeListener);
		reconnectionInterval.addEventFilter(KeyEvent.KEY_TYPED, KeyboardUtils.nonNumericKeyConsumer);
		resubscribe.selectedProperty().addListener(basicOnChangeListener);
		
		
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
				
		// LWT
		lastWillAndTestament.selectedProperty().addListener(basicOnChangeListener);
		lastWillAndTestamentMessageController.getPublicationTopicText().valueProperty().addListener(basicOnChangeListener);
		lastWillAndTestamentMessageController.getPublicationData().textProperty().addListener(basicOnChangeListener);
		lastWillAndTestamentMessageController.getPublicationQosChoice().getSelectionModel().selectedIndexProperty().addListener(basicOnChangeListener);
		lastWillAndTestamentMessageController.getRetainedBox().selectedProperty().addListener(basicOnChangeListener);
		lastWillAndTestamentMessageController.hidePublishButton();
		
		// UI
		autoConnect.selectedProperty().addListener(basicOnChangeListener);
		autoOpen.selectedProperty().addListener(basicOnChangeListener);
		autoSubscribe.selectedProperty().addListener(basicOnChangeListener);
		
		maxMessagesStored.textProperty().addListener(basicOnChangeListener);
		maxMessagesStored.addEventFilter(KeyEvent.KEY_TYPED, KeyboardUtils.nonNumericKeyConsumer);
		
		minMessagesPerTopicStored.textProperty().addListener(basicOnChangeListener);
		minMessagesPerTopicStored.addEventFilter(KeyEvent.KEY_TYPED, KeyboardUtils.nonNumericKeyConsumer);
		
		formatter.getSelectionModel().selectedIndexProperty().addListener(basicOnChangeListener);
		formatter.setCellFactory(new Callback<ListView<FormatterDetails>, ListCell<FormatterDetails>>()
				{
					@Override
					public ListCell<FormatterDetails> call(ListView<FormatterDetails> l)
					{
						return new ListCell<FormatterDetails>()
						{
							@Override
							protected void updateItem(FormatterDetails item, boolean empty)
							{
								super.updateItem(item, empty);
								if (item == null || empty)
								{
									setText(null);
								}
								else
								{									
									setText(item.getName());
								}
							}
						};
					}
				});
		formatter.setConverter(new StringConverter<FormatterDetails>()
		{
			@Override
			public String toString(FormatterDetails item)
			{
				if (item == null)
				{
					return null;
				}
				else
				{
					return item.getName();
				}
			}

			@Override
			public FormatterDetails fromString(String id)
			{
				return null;
			}
		});
		
		// Publication topics
		publicationTopicColumn.setCellValueFactory(new PropertyValueFactory<BaseTopicProperty, String>("topic"));
		publicationTopicColumn.setCellFactory(TextFieldTableCell.<BaseTopicProperty>forTableColumn());
		publicationTopicColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<BaseTopicProperty, String>>()
		{
			@Override
			public void handle(CellEditEvent<BaseTopicProperty, String> event)
			{
				BaseTopicProperty p = event.getRowValue();
	            String newValue = event.getNewValue();
	            p.topicProperty().set(newValue);            
				logger.debug("New value = {}", publicationsTable.getSelectionModel().getSelectedItem().topicProperty().getValue());
				onChange();
			}		
		});
		
		// Publication scripts
		publicationScriptsText.textProperty().addListener(basicOnChangeListener);
		publicationAutoStartColumn.setCellValueFactory(new PropertyValueFactory<BackgroundScriptProperties, Boolean>("autoStart"));
		publicationAutoStartColumn.setCellFactory(new Callback<TableColumn<BackgroundScriptProperties, Boolean>, TableCell<BackgroundScriptProperties, Boolean>>()
				{
					public TableCell<BackgroundScriptProperties, Boolean> call(
							TableColumn<BackgroundScriptProperties, Boolean> p)
					{
						final TableCell<BackgroundScriptProperties, Boolean> cell = new TableCell<BackgroundScriptProperties, Boolean>()
						{
							@Override
							public void updateItem(final Boolean item, boolean empty)
							{
								super.updateItem(item, empty);
								if (!isEmpty())
								{
									final BackgroundScriptProperties shownItem = getTableView().getItems().get(getIndex());
									CheckBox box = new CheckBox();
									box.selectedProperty().bindBidirectional(shownItem.autoStartProperty());
									box.setOnAction(new EventHandler<ActionEvent>()
									{										
										@Override
										public void handle(ActionEvent event)
										{
											logger.info("New value = {} {}", 
													shownItem.scriptProperty().getValue(),
													shownItem.autoStartProperty().getValue());
											onChange();
										}
									});
									setGraphic(box);
								}
								else
								{
									setGraphic(null);
								}
							}
						};
						cell.setAlignment(Pos.CENTER);
						return cell;
					}
				});
		
		publicationRepeatColumn.setCellValueFactory(new PropertyValueFactory<BackgroundScriptProperties, Boolean>("repeat"));
		publicationRepeatColumn.setCellFactory(new Callback<TableColumn<BackgroundScriptProperties, Boolean>, TableCell<BackgroundScriptProperties, Boolean>>()
				{
					public TableCell<BackgroundScriptProperties, Boolean> call(
							TableColumn<BackgroundScriptProperties, Boolean> p)
					{
						final TableCell<BackgroundScriptProperties, Boolean> cell = new TableCell<BackgroundScriptProperties, Boolean>()
						{
							@Override
							public void updateItem(final Boolean item, boolean empty)
							{
								super.updateItem(item, empty);
								if (!isEmpty())
								{
									final BackgroundScriptProperties shownItem = getTableView().getItems().get(getIndex());
									CheckBox box = new CheckBox();
									box.selectedProperty().bindBidirectional(shownItem.repeatProperty());
									box.setOnAction(new EventHandler<ActionEvent>()
									{										
										@Override
										public void handle(ActionEvent event)
										{
											logger.info("New value = {} {}", 
													shownItem.scriptProperty().getValue(),
													shownItem.repeatProperty().getValue());
											onChange();
										}
									});
									setGraphic(box);
								}
								else
								{
									setGraphic(null);
								}
							}
						};
						cell.setAlignment(Pos.CENTER);
						return cell;
					}
				});
		
		publicationScriptColumn.setCellValueFactory(new PropertyValueFactory<BackgroundScriptProperties, String>("script"));
		publicationScriptColumn.setCellFactory(TextFieldTableCell.<BackgroundScriptProperties>forTableColumn());
		publicationScriptColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<BackgroundScriptProperties, String>>()
				{
					@Override
					public void handle(CellEditEvent<BackgroundScriptProperties, String> event)
					{
						BackgroundScriptProperties p = event.getRowValue();
			            String newValue = event.getNewValue();
			            p.scriptProperty().set(newValue);            
						logger.debug("New value = {}", backgroundPublicationScriptsTable.getSelectionModel().getSelectedItem().scriptProperty().getValue());
						onChange();
					}		
				});
		
		// Subscriptions
		searchScriptsText.textProperty().addListener(basicOnChangeListener);
		createTabSubscriptionColumn.setCellValueFactory(new PropertyValueFactory<SubscriptionTopicProperties, Boolean>("show"));
		createTabSubscriptionColumn.setCellFactory(new Callback<TableColumn<SubscriptionTopicProperties, Boolean>, TableCell<SubscriptionTopicProperties, Boolean>>()
				{
					public TableCell<SubscriptionTopicProperties, Boolean> call(
							TableColumn<SubscriptionTopicProperties, Boolean> p)
					{
						final TableCell<SubscriptionTopicProperties, Boolean> cell = new TableCell<SubscriptionTopicProperties, Boolean>()
						{
							@Override
							public void updateItem(final Boolean item, boolean empty)
							{
								super.updateItem(item, empty);
								if (!isEmpty())
								{
									final SubscriptionTopicProperties shownItem = getTableView().getItems().get(getIndex());
									CheckBox box = new CheckBox();
									box.selectedProperty().bindBidirectional(shownItem.showProperty());
									box.setOnAction(new EventHandler<ActionEvent>()
									{										
										@Override
										public void handle(ActionEvent event)
										{
											logger.info("New value = {} {}", 
													shownItem.topicProperty().getValue(),
													shownItem.showProperty().getValue());
											onChange();
										}
									});
									setGraphic(box);
								}
								else
								{
									setGraphic(null);
								}
							}
						};
						cell.setAlignment(Pos.CENTER);
						return cell;
					}
				});

		subscriptionTopicColumn.setCellValueFactory(new PropertyValueFactory<SubscriptionTopicProperties, String>("topic"));
		subscriptionTopicColumn.setCellFactory(TextFieldTableCell.<SubscriptionTopicProperties>forTableColumn());
		subscriptionTopicColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<SubscriptionTopicProperties, String>>()
				{
					@Override
					public void handle(CellEditEvent<SubscriptionTopicProperties, String> event)
					{
						BaseTopicProperty p = event.getRowValue();
			            String newValue = event.getNewValue();
			            p.topicProperty().set(newValue);            
						logger.debug("New value = {}", subscriptionsTable.getSelectionModel().getSelectedItem().topicProperty().getValue());
						onChange();
					}		
				});
		
		scriptColumn.setCellValueFactory(new PropertyValueFactory<SubscriptionTopicProperties, String>("script"));
		scriptColumn.setCellFactory(TextFieldTableCell.<SubscriptionTopicProperties>forTableColumn());
		scriptColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<SubscriptionTopicProperties, String>>()
				{
					@Override
					public void handle(CellEditEvent<SubscriptionTopicProperties, String> event)
					{
						SubscriptionTopicProperties p = event.getRowValue();
			            String newValue = event.getNewValue();
			            p.scriptProperty().set(newValue);            
						logger.debug("New value = {}", subscriptionsTable.getSelectionModel().getSelectedItem().scriptProperty().getValue());
						onChange();
					}		
				});
		
		final ObservableList<Integer> qosChoice = FXCollections.observableArrayList (
			    new Integer(0),
			    new Integer(1),
			    new Integer(2)
			);
		
		qosSubscriptionColumn.setCellValueFactory(new PropertyValueFactory<SubscriptionTopicProperties, Integer>("qos"));
		qosSubscriptionColumn.setCellFactory(new Callback<TableColumn<SubscriptionTopicProperties, Integer>, TableCell<SubscriptionTopicProperties, Integer>>()
				{
					public TableCell<SubscriptionTopicProperties, Integer> call(
							TableColumn<SubscriptionTopicProperties, Integer> p)
					{
						final TableCell<SubscriptionTopicProperties, Integer> cell = new TableCell<SubscriptionTopicProperties, Integer>()
						{
							@Override
							public void updateItem(final Integer item, boolean empty)
							{
								super.updateItem(item, empty);
								if (!isEmpty())
								{
									final SubscriptionTopicProperties shownItem = getTableView().getItems().get(getIndex());
									ChoiceBox box = new ChoiceBox();
									box.setItems(qosChoice);
									box.setId("subscriptionQosChoice");
									int qos = shownItem.qosProperty().getValue();
									box.getSelectionModel().select(qos);
									box.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>()
									{
										@Override
										public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue)
										{
											shownItem.qosProperty().setValue(newValue);
											logger.info("New value = {} {}", 
													shownItem.topicProperty().getValue(),
													shownItem.qosProperty().getValue());
											onChange();
										}
									});
									setGraphic(box);
								}
								else
								{
									setGraphic(null);
								}
							}
						};
						cell.setAlignment(Pos.CENTER);
						return cell;
					}
				});
		qosSubscriptionColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<SubscriptionTopicProperties, Integer>>()
				{
					@Override
					public void handle(CellEditEvent<SubscriptionTopicProperties, Integer> event)
					{
						SubscriptionTopicProperties p = event.getRowValue();
						Integer newValue = event.getNewValue();
			            p.qosProperty().set(newValue);            
						logger.debug("New value = {}", subscriptionsTable.getSelectionModel().getSelectedItem().qosProperty().getValue());
						onChange();
					}		
				});
	}

	public void init()
	{
		formatter.getItems().clear();		
		formatter.getItems().add(FormattingUtils.createBasicFormatter("default", 				"Plain", ConversionMethod.PLAIN));
		formatter.getItems().add(FormattingUtils.createBasicFormatter("default-hexDecoder", 	"HEX decoder", ConversionMethod.HEX_DECODE));
		formatter.getItems().add(FormattingUtils.createBasicFormatter("default-hexEncoder", 	"HEX encoder", ConversionMethod.HEX_ENCODE));
		formatter.getItems().add(FormattingUtils.createBasicFormatter("default-base64Decoder", 	"Base64 decoder", ConversionMethod.BASE_64_DECODE));
		formatter.getItems().add(FormattingUtils.createBasicFormatter("default-base64Encoder", 	"Base64 encoder", ConversionMethod.BASE_64_ENCODE));		

		// Populate those from the configuration file
		for (final FormatterDetails formatterDetails : configurationManager.getConfiguration().getFormatting().getFormatter())
		{			
			// Make sure the element we're trying to add is not on the list already
			boolean found = false;
			
			for (final FormatterDetails existingFormatterDetails : formatter.getItems())
			{
				if (existingFormatterDetails.getID().equals(formatterDetails.getID()))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				formatter.getItems().add(formatterDetails);
			}
		}	
	}

	// ===============================
	// === FXML ======================
	// ===============================

	@FXML
	private void addTimestamp()
	{
		updateClientId(true);
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
		
		logger.debug("Saving connection " + connectionNameText.getText());
		if (configurationManager.saveConfiguration())
		{
			DialogUtils.showTooltip(saveButton, "Changes for connection " + editedConnectionDetails.getName() + " have been saved.");
		}
	}	
	
	private boolean updateClientId(final boolean addTimestamp)
	{
		String clientId = clientIdText.getText();
		String newClientId = clientId;
		
		if (clientId.length() > MqttUtils.MAX_CLIENT_LENGTH)
		{
			newClientId = clientId.substring(0, MqttUtils.MAX_CLIENT_LENGTH);
		}
		
		if (addTimestamp)
		{
			newClientId = MqttUtils.generateClientIdWithTimestamp(newClientId);
		}
		
		if (!clientId.equals(newClientId))
		{
			final int currentCurrentPosition = clientIdText.getCaretPosition();
			clientIdText.setText(newClientId);
			clientIdText.positionCaret(currentCurrentPosition);
			return true;
		}
		
		return false;
	}

	private void updateClientIdLength()
	{					
		lengthLabel.setText("Length = " + clientIdText.getText().length() + "/" + MqttUtils.MAX_CLIENT_LENGTH);
	}
	
	@FXML
	private void addPublication()
	{
		final BaseTopicProperty item = new BaseTopicProperty("/samplePublication/");		
		publicationsTable.getItems().add(item);
		onChange();
	}
	
	@FXML
	private void addScript()
	{
		final BackgroundScriptProperties item = new BackgroundScriptProperties("put your script location here...", false, false);
		backgroundPublicationScriptsTable.getItems().add(item);
		onChange();
	}
	
	@FXML
	private void addSubscription()
	{
		final SubscriptionTopicProperties item = new SubscriptionTopicProperties("/sampleSubscription/", "", 0, false);		
		subscriptionsTable.getItems().add(item);
		onChange();
	}
	
	@FXML
	private void removePublication()
	{
		final BaseTopicProperty item = publicationsTable.getSelectionModel().getSelectedItem(); 
		if (item != null)
		{
			publicationsTable.getItems().remove(item);
			onChange();
		}
	}
	
	@FXML
	private void removeSubscription()
	{
		final SubscriptionTopicProperties item = subscriptionsTable.getSelectionModel().getSelectedItem(); 
		if (item != null)
		{
			subscriptionsTable.getItems().remove(item);
			onChange();
		}
	}
	
	@FXML
	private void removeScript()
	{
		final BackgroundScriptProperties item = backgroundPublicationScriptsTable.getSelectionModel().getSelectedItem();
		if (item != null)
		{
			backgroundPublicationScriptsTable.getItems().remove(item);
			onChange();
		}
	}

	private void updateConnectionName()
	{
		if (connectionNameText.getText().isEmpty()
				|| lastGeneratedConnectionName.equals(connectionNameText.getText()))
		{
			final String newName = ConnectionUtils.composeConnectionName(clientIdText.getText(), brokerAddressText.getText());
			connectionNameText.setText(newName);
			lastGeneratedConnectionName = newName;
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
			
			logger.info("Opening connection " + connectionNameText.getText());
	
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
						connectionManager.openConnection(editedConnectionDetails, mainController);
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

	private void onChange()
	{
		if (recordModifications && !emptyConnectionList)
		{					
			if (readAndDetectChanges())
			{
				updateButtons();
				updateClientId(false);
				updateClientIdLength();
				updateConnectionName();		
				updateReconnection();
				updateUserAuthentication();
				editConnectionsController.listConnections();
			}
		}				
	}

	private UserInterfaceMqttConnectionDetails readValues()
	{
		final UserInterfaceMqttConnectionDetails connection = new UserInterfaceMqttConnectionDetails();
		
		connection.setName(connectionNameText.getText());
		
		final List<String> serverURIs = Arrays.asList(brokerAddressText.getText().split(ConnectionUtils.SERVER_DELIMITER));
		for (final String serverURI : serverURIs)
		{
			logger.trace("Adding " + serverURI);
			connection.getServerURI().add(serverURI.trim());
		}
		if (brokerAddressText.getText().endsWith(ConnectionUtils.SERVER_DELIMITER))
		{
			logger.trace("Adding empty");
			connection.getServerURI().add("");
		}
		
		connection.setClientID(clientIdText.getText());
		
		connection.setCleanSession(cleanSession.isSelected());
		connection.setConnectionTimeout(Integer.valueOf(connectionTimeout.getText()));
		connection.setKeepAliveInterval(Integer.valueOf(keepAlive.getText()));
		
		if (reconnect.isSelected())
		{
			connection.setReconnectionSettings(
					new ReconnectionSettings(
							Integer.valueOf(reconnectionInterval.getText()), 
							resubscribe.isSelected()));
		}
		
		connection.setAutoConnect(autoConnect.isSelected());
		connection.setAutoOpen(autoOpen.isSelected());
		connection.setAutoSubscribe(autoSubscribe.isSelected());
		connection.setFormatter(formatter.getSelectionModel().getSelectedItem());
		connection.setMaxMessagesStored(Integer.valueOf(maxMessagesStored.getText()));
		connection.setMinMessagesStoredPerTopic(Integer.valueOf(minMessagesPerTopicStored.getText()));
		
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
		
		// Publications topics		
		for (final BaseTopicProperty publicationDetails : publicationsTable.getItems())
		{
			final PublicationDetails newPublicationDetails = new PublicationDetails();
			newPublicationDetails.setTopic(publicationDetails.topicProperty().getValue());
			connection.getPublication().add(newPublicationDetails);
		}
		
		// Publication scripts
		connection.setPublicationScripts(publicationScriptsText.getText());
		for (final BackgroundScriptProperties scriptDetails : backgroundPublicationScriptsTable.getItems())
		{
			final ScriptDetails newScriptDetails = new ScriptDetails();			
			newScriptDetails.setFile(scriptDetails.scriptProperty().getValue());
			newScriptDetails.setAutoStart(scriptDetails.autoStartProperty().getValue());
			newScriptDetails.setRepeat(scriptDetails.repeatProperty().getValue());
			connection.getBackgroundScript().add(newScriptDetails);
		}
		
		// Subscriptions
		connection.setSearchScripts(searchScriptsText.getText());
		for (final SubscriptionTopicProperties subscriptionDetails : subscriptionsTable.getItems())
		{
			final TabbedSubscriptionDetails newSubscriptionDetails = new TabbedSubscriptionDetails();
			newSubscriptionDetails.setTopic(subscriptionDetails.topicProperty().getValue());
			newSubscriptionDetails.setScriptFile(subscriptionDetails.scriptProperty().getValue());
			newSubscriptionDetails.setCreateTab(subscriptionDetails.showProperty().getValue());
			newSubscriptionDetails.setQos(subscriptionDetails.qosProperty().getValue());
			connection.getSubscription().add(newSubscriptionDetails);
		}		
		
		if (lastWillAndTestament.isSelected())
		{			
			final BaseMqttMessage message = lastWillAndTestamentMessageController.readMessage(false);
			if (message != null)
			{
				connection.setLastWillAndTestament(message);
			}
		}		
		
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
			updateClientIdLength();
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

	private void updateUserAuthentication()
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
	
	private void updateReconnection()
	{
		if (reconnect.isSelected())
		{
			reconnectionInterval.setDisable(false);
			if (reconnectionInterval.getText().length() == 0)
			{
				reconnectionInterval.setText(String.valueOf(ConfigurationUtils.DEFAULT_RECONNECTION_INTERVAL));
			}
			resubscribe.setDisable(false);
		}
		else
		{
			reconnectionInterval.setDisable(true);
			resubscribe.setDisable(true);
		}
	}
	
	private void displayConnectionDetails(final ConfiguredConnectionDetails connection)
	{
		ConfigurationUtils.populateConnectionDefaults(connection);
		
		// Connectivity
		connectionNameText.setText(connection.getName());			
		
		brokerAddressText.setText(ConnectionUtils.serverURIsToString(connection.getServerURI()));
		clientIdText.setText(connection.getClientID());
				
		connectionTimeout.setText(connection.getConnectionTimeout().toString());
		keepAlive.setText(connection.getKeepAliveInterval().toString());
		cleanSession.setSelected(connection.isCleanSession());
		
		reconnect.setSelected(connection.getReconnectionSettings() != null);
		if (connection.getReconnectionSettings() != null)
		{
			reconnect.setSelected(true);
			reconnectionInterval.setText(String.valueOf(connection.getReconnectionSettings().getRetryInterval()));
			resubscribe.setSelected(connection.getReconnectionSettings().isResubscribe());
		}
		else
		{
			reconnect.setSelected(false);
			reconnectionInterval.setText("");
			resubscribe.setSelected(false);
		}
		
		updateReconnection();

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

		// UI
		autoConnect.setSelected(connection.isAutoConnect() == null ? false : connection.isAutoConnect());
		autoOpen.setSelected(connection.isAutoOpen() == null ? false : connection.isAutoOpen());
		autoSubscribe.setSelected(connection.isAutoSubscribe() == null ? false : connection.isAutoSubscribe());
		maxMessagesStored.setText(connection.getMaxMessagesStored().toString());
		minMessagesPerTopicStored.setText(connection.getMinMessagesStoredPerTopic().toString());
				
		if (formatter.getItems().size() > 0 && connection.getFormatter() != null)
		{
			for (final FormatterDetails item : formatter.getItems())
			{
				if (item.getID().equals(((FormatterDetails) connection.getFormatter()).getID()))
				{
					formatter.getSelectionModel().select(item);
					break;
				}
			}
		}	
		else
		{
			formatter.getSelectionModel().clearSelection();
		}
		
		// Publications topics
		removePublicationButton.setDisable(true);
		publicationsTable.getItems().clear();
		lastWillAndTestamentMessageController.clearTopics();
		for (final PublicationDetails pub : connection.getPublication())
		{
			publicationsTable.getItems().add(new BaseTopicProperty(pub.getTopic()));
			lastWillAndTestamentMessageController.recordPublicationTopic(pub.getTopic());
		}
		publicationsTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				removePublicationButton.setDisable(false);
			}		
		});
		
		// Publication scripts
		publicationScriptsText.setText(connection.getPublicationScripts());	
		removeScriptButton.setDisable(true);
		
		backgroundPublicationScriptsTable.getItems().clear();
		for (final ScriptDetails script : connection.getBackgroundScript())
		{
			backgroundPublicationScriptsTable.getItems().add(new BackgroundScriptProperties(script.getFile(), script.isAutoStart(), script.isRepeat()));
		}
		backgroundPublicationScriptsTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				removeScriptButton.setDisable(false);
			}		
		});
				
		// Subscriptions
		searchScriptsText.setText(connection.getSearchScripts());
		removeSubscriptionButton.setDisable(true);
		subscriptionsTable.getItems().clear();
		for (final TabbedSubscriptionDetails sub : connection.getSubscription())
		{
			subscriptionsTable.getItems().add(new SubscriptionTopicProperties(
					sub.getTopic(), 
					sub.getScriptFile() == null ? "" : sub.getScriptFile(), 
					sub.getQos(), sub.isCreateTab()));
		}
		subscriptionsTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				removeSubscriptionButton.setDisable(false);
			}		
		});
		
		// LWT
		lastWillAndTestament.setSelected(connection.getLastWillAndTestament() != null);
		lastWillAndTestamentMessageController.displayMessage(connection.getLastWillAndTestament());				
		
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
		
	public TextField getConnectionName()
	{
		return connectionNameText;
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
}
