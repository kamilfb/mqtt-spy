package pl.baczkowicz.mqttspy.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.Main;
import pl.baczkowicz.mqttspy.ui.utils.MqttSpyPerspective;

public class UiProperties
{	
	public final static String WIDTH_PROPERTY = "application.width";
	
	public final static String HEIGHT_PROPERTY = "application.height";
	
	public final static String PERSPECTIVE_PROPERTY = "application.perspective";
	
	public final static String MESSAGE_PANE_RESIZE_PROPERTY = "application.panes.message.resize";

	public static final String MAXIMIZED_PROPERTY = "application.maximized";
	
	public static final String SUMMARYTABLE_PAYLOAD_MAX = "ui.summarytable.columns.payload.maxlength";
	
	public static final String BROWSER_LM_SIZE = "ui.messagebrowser.largemessage.size";
	
	public static final String BROWSER_LM_HIDE = "ui.messagebrowser.largemessage.hide";
	
	public static final String BROWSER_LM_SUBSTRING = "ui.messagebrowser.largemessage.substring";
	
	private final static Logger logger = LoggerFactory.getLogger(UiProperties.class);
	
	private static Integer summaryMaxPayloadLength;
	
	private static Integer largeMessageSize;
	
	private static Boolean largeMessageHide;
	
	private static Integer largeMessageSubstring;

	public static double getApplicationHeight(final ConfigurationManager configurationManager)
	{
		return ConfigurationUtils.getDoubleProperty(HEIGHT_PROPERTY, Main.DEFAULT_HEIGHT, configurationManager);		
	}
	
	public static boolean getApplicationMaximized(final ConfigurationManager configurationManager)
	{
		return ConfigurationUtils.getBooleanProperty(MAXIMIZED_PROPERTY, Boolean.FALSE, configurationManager);		
	}
	
	public static double getApplicationWidth(final ConfigurationManager configurationManager)
	{
		return ConfigurationUtils.getDoubleProperty(WIDTH_PROPERTY, Main.DEFAULT_WIDTH, configurationManager);
	}
	
	public static int getSummaryMaxPayloadLength(final ConfigurationManager configurationManager)
	{
		if (summaryMaxPayloadLength == null)
		{
			summaryMaxPayloadLength = ConfigurationUtils.getIntegerProperty(SUMMARYTABLE_PAYLOAD_MAX, 100, configurationManager);
		}
		
		return summaryMaxPayloadLength;
	}
	
	public static int getLargeMessageSize(final ConfigurationManager configurationManager)
	{
		if (largeMessageSize == null)
		{
			largeMessageSize = ConfigurationUtils.getIntegerProperty(BROWSER_LM_SIZE, 10000, configurationManager);
		}
		
		return largeMessageSize;
	}
	
	public static boolean getLargeMessageHide(final ConfigurationManager configurationManager)
	{
		if (largeMessageHide == null)
		{
			largeMessageHide = ConfigurationUtils.getBooleanProperty(BROWSER_LM_HIDE, Boolean.FALSE, configurationManager);
		}
		
		return largeMessageHide;
	}
	
	public static int getLargeMessageSubstring(final ConfigurationManager configurationManager)
	{
		if (largeMessageSubstring == null)
		{
			largeMessageSubstring = ConfigurationUtils.getIntegerProperty(BROWSER_LM_SUBSTRING, 1000, configurationManager);
		}
		
		return largeMessageSubstring;
	}	

	public static MqttSpyPerspective getApplicationPerspective(final ConfigurationManager configurationManager)
	{
		final String value = configurationManager.getUiPropertyFile().getProperty(PERSPECTIVE_PROPERTY);
		
		try
		{
			return MqttSpyPerspective.valueOf(value);
		}
		catch (IllegalArgumentException e)
		{
			logger.error("Invalid format " + value);
			return MqttSpyPerspective.DEFAULT;
		}
	}

	public static boolean getResizeMessagePane(final ConfigurationManager configurationManager)
	{
		return ConfigurationUtils.getBooleanProperty(MESSAGE_PANE_RESIZE_PROPERTY, Boolean.TRUE, configurationManager);
	}
}
