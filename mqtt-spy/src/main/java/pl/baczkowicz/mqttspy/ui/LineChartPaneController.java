/***********************************************************************************
 * 
 * Copyright (c) 2013-2015 Jason Winnebeck, Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License Version 2.0 which 
 * accompany this distribution.
 *    
 * The Apache License Version 2.0 is available at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation; 
 *    					 partially derivative work created from the JFXUtils examples (https://github.com/gillius/jfxutils).
 *    
 */
package pl.baczkowicz.mqttspy.ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.StringConverter;

import javax.imageio.ImageIO;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;
import org.gillius.jfxutils.chart.StableTicksAxis;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.JsonPath;

import pl.baczkowicz.mqttspy.connectivity.MqttSubscription;
import pl.baczkowicz.mqttspy.ui.charts.ChartMode;
import pl.baczkowicz.mqttspy.ui.events.SaveChartSeriesEvent;
import pl.baczkowicz.mqttspy.ui.events.ShowEditChartSeriesWindowEvent;
import pl.baczkowicz.mqttspy.ui.utils.StylingUtils;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.exceptions.ExceptionUtils;
import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.ui.charts.ChartSeriesStatusEnum;
import pl.baczkowicz.spy.ui.charts.ChartSeriesTypeEnum;
import pl.baczkowicz.spy.ui.events.MessageAddedEvent;
import pl.baczkowicz.spy.ui.events.queuable.ui.BrowseReceivedMessageEvent;
import pl.baczkowicz.spy.ui.properties.ChartSeriesProperties;
import pl.baczkowicz.spy.ui.properties.MessageLimitProperties;
import pl.baczkowicz.spy.ui.properties.PublicationScriptProperties;
import pl.baczkowicz.spy.ui.properties.SubscriptionTopicSummaryProperties;
import pl.baczkowicz.spy.ui.storage.BasicMessageStoreWithSummary;
import pl.baczkowicz.spy.ui.utils.DialogFactory;
import pl.baczkowicz.spy.utils.TimeUtils;

/**
 * Controller for line chart pane.
 */
