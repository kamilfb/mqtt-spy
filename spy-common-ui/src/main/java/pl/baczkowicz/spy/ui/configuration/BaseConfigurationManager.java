package pl.baczkowicz.spy.ui.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.common.generated.ConnectionGroup;
import pl.baczkowicz.spy.configuration.PropertyFileLoader;
import pl.baczkowicz.spy.exceptions.ConfigurationException;
import pl.baczkowicz.spy.utils.ThreadingUtils;
import pl.baczkowicz.spy.utils.TimeUtils;

public abstract class BaseConfigurationManager implements IConfigurationManager
{
	public static final String SPY_COMMON_SCHEMA = "/spy-common.xsd";

	private final static Logger logger = LoggerFactory.getLogger(BaseConfigurationManager.class);
	
	private List<ConfiguredConnectionGroupDetails> connectionGroups = new ArrayList<>();
	
	private ConfiguredConnectionGroupDetails rootGroup;
	
	private File loadedConfigurationFile;

	private Exception lastException;
	
	/** The application name. */
	public static String APPLICATION_NAME = "mqtt-spy";

	private PropertyFileLoader defaultPropertyFile;

	private PropertyFileLoader uiPropertyFile;
	
	protected void loadDefaultPropertyFile() throws ConfigurationException
	{
		// Load the default property file from classpath
		this.defaultPropertyFile = new PropertyFileLoader();
		this.defaultPropertyFile.readFromClassPath(getDefaultPropertyFileLocation());
	}
	
	protected void loadUiPropertyFile() throws ConfigurationException
	{
		// Load the UI property file
		if (!getUiPropertyFileObject().exists())
		{
			logger.info("Creating UI property file");
			createUiPropertyFileFromClassPath();
		}
		this.uiPropertyFile = new PropertyFileLoader();
		this.uiPropertyFile.readFromFileSystem(getUiPropertyFileObject());
	}
	
	public static String getDefaultHomeDirectory()
	{
		final String filePathSeparator = System.getProperty("file.separator");
		String userHomeDirectory = System.getProperty("user.home");
		
		if (!userHomeDirectory.endsWith(filePathSeparator))
		{
			userHomeDirectory = userHomeDirectory + filePathSeparator;
		}
		
		return userHomeDirectory + APPLICATION_NAME + filePathSeparator;
	}
	
	public static String getDefaultPropertyFileLocation()
	{
		return "/" + APPLICATION_NAME + ".properties";
	}
	
	public static String getUiPropertyFileLocation()
	{
		return "/" + APPLICATION_NAME + "-ui.properties";
	}
	
	public static String getDefaultConfigurationFileName()
	{			
		return APPLICATION_NAME + "-configuration.xml";
	}
	
	public static File getDefaultConfigurationFileObject()
	{			
		return new File(getDefaultHomeDirectory() + getDefaultConfigurationFileName());
	}
	
	/**
	 * Gets the default property file.
	 * 
	 * @return the defaultPropertyFile
	 */
	public PropertyFileLoader getDefaultPropertyFile()
	{
		return defaultPropertyFile;
	}

	/**
	 * Gets the UI property file.
	 * 
	 * @return the uiPropertyFile
	 */
	public PropertyFileLoader getUiPropertyFile()
	{
		return uiPropertyFile;
	}

	public static File getUiPropertyFileObject()
	{			
		return new File(getDefaultHomeDirectory() + getUiPropertyFileLocation());
	}
	
	
	public static boolean createUiPropertyFileFromClassPath()
	{
		final String origin = "/samples" + getUiPropertyFileLocation();
		try
		{			
			return copyFileFromClassPath(BaseConfigurationManager.class.getResourceAsStream(origin), getUiPropertyFileObject());
		}
		catch (IllegalArgumentException | IOException e)
		{
			// TODO: show warning dialog for invalid
			logger.error("Cannot copy file from {}", origin, e);
		}
		
		return false;
	} 
	
	public static File getDefaultHomeDirectoryFileObject()
	{			
		return new File(getDefaultHomeDirectory());
	}
	
	public static boolean copyFileFromClassPath(final InputStream orig, final File dest) throws IOException
	{
		getDefaultHomeDirectoryFileObject().mkdirs();
		streamToFile(orig, dest);

		return true;	
	}	
		
	public static void streamToFile (final InputStream input, final File output) throws IOException 
	{            
	    try (FileOutputStream out = new FileOutputStream(output)) 
	    {
	        IOUtils.copy(input, out);
	    }         
	}
	
	public String generateConnectionGroupId()
	{
		ThreadingUtils.sleep(1);
		return "cg" + TimeUtils.getMonotonicTime();
	}
	
	public static String generateConnectionId()
	{
		ThreadingUtils.sleep(1);
		return "conn" + TimeUtils.getMonotonicTime();		
	}
	
	public List<ConfiguredConnectionGroupDetails> getConnectionGrops()
	{
		return connectionGroups;
	}
	
	public ConfiguredConnectionGroupDetails getRootGroup()
	{
		return rootGroup;
	}
	
	public void setRootGroup(ConfiguredConnectionGroupDetails value)
	{
		this.rootGroup = value;
	}

	protected ConfiguredConnectionGroupDetails findMatchingGroup(final ConnectionGroup group)
	{
		for (final ConfiguredConnectionGroupDetails groupDetails : connectionGroups)
		{
			if (group.getID().equals(groupDetails.getID()))
			{
				return groupDetails;
			}
		}
		
		return null;
	}
	

	public Exception getLastException()
	{
		return lastException;
	}

	public void setLastException(Exception lastException)
	{
		this.lastException = lastException;
	}
	
	public File getLoadedConfigurationFile()
	{
		return loadedConfigurationFile;
	}
	
	public void setLoadedConfigurationFile(File file)
	{
		this.loadedConfigurationFile = file;		
	}

	
	public boolean isConfigurationWritable()
	{
		if (loadedConfigurationFile != null && loadedConfigurationFile.canWrite())
		{
			return true;
		}
		return false;
	}
	
	public boolean isConfigurationReadOnly()
	{
		if (loadedConfigurationFile != null && !loadedConfigurationFile.canWrite())
		{					
			return true;
		}
		
		return false;
	}
	
	
	public void updateUiProperty(final String propertyName, final String propertyValue)
	{
		getUiPropertyFile().setProperty(propertyName, propertyValue);
	}
}