public class LineChartPaneController<T extends FormattedMessage> implements Initializable
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(LineChartPaneController.class);
	
	private static boolean lastAutoRefresh = true;
	
	private static boolean lastDisplaySymbols = true;
	
	private int lastSeriesId = 0;
	
	private static MessageLimitProperties lastMessageLimit 
		= new MessageLimitProperties("50 messages", 50, 0);

	@FXML
	private AnchorPane chartPane;
	
	@FXML
	private Label showRangeLabel;
	
	@FXML
	private ComboBox<MessageLimitProperties> showRangeBox;
	
	@FXML
	private Button refreshButton;
	
	@FXML
	private CheckBox autoRefreshCheckBox;
	
	@FXML
	private CheckMenuItem displaySymbolsCheckBox;
	
	@FXML
	private MenuButton optionsButton;
	
	@FXML
	private CheckMenuItem saveImageOnMessage;
	
	@FXML
	private CheckMenuItem saveImageAtInterval;
	
	@FXML
	private CheckMenuItem addTimestampOnExport;
	
	@FXML
	private Menu autoImageExport;
	
	@FXML
	private Button addSeriesButton;
	
	@FXML
	private Button removeSeriesButton;
	
	@FXML
	private TableView<ChartSeriesProperties> seriesTable;
	
	@FXML
	private TableColumn<ChartSeriesProperties, String> nameColumn;
	
	@FXML
	private TableColumn<ChartSeriesProperties, String> topicColumn;
	
	@FXML
	private TableColumn<ChartSeriesProperties, ChartSeriesTypeEnum> typeColumn;
	
	@FXML
	private TableColumn<ChartSeriesProperties, ChartSeriesStatusEnum> statusColumn;
	
	@FXML
	private TableColumn<ChartSeriesProperties, String> expressionColumn;	
	
	@FXML
	private TableColumn<ChartSeriesProperties, Boolean> visibleColumn;
	
	private BasicMessageStoreWithSummary<T> store;
	
	private IKBus eventBus;
	
	private MqttSubscription subscription;

	private Collection<String> topics;
	
	private Map<String, List<FormattedMessage>> chartData = new HashMap<>();
	
	private Map<String, Series<Number, Number>> topicToSeries = new LinkedHashMap<>();
	
	private LineChart<Number, Number> lineChart;
	
	private boolean warningShown;

	private String seriesTypeName;

	private ChartMode chartMode;

	private String seriesValueName;

	private String seriesUnit;

	private File selectedImageFile;

	private boolean saveOnMessage;

	private Integer exportInterval;

	private boolean addTimestampOnAutoExport;

	private Map<String, ChartSeriesProperties> seriesList = new HashMap<>();
	
	/**
	 * @param seriesValueName the seriesValueName to set
	 */
	public void setSeriesValueName(String seriesValueName)
	{
		this.seriesValueName = seriesValueName;
	}

	public void initialize(URL location, ResourceBundle resources)
	{
		autoRefreshCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				lastAutoRefresh = autoRefreshCheckBox.isSelected();
				// Only refresh when auto-refresh enabled
				if (lastAutoRefresh)
				{
					refresh();				
				}
			}
		});
		displaySymbolsCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				lastDisplaySymbols = displaySymbolsCheckBox.isSelected();
				refresh();
			}
		});
		showRangeBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				lastMessageLimit = showRangeBox.getValue();
				refresh();
			}
		});
		
		// Axis and chart        
		final NumberAxis xAxis = new NumberAxis();
		final StableTicksAxis yAxis = new StableTicksAxis();

        xAxis.setForceZeroInRange(false);
        xAxis.setTickLabelFormatter(new StringConverter<Number>()
		{
			@Override
			public String toString(Number object)
			{
				final Date date = new Date(object.longValue());
				return TimeUtils.TIME_SDF.format(date);
			}
			
			@Override
			public Number fromString(String string)
			{
				return null;
			}
		});
        yAxis.setForceZeroInRange(false);
		lineChart = new LineChart<>(xAxis, yAxis);
		
		// Set up table
		nameColumn.setCellValueFactory(new PropertyValueFactory<ChartSeriesProperties, String>("name"));
		nameColumn.setCellFactory(TextFieldTableCell.<ChartSeriesProperties>forTableColumn());
		nameColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ChartSeriesProperties, String>>()
		{
			@Override
			public void handle(CellEditEvent<ChartSeriesProperties, String> event)
			{
				final ChartSeriesProperties item = event.getRowValue();
				final String newValue = event.getNewValue();
				
				item.nameProperty().set(newValue);
				
				refresh();
			}		
		});
		
		topicColumn.setCellValueFactory(new PropertyValueFactory<ChartSeriesProperties, String>("topic"));
		topicColumn.setCellFactory(new Callback<TableColumn<ChartSeriesProperties, String>, TableCell<ChartSeriesProperties, String>>()
		{
			public TableCell<ChartSeriesProperties, String> call(
					TableColumn<ChartSeriesProperties, String> p)
			{
				final TableCell<ChartSeriesProperties, String> cell = new TableCell<ChartSeriesProperties, String>()
				{
					@Override
					public void updateItem(final String item, boolean empty)
					{
						super.updateItem(item, empty);	
						
						if (item != null)
						{
							this.setText(item.toString());
						}
						else
						{
							this.setText(null);
						}
					}
				};
				//cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});

		typeColumn.setCellValueFactory(new PropertyValueFactory<ChartSeriesProperties, ChartSeriesTypeEnum>("type"));
		typeColumn.setCellFactory(new Callback<TableColumn<ChartSeriesProperties, ChartSeriesTypeEnum>, TableCell<ChartSeriesProperties, ChartSeriesTypeEnum>>()
		{
			public TableCell<ChartSeriesProperties, ChartSeriesTypeEnum> call(
					TableColumn<ChartSeriesProperties, ChartSeriesTypeEnum> p)
			{
				final TableCell<ChartSeriesProperties, ChartSeriesTypeEnum> cell = new TableCell<ChartSeriesProperties, ChartSeriesTypeEnum>()
				{
					@Override
					public void updateItem(final ChartSeriesTypeEnum item, boolean empty)
					{
						super.updateItem(item, empty);			
						
						if (item != null)
						{
							this.setText(item.value());
						}
						else
						{
							this.setText(null);
						}
					}
				};
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});
		
		statusColumn.setCellValueFactory(new PropertyValueFactory<ChartSeriesProperties, ChartSeriesStatusEnum>("status"));
		statusColumn.setCellFactory(new Callback<TableColumn<ChartSeriesProperties, ChartSeriesStatusEnum>, TableCell<ChartSeriesProperties, ChartSeriesStatusEnum>>()
		{
			public TableCell<ChartSeriesProperties, ChartSeriesStatusEnum> call(
					TableColumn<ChartSeriesProperties, ChartSeriesStatusEnum> p)
			{
				final TableCell<ChartSeriesProperties, ChartSeriesStatusEnum> cell = new TableCell<ChartSeriesProperties, ChartSeriesStatusEnum>()
				{
					@Override
					public void updateItem(final ChartSeriesStatusEnum item, boolean empty)
					{
						super.updateItem(item, empty);			
						
						if (item != null)
						{
							this.setText(item.value());
						}
						else
						{
							this.setText(null);
						}
						
						final TableRow<ChartSeriesProperties> currentRow = getTableRow();

		                if (!isEmpty()) 
		                {
		                	currentRow.getStyleClass().clear();
		                	currentRow.setTooltip(null);
		                	
		                    if(ChartSeriesStatusEnum.NO_MESSAGES.equals(item))
		                    {
		                        currentRow.getStyleClass().add("seriesNoMessages");
		                    }
		                    else if(ChartSeriesStatusEnum.OK.equals(item))
		                    {
		                    	currentRow.getStyleClass().add("seriesOK");
		                    }
		                    else
		                    {
		                    	currentRow.getStyleClass().add("seriesError");
		                    	final ChartSeriesProperties properties = (ChartSeriesProperties) this.getTableRow().getItem();
		                    	currentRow.setTooltip(new Tooltip(properties.getErrorMessage()));
		                    }
		                }
		                
					}
				};
								
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});
		
		expressionColumn.setCellValueFactory(new PropertyValueFactory<ChartSeriesProperties, String>("valueExpression"));
		expressionColumn.setCellFactory(TextFieldTableCell.<ChartSeriesProperties>forTableColumn());
		expressionColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ChartSeriesProperties, String>>()
				{
					@Override
					public void handle(CellEditEvent<ChartSeriesProperties, String> event)
					{						
			            final String newValue = event.getNewValue();
			            			            
						// TODO	
					}		
				});
		
		visibleColumn.setCellValueFactory(new PropertyValueFactory<ChartSeriesProperties, Boolean>("visible"));
		visibleColumn.setCellFactory(new Callback<TableColumn<ChartSeriesProperties, Boolean>, TableCell<ChartSeriesProperties, Boolean>>()
		{
			public TableCell<ChartSeriesProperties, Boolean> call(
					TableColumn<ChartSeriesProperties, Boolean> param)
			{
				final CheckBoxTableCell<ChartSeriesProperties, Boolean> cell = new CheckBoxTableCell<ChartSeriesProperties, Boolean>()
				{
					@Override
					public void updateItem(final Boolean checked, boolean empty)
					{
						super.updateItem(checked, empty);
						if (!isEmpty() && checked != null && this.getTableRow() != null && this.getTableRow().getItem() != null && store != null)
						{
							final ChartSeriesProperties item = (ChartSeriesProperties) this.getTableRow().getItem();
							item.visibleProperty().set(checked);

							refresh();
						}									
					}
				};
				cell.setAlignment(Pos.TOP_CENTER);
				
				return cell;
			}
		});
		
		seriesTable.setRowFactory(new Callback<TableView<ChartSeriesProperties>, TableRow<ChartSeriesProperties>>()
		{
			public TableRow<ChartSeriesProperties> call(
					TableView<ChartSeriesProperties> tableView)
			{
				final TableRow<ChartSeriesProperties> row = new TableRow<ChartSeriesProperties>()					
				{
					@Override
					protected void updateItem(final ChartSeriesProperties item, final boolean empty)
					{
						super.updateItem(item, empty);
						if (!isEmpty() && item != null)
						{
							final MenuItem editMenu = new MenuItem("Edit..."); 
							editMenu.setOnAction(new EventHandler<ActionEvent>()
							{										
								@Override
								public void handle(ActionEvent event)
								{
									eventBus.publish(new ShowEditChartSeriesWindowEvent(chartPane.getScene().getWindow(), item));									
								}
							});

							this.setContextMenu(new ContextMenu(editMenu));
						}
					}
				};
				
				row.setOnMouseClicked(event -> 
				{
			        if (event.getClickCount() == 2 && !row.isEmpty()) 
			        {
			        	eventBus.publish(new ShowEditChartSeriesWindowEvent(chartPane.getScene().getWindow(), row.getItem()));
			        }
			    });

				return row;
			}
		});				
	}		

	public void init()
	{		
		showRangeBox.setCellFactory(new Callback<ListView<MessageLimitProperties>, ListCell<MessageLimitProperties>>()
		{
			@Override
			public ListCell<MessageLimitProperties> call(ListView<MessageLimitProperties> l)
			{
				return new ListCell<MessageLimitProperties>()
				{
					@Override
					protected void updateItem(MessageLimitProperties item, boolean empty)
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
		showRangeBox.setConverter(new StringConverter<MessageLimitProperties>()
		{
			@Override
			public String toString(MessageLimitProperties item)
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
			public MessageLimitProperties fromString(String id)
			{
				return null;
			}
		});
		showRangeBox.getItems().clear();
		showRangeBox.getItems().add(new MessageLimitProperties("All", 0, 0));
		showRangeBox.getItems().add(new MessageLimitProperties("10 messages", 10, 0));
		showRangeBox.getItems().add(new MessageLimitProperties("50 messages", 50, 0));
		showRangeBox.getItems().add(new MessageLimitProperties("100 messages", 100, 0));
		showRangeBox.getItems().add(new MessageLimitProperties("1k messages", 1000, 0));
		showRangeBox.getItems().add(new MessageLimitProperties("10k messages", 10000, 0));
		showRangeBox.getItems().add(new MessageLimitProperties("1 minute", 0, TimeUtils.ONE_MINUTE));
		showRangeBox.getItems().add(new MessageLimitProperties("5 minutes", 0, 5 * TimeUtils.ONE_MINUTE));
		showRangeBox.getItems().add(new MessageLimitProperties("30 minutes", 0, 30 * TimeUtils.ONE_MINUTE));
		showRangeBox.getItems().add(new MessageLimitProperties("1 hour", 0, TimeUtils.ONE_HOUR));
		showRangeBox.getItems().add(new MessageLimitProperties("24 hours", 0, TimeUtils.ONE_DAY));
		showRangeBox.getItems().add(new MessageLimitProperties("48 hours", 0, 2 * TimeUtils.ONE_DAY));
		showRangeBox.getItems().add(new MessageLimitProperties("1 week", 0, 7 * TimeUtils.ONE_DAY));
				
		if (subscription != null)
		{
			refreshButton.setStyle(StylingUtils.createBaseRGBString(subscription.getColor()));
		}
		
		chartPane.getChildren().add(lineChart);
		AnchorPane.setBottomAnchor(lineChart, 0.0);
		AnchorPane.setLeftAnchor(lineChart, 0.0);
		AnchorPane.setTopAnchor(lineChart, 45.0);
		AnchorPane.setRightAnchor(lineChart, 0.0);
						
		if (ChartMode.USER_DRIVEN_MSG_PAYLOAD.equals(chartMode) || ChartMode.USER_DRIVEN_MSG_SIZE.equals(chartMode))
		{
			// Selecting a value will perform a refresh
			for (final MessageLimitProperties limit : showRangeBox.getItems())
			{
				if (limit.getMessageLimit() == lastMessageLimit.getMessageLimit()
						&& limit.getTimeLimit() == lastMessageLimit.getTimeLimit())
				{
					showRangeBox.setValue(limit);
				}
			}
			
			autoRefreshCheckBox.setSelected(lastAutoRefresh);
			displaySymbolsCheckBox.setSelected(lastDisplaySymbols);
		}
//		else if (ChartMode.STATS.equals(chartMode))
//		{
//			showRangeBox.setVisible(false);
//			showRangeLabel.setVisible(false);
//		}
		
		eventBus.subscribeWithFilterOnly(this, this::onMessageAdded, MessageAddedEvent.class, store.getMessageList());
		// eventManager.registerMessageAddedObserver(this, store.getMessageList());
		
		setupPanAndZoom();
		
		eventBus.subscribe(this, this::onSeriesEdited, SaveChartSeriesEvent.class);
	}
	
	private void onSeriesEdited(final SaveChartSeriesEvent event)
	{
		if (event.isNew())
		{
			event.getEditedProperties().setId(lastSeriesId++);
			
			addNewSeries(event.getEditedProperties());
		}		
		
		warningShown = false;
		
		refresh();
	}
	
	/**
	 * Sets up pan and zoom. Derivative work created from the JFXUtils examples (https://github.com/gillius/jfxutils).
	 */
	private void setupPanAndZoom()
	{
		// Panning works via either secondary (right) mouse or primary with ctrl
		// held down
		ChartPanManager panner = new ChartPanManager(lineChart);
		panner.setMouseFilter(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent mouseEvent)
			{
				if (mouseEvent.getButton() == MouseButton.SECONDARY
						|| (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent
								.isShortcutDown()))
				{
					// let it through
				}
				else
				{
					mouseEvent.consume();
				}
			}
		});
		panner.start();
		// Zooming works only via primary mouse button without ctrl held down
		JFXChartUtil.setupZooming(lineChart, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent mouseEvent)
			{
				if (mouseEvent.getButton() != MouseButton.PRIMARY
						|| mouseEvent.isShortcutDown())
					mouseEvent.consume();
			}
		});
		
		// Set up reset on double click
		lineChart.setOnMouseClicked(new EventHandler<MouseEvent>()
		{

			@Override
			public void handle(MouseEvent event)
			{
				if (event.getClickCount() == 2)
				{
					reset();
				}
			}
		});
	}
	
	public void cleanup()
	{
		eventBus.unsubscribeConsumer(this, MessageAddedEvent.class);
		
		exportInterval = null;
	}
	
	private void divideMessagesByTopic(final Collection<String> topics)
	{
		chartData.clear();
		for (final FormattedMessage message : store.getMessages())
		{
			final String topic = message.getTopic();
			// logger.info("Topics = " + topics);
			if (topics.contains(topic))
			{				
				createTopicIfDoesNotExist(topic);
				chartData.get(topic).add(message);
			}
		}
	}
	
	private void createTopicIfDoesNotExist(final String topic)
	{
		if (chartData.get(topic) == null)
		{
			chartData.put(topic, new ArrayList<FormattedMessage>());
		}
	}
	
	/**
	 * Reset chart. Derivative work created from the JFXUtils examples (https://github.com/gillius/jfxutils).
	 */
	@FXML
	private void reset()
	{
		lineChart.getXAxis().setAutoRanging(true);
		lineChart.getYAxis().setAutoRanging(true);

		// Reset data to get ranging right
		final ObservableList<XYChart.Series<Number, Number>> data = lineChart.getData();
		lineChart.setData(FXCollections.<XYChart.Series<Number, Number>> emptyObservableList());
		lineChart.setData(data);
		lineChart.setData(FXCollections.<XYChart.Series<Number, Number>> emptyObservableList());
		lineChart.setData(data);
		lineChart.setAnimated(true);
	}
	
	@FXML
	private void removeSeries()
	{
		// TODO:
	}
	
	@FXML
	private void addSeries()
	{
		eventBus.publish(new ShowEditChartSeriesWindowEvent(chartPane.getScene().getWindow(), null));
	}
	
	private XYChart.Data<Number, Number> createDataObject(
			final ChartSeriesProperties seriesProperties, 
			final FormattedMessage message) throws XPathExpressionException, IOException
	{
		if (ChartSeriesTypeEnum.PAYLOAD_PLAIN.equals(seriesProperties.typeProperty().get()))			
		{
			final Double value = Double.valueOf(message.getFormattedPayload());
			return new XYChart.Data<Number, Number>(message.getDate().getTime(), value);	
		}
		else if (ChartSeriesTypeEnum.PAYLOAD_JSON.equals(seriesProperties.typeProperty().get()))			
		{			
			final String jsonValue = String.valueOf(JsonPath.read(message.getFormattedPayload(), seriesProperties.valueExpressionProperty().get())); 
			logger.debug("JSON path value = {}", jsonValue);
			
			final Double value = Double.valueOf(jsonValue);							
			return new XYChart.Data<Number, Number>(message.getDate().getTime(), value);	
		}
//		else if (ChartSeriesTypeEnum.PAYLOAD_XML.equals(seriesProperties.typeProperty().get()))
//		{
//			final XPath xpath = XPathFactory.newInstance().newXPath();
//			
//			final String xpathValue = String.valueOf(
//					xpath.evaluate(seriesProperties.valueExpressionProperty().get(), 
//							IOUtils.toInputStream(message.getFormattedPayload(), "UTF-8"), XPathConstants.NUMBER));
//			
//			logger.debug("XPath value = {}", xpathValue);
//			
//			final Double value = Double.valueOf(xpathValue);							
//			return new XYChart.Data<Number, Number>(message.getDate().getTime(), value);				
//		}
		else if (ChartSeriesTypeEnum.SIZE.equals(seriesProperties.typeProperty().get()))
		{
			final Integer value = Integer.valueOf(message.getPayload().length());
			return new XYChart.Data<Number, Number>(message.getDate().getTime(), value);	
		}
		else
		{
			// Nothing to do for now
		}
					
		return null;
	}
	
	private void addMessageToSeries(final Series<Number, Number> series, final FormattedMessage message)
	{
		final ChartSeriesProperties seriesProperties = seriesList.get(message.getTopic()); 
		
		try
    	{
    		series.getData().add(createDataObject(seriesProperties, message));
    		seriesProperties.statusProperty().set(ChartSeriesStatusEnum.OK);
    	}
    	catch (Exception e)
    	{
    		if (!warningShown && ChartMode.USER_DRIVEN_MSG_PAYLOAD.equals(chartMode))
    		{
    			seriesProperties.setErrorMessage(e.getLocalizedMessage());
    			seriesProperties.statusProperty().set(ChartSeriesStatusEnum.ERROR);
    			logger.error("Invalid payload", e);
    			warningShown = true;
//    			
//    			String payload = message.getFormattedPayload();
//    			if (payload.length() > 25)
//    			{
//    				payload = payload.substring(0, 25) + "...";
//    			}
//    			
//    			// TODO: replace with longer dialog
//    			DialogFactory.createWarningDialog(
//    					"Invalid content",
//    					"Payload \"" + payload 
//    					+ "\" on \"" + message.getTopic() 
//    					+ "\" cannot be converted to a number - ignoring all invalid values.");    			
    		}
    	}
	}
	
	@FXML
	private void exportAsImage()
	{
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select PNG file to save as...");
		
		selectedImageFile = fileChooser.showSaveDialog(lineChart.getScene().getWindow());

		if (selectedImageFile != null)
		{			
			exportAsImage(selectedImageFile);
		}		
	}

	private void exportAsImage(final File selectedFile)
	{
		final WritableImage image = lineChart.snapshot(new SnapshotParameters(), null);

	    try 
	    {
	    	if (addTimestampOnAutoExport)
	    	{
	    		final String newName = selectedFile.getAbsolutePath().replace(
	    				selectedFile.getName(), 
	    				TimeUtils.DATE_WITH_SECONDS_FILENAME_SDF.format(new Date()) + "_" + selectedFile.getName());
	    		
	    		final File withTimestamp = new File(newName);
	    		ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", withTimestamp);
	    	}
	    	else
	    	{
	    		ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", selectedFile);
	    	}
	        
	    	autoImageExport.setDisable(false);
	    } 
	    catch (IOException e) 
	    {
	        logger.error("Cannot export to file {}", selectedFile.getAbsoluteFile(), e);
	        DialogFactory.createErrorDialog("Cannot export to file", "Chart cannot be exported to file: " + e.getLocalizedMessage());
	    }
	}
	
	@FXML
	private void updateSaveOnMessage()
	{
		saveOnMessage = saveImageOnMessage.isSelected();
	}
	
	@FXML
	private void updateSaveOnInterval()
	{
		if (saveImageAtInterval.isSelected())
		{
			final TextInputDialog dialog = new TextInputDialog("60");
			dialog.setTitle("Export interval");
			dialog.setHeaderText(null);
			dialog.setContentText("Please enter export interval (in seconds):");
	
			final Optional<String> result = dialog.showAndWait();
			if (result.isPresent())
			{
				try
				{
					exportInterval = Integer.valueOf(result.get());
					
					if (exportInterval > 0)
					{
						new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								while (exportInterval != null && exportInterval > 0)
								{
									Platform.runLater(new Runnable()
									{										
										@Override
										public void run()
										{
											exportAsImage(selectedImageFile);											
										}
									});									
								
									try
									{
										Thread.sleep(exportInterval * 1000);
									}
									catch (InterruptedException e)
									{
										e.printStackTrace();
									}
								}
							}						
						}).start();
					}
//					else if (exportInterval == 0)
//					{
//						exportInterval = null;
//					}						
					else
					{
						DialogFactory.createErrorDialog("Invalid number format", "The provided value is not a correct number (>0).");
						exportInterval = null;
					}
				}
				catch (NumberFormatException e)
				{
					DialogFactory.createErrorDialog("Invalid number format", "The provided value is not a correct number (>0).");
				}
			}
		}
		else
		{
			exportInterval = null;
		}
	}
	
	@FXML
	private void addTimestampOnAutoExport()
	{
		addTimestampOnAutoExport = addTimestampOnExport.isSelected();
	}
	
	@FXML
	private void refresh()
	{		
		synchronized (chartData)
		{
			divideMessagesByTopic(topics);
			lineChart.getData().clear();
			lineChart.setCreateSymbols(lastDisplaySymbols);
			topicToSeries.clear();
			
			for (final ChartSeriesProperties seriesProperties : seriesList.values())
			{
				if (!seriesProperties.visibleProperty().get())
				{
					continue;
				}
					
				final List<FormattedMessage> extractedMessages = new ArrayList<>();
				final Series<Number, Number> series = new XYChart.Series<>();
				topicToSeries.put(seriesProperties.getTopic(), series);
		        series.setName(seriesProperties.getName());
		        
		        final MessageLimitProperties limit = showRangeBox.getValue();
		        final int itemsAvailable = chartData.get(seriesProperties.getTopic()).size();
		        
		        // Limit by number
		        int startIndex = 0;	        
		        if (limit.getMessageLimit() > 0 && limit.getMessageLimit() < itemsAvailable)
		        {
		        	startIndex = itemsAvailable - limit.getMessageLimit(); 
		        }
		        
		        // Limit by time
		        final Date now = new Date();
		        
		        for (int i = startIndex; i < itemsAvailable; i++)
		        {
		        	final FormattedMessage message = chartData.get(seriesProperties.getTopic()).get(chartData.get(seriesProperties.getTopic()).size() - i - 1);
		        	
		        	if (limit.getTimeLimit() > 0 && (message.getDate().getTime() + limit.getTimeLimit() < now.getTime()))
		        	{
		        		continue;
		        	}
		        	
		        	extractedMessages.add(message);
		        	addMessageToSeries(series, message);
		        }
		        // logger.info("Populated = {}=?{}/{}", chartData.get(topic).size(), topicToSeries.get(topic).getData().size(), limit.getMessageLimit());
				
		        // For further processing, take only messages put on chart
		        chartData.put(seriesProperties.getTopic(), extractedMessages);
		        lineChart.getData().add(series);
		        
		        populateTooltips(lineChart);
			}
		}
	}
	
	/**
	 * Populates the tooltip with data (chart-type independent).
	 * 
	 * @param series The series
	 * @param data The data
	 */
	private void populateTooltip(final Series<Number, Number> series, final Data<Number, Number> data)
	{
		final Date date = new Date();
		date.setTime(data.getXValue().longValue());
		
		final Tooltip tooltip = new Tooltip(
				seriesTypeName + " = " + series.getName()
				+ System.lineSeparator()
				+ seriesValueName + " = " + data.getYValue() + " " + seriesUnit
				+ System.lineSeparator()
				+ "Time = " + TimeUtils.TIME_SDF.format(date));
		
		Tooltip.install(data.getNode(), tooltip);
	}
	
	private void populateTooltips(final LineChart<Number, Number> lineChart)
	{
		for (final Series<Number, Number> series : lineChart.getData())
		{
			for (final Data<Number, Number> data : series.getData())
			{
				populateTooltip(series, data);
			}
		}
	}
	
	// TODO: optimise message handling
	public void onMessageAdded(final MessageAddedEvent<FormattedMessage> event)
	{
		for (final BrowseReceivedMessageEvent<FormattedMessage> message : event.getMessages())
		{
			onMessageAdded(message.getMessage());
		}
	}
	
	public void onMessageAdded(final FormattedMessage message)
	{
		// TODO: is that ever deregistered?
		synchronized (chartData)
		{	
			final String topic = message.getTopic();
			createTopicIfDoesNotExist(topic);		
			
			final MessageLimitProperties limit = showRangeBox.getValue();
			//logger.info("Message limit = {}", limit.getMessageLimit());
			//logger.info("Time limit = {}", limit.getTimeLimit());
			
			if (autoRefreshCheckBox.isSelected() && topics.contains(topic))
			{			
				// Apply message limit			
				while ((limit.getMessageLimit() > 0) && (chartData.get(topic).size() >= limit.getMessageLimit()))
				{
					//logger.info("Deleting = {}=?{}/{}", chartData.get(topic).size(), topicToSeries.get(topic).getData().size(), limit.getMessageLimit());
					chartData.get(topic).remove(0);
					
					if (topicToSeries.get(topic).getData().size() > 0)
					{
						topicToSeries.get(topic).getData().remove(0);
					}
				}
				
				// Apply time limit
				final Date now = new Date();
				if (limit.getTimeLimit() > 0)
				{
					FormattedMessage oldestMessage = chartData.get(topic).get(0);
					while (oldestMessage.getDate().getTime() + limit.getTimeLimit() < now.getTime())
					{
						chartData.get(topic).remove(0);
						topicToSeries.get(topic).getData().remove(0);
						
						if (chartData.get(topic).size() == 0)
						{
							break;
						}
						oldestMessage = chartData.get(topic).get(0);
					}				
				}
				
				// Add the new message
				chartData.get(topic).add(message);
				addMessageToSeries(topicToSeries.get(topic), message);
				//logger.info("Added = {}=?{}/{}", chartData.get(topic).size(), topicToSeries.get(topic).getData().size(), limit.getMessageLimit());
				
				if (topicToSeries.get(topic).getData().size() > 0)
				{
					populateTooltip(topicToSeries.get(topic), 
						topicToSeries.get(topic).getData().get(topicToSeries.get(topic).getData().size() - 1));
				}
				
				saveOnMessage();
			}
		}
	}	
	
	private void saveOnMessage()
	{
		if (saveOnMessage)
		{
			new Thread(new Runnable()
			{						
				@Override
				public void run()
				{
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					
					Platform.runLater(new Runnable()
					{						
						@Override
						public void run()
						{
							exportAsImage(selectedImageFile);						
						}
					});									
				}
			}).start();								
		}
	}
	
	public void setChartMode(final ChartMode mode)
	{
		this.chartMode = mode;
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================

	public void initialiseSeries(final Collection<String> topics)
	{
		this.topics = topics;
		
		for (final String topic : topics)
		{
			ChartSeriesProperties series;
			
			if (ChartMode.USER_DRIVEN_MSG_PAYLOAD.equals(chartMode))
			{
				series = new ChartSeriesProperties(lastSeriesId++, topic, topic, ChartSeriesTypeEnum.PAYLOAD_PLAIN, "");				
			}
			else
			{
				series = new ChartSeriesProperties(lastSeriesId++, topic, topic, ChartSeriesTypeEnum.SIZE, "");				
			}
			
			addNewSeries(series);			
		}		
	}
	
	private void addNewSeries(final ChartSeriesProperties series)
	{
		this.seriesTable.getItems().add(series);
		seriesList.put(series.getTopic(), series);
	}
	
	public void setSubscription(MqttSubscription subscription)
	{
		this.subscription = subscription;		
	}
	
	public void setStore(final BasicMessageStoreWithSummary<T> store)
	{
		this.store = store;		
	}	
	
	public void setSeriesTypeName(final String seriesTypeName)
	{
		this.seriesTypeName = seriesTypeName;
	}

	public void setSeriesUnit(String seriesUnit)
	{
		this.seriesUnit = seriesUnit;
	}
	
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}
}
