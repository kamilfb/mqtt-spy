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
package pl.baczkowicz.mqttspy.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.generated.Connectivity;
import pl.baczkowicz.mqttspy.configuration.generated.MqttSpyConfiguration;
import pl.baczkowicz.mqttspy.configuration.generated.TabbedSubscriptionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttSubscription;
import pl.baczkowicz.spy.common.generated.ConnectionGroup;
import pl.baczkowicz.spy.common.generated.ConnectionGroupReference;
import pl.baczkowicz.spy.common.generated.ConnectionReference;
import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.common.generated.Formatting;
import pl.baczkowicz.spy.configuration.BaseConfigurationUtils;
import pl.baczkowicz.spy.connectivity.BaseSubscription;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.ui.configuration.BaseConfigurationManager;
import pl.baczkowicz.spy.ui.configuration.ConfiguredConnectionGroupDetails;
import pl.baczkowicz.spy.ui.configuration.UiProperties;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;
import pl.baczkowicz.spy.ui.utils.DialogFactory;
import pl.baczkowicz.spy.xml.XMLParser;

/**
 * Manages loading and saving configuration files.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class MqttConfigurationManager extends BaseConfigurationManager
{
	private final static Logger logger = LoggerFactory.getLogger(MqttConfigurationManager.class);
	
	public static final String PACKAGE = "pl.baczkowicz.mqttspy.configuration.generated";
	
	public static final String SCHEMA = "/mqtt-spy-configuration.xsd";
		
	public static final String MQTT_COMMON_SCHEMA = "/mqtt-spy-common.xsd";

	private MqttSpyConfiguration configuration;
	
	private List<ConfiguredMqttConnectionDetails> connections = new ArrayList<>();	
	
	private final XMLParser parser;
	
	public MqttConfigurationManager() throws XMLException
	{
		loadDefaultPropertyFile();
		loadUiPropertyFile();		
		
		this.parser = new XMLParser(PACKAGE, new String[] {SPY_COMMON_SCHEMA, MQTT_COMMON_SCHEMA, SCHEMA});
					
		// Create empty configuration
		this.configuration = new MqttSpyConfiguration();
		this.configuration.setConnectivity(new Connectivity());
		this.configuration.setFormatting(new Formatting());		
	}
	
	public boolean loadConfiguration(final File file)
	{
		try
		{
			clear();
			configuration = (MqttSpyConfiguration) parser.loadFromFile(file);
			
			initialiseConfiguration();
			
			setLoadedConfigurationFile(file);
			return true;
		}
		catch (XMLException e)
		{
			setLastException(e);
			DialogFactory.createErrorDialog("Invalid configuration file", "Cannot process the given configuration file. See the log file for more details.");					
			logger.error("Cannot process the configuration file at " + file.getAbsolutePath(), e);
			// eventManager.notifyConfigurationFileReadFailure();
		}
		catch (FileNotFoundException e)
		{
			setLastException(e);
			DialogFactory.createErrorDialog("Invalid configuration file", "Cannot read the given configuration file. See the log file for more details.");
			logger.error("Cannot read the configuration file from " + file.getAbsolutePath(), e);
			// eventManager.notifyConfigurationFileReadFailure();
		}
		
		return false;
	}
	
	public void initialiseConfiguration()
	{
		createConnections();
		createConnectionGroups();
		createConfigurationDefaults();
	}
	
	private void createConfigurationDefaults()
	{
		if (configuration.getFormatting() == null)
		{
			configuration.setFormatting(new Formatting());
		}
	}
	
	private void createConnections()
	{
		for (final Object connectionDetails : getConfiguration().getConnectivity().getConnectionV2())
		{
			ConfiguredMqttConnectionDetails configuredConnectionDetails = null;
			
//			if (connectionDetails instanceof UserInterfaceMqttConnectionDetailsV010)
//			{			
//				final UserInterfaceMqttConnectionDetailsV010 connectionDetailsV010 = (UserInterfaceMqttConnectionDetailsV010) connectionDetails;
//				
//				final UserInterfaceMqttConnectionDetails details = new UserInterfaceMqttConnectionDetails();
//				details.setName(connectionDetailsV010.getName());
//				details.getServerURI().add(connectionDetailsV010.getServerURI());
//				details.setClientID(connectionDetailsV010.getClientID());
//				details.setUserCredentials(connectionDetailsV010.getUserAuthentication());
//				if (connectionDetailsV010.getUserAuthentication() != null)
//				{
//					details.setUserAuthentication(new UserAuthenticationOptions(
//							connectionDetailsV010.getUserAuthentication().isAskForUsername(), 
//							connectionDetailsV010.getUserAuthentication().isAskForPassword()));
//				}
//				
//				if (connectionDetailsV010.getLastWillAndTestament() != null)
//				{
//					details.setLastWillAndTestament(new SimpleMqttMessage(
//							connectionDetailsV010.getLastWillAndTestament().getPayload(), 
//							connectionDetailsV010.getLastWillAndTestament().getTopic(), 
//							connectionDetailsV010.getLastWillAndTestament().getQoS(), 
//							connectionDetailsV010.getLastWillAndTestament().isRetained()));
//				}
//				details.setCleanSession(connectionDetailsV010.isCleanSession());
//				details.setConnectionTimeout(connectionDetailsV010.getConnectionTimeout());
//				details.setKeepAliveInterval(connectionDetailsV010.getKeepAliveInterval());
//				
//				details.setAutoOpen(connectionDetailsV010.isAutoOpen());
//				details.setAutoConnect(connectionDetailsV010.isAutoConnect());
//				details.setFormatter(connectionDetailsV010.getFormatter());
//				details.setMinMessagesStoredPerTopic(connectionDetailsV010.getMinMessagesStoredPerTopic());
//				details.setMaxMessagesStored(connectionDetailsV010.getMaxMessagesStored());
//				details.setPublicationScripts(connectionDetailsV010.getPublicationScripts());
//				details.getPublication().addAll(connectionDetailsV010.getPublication());
//				details.getSubscription().addAll(connectionDetailsV010.getSubscription());
//				
//				// Put the defaults at the point of loading the config, so we don't need to do it again
//				ConfigurationUtils.populateConnectionDefaults(details);
//				configuredConnectionDetails = new ConfiguredMqttConnectionDetails(false, false, details);
//			}
//			else 
			if (connectionDetails instanceof UserInterfaceMqttConnectionDetails)
			{
				// Put the defaults at the point of loading the config, so we don't need to do it again
				ConfigurationUtils.populateConnectionDefaults((UserInterfaceMqttConnectionDetails) connectionDetails);
				configuredConnectionDetails = new ConfiguredMqttConnectionDetails(false, false, 
						(UserInterfaceMqttConnectionDetails) connectionDetails);
			}
			
			connections.add(configuredConnectionDetails);
			
			// Populate the connection ID for referencing in XML
			if (configuredConnectionDetails.getID() == null)
			{
				configuredConnectionDetails.setID(generateConnectionId());				
			}
		}		
	}
	
	public boolean saveConfiguration()
	{
		if (isConfigurationWritable())
		{
			try
			{
				configuration.getConnectivity().getConnectionV2().clear();
				configuration.getConnectivity().getConnectionV2().addAll(connections);
				
				configuration.getConnectionGroups().clear();
				configuration.getConnectionGroups().addAll(getConnectionGrops());
				
				populateMissingFormatters(configuration.getFormatting().getFormatter(), connections);
//				for (final ConnectionGroup group : connectionGroups)
//				{
//					if (group.getGroup() != null && group.getGroup().getReference() == null)
//					{
//						group.setGroup(null);
//					}
//				}
				
				parser.saveToFile(getLoadedConfigurationFile(), 
						new JAXBElement(new QName("http://baczkowicz.pl/mqtt-spy-configuration", "MqttSpyConfiguration"), MqttSpyConfiguration.class, configuration));
				return true;
			}
			catch (XMLException e)
			{
				setLastException(e);
				logger.error("Cannot save the configuration file", e);
			}
		}
		
		return false;
	}
	
	private void populateMissingFormatters(final List<FormatterDetails> formatters, final List<ConfiguredMqttConnectionDetails> connections)
	{
		for (final ConfiguredMqttConnectionDetails connection : connections)
		{
			if (connection.getFormatter() == null)
			{
				continue;
			}
			
			boolean formatterFound = false;
			
			for (final FormatterDetails formatter : formatters)
			{
				if (((FormatterDetails) connection.getFormatter()).getID().equals(formatter.getID()))
				{
					formatterFound = true;
				}
			}
			
			if (!formatterFound)
			{
				formatters.add((FormatterDetails) connection.getFormatter());
			}
		}
	}
	
	public void clear()
	{
		connections.clear();
		configuration = null;
		setLoadedConfigurationFile(null);
		setLastException(null);
	}
	
	public ConfiguredMqttConnectionDetails getMatchingConnection(final String id)
	{
		for (final ConfiguredMqttConnectionDetails details : getConnections())
		{
			if (id.equals(details.getID()))
			{
				return details;
			}
		}
		
		return null;
	}
	
	public void updateSubscriptionConfiguration(final String connectionId, final BaseSubscription subscription)	
	{
		final ConfiguredMqttConnectionDetails details = getMatchingConnection(connectionId);
		
		boolean matchFound = false;
		for (final TabbedSubscriptionDetails subscriptionDetails : details.getSubscription())
		{							
			if (subscriptionDetails.getTopic().equals(subscription.getTopic()))
			{
				if (subscription instanceof MqttSubscription)
				{
					subscriptionDetails.setQos(((MqttSubscription) subscription).getQos());
				}
				subscriptionDetails.setCreateTab(true);
				subscriptionDetails.setScriptFile(subscription.getDetails().getScriptFile());
				matchFound = true;
				break;
			}
		}
		
		// If no match found, add this subscription
		if (!matchFound)
		{
			final TabbedSubscriptionDetails subscriptionDetails = new TabbedSubscriptionDetails();
			subscriptionDetails.setTopic(subscription.getTopic());
			if (subscription instanceof MqttSubscription)
			{
				subscriptionDetails.setQos(((MqttSubscription) subscription).getQos());
			}
			subscriptionDetails.setCreateTab(true);
			subscriptionDetails.setScriptFile(subscription.getDetails().getScriptFile());
			details.getSubscription().add(subscriptionDetails);							
		}					
		
		saveConfiguration();
	}
	
	public void deleteSubscriptionConfiguration(final String connectionId, final BaseSubscription subscription)	
	{
		final ConfiguredMqttConnectionDetails details = getMatchingConnection(connectionId);
		
		TabbedSubscriptionDetails itemToRemove = null;
		
		for (final TabbedSubscriptionDetails subscriptionDetails : details.getSubscription())
		{							
			if (subscriptionDetails.getTopic().equals(subscription.getTopic()))
			{
				itemToRemove = subscriptionDetails;
				break;
			}
		}
		
		if (itemToRemove != null)
		{
			details.getSubscription().remove(itemToRemove);
		}		
		
		saveConfiguration();
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public MqttSpyConfiguration getConfiguration()
	{
		return configuration;
	}
	
	public List<ConfiguredMqttConnectionDetails> getConnections()
	{
		return connections;
	}
	
	public List<ConfiguredMqttConnectionDetails> getConnections(final ConfiguredConnectionGroupDetails group)
	{
		List<ConfiguredMqttConnectionDetails> orderedConnections = new ArrayList<>();
		for (final ConnectionReference connetionRef : group.getConnections())
		{
			orderedConnections.add((ConfiguredMqttConnectionDetails) connetionRef.getReference());
		}
		return orderedConnections;
	}
	
	public List<ConfiguredMqttConnectionDetails> getOrderedConnections()
	{
		List<ConfiguredMqttConnectionDetails> orderedConnections = new ArrayList<>();		
		List<ConfiguredConnectionGroupDetails> orderedGroups = new ArrayList<>();
		
		sortConnections(getRootGroup(), orderedGroups, orderedConnections);
		
		return orderedConnections;
	}
	
	public List<ConfiguredConnectionGroupDetails> getOrderedGroups()
	{
		List<ConfiguredMqttConnectionDetails> orderedConnections = new ArrayList<>();		
		List<ConfiguredConnectionGroupDetails> orderedGroups = new ArrayList<>();
		
		orderedGroups.add(getRootGroup());
		sortConnections(getRootGroup(), orderedGroups, orderedConnections);
		
		return orderedGroups;
	}
	
	private void sortConnections(final ConfiguredConnectionGroupDetails parentGroup, 
			final List<ConfiguredConnectionGroupDetails> orderedGroups, List<ConfiguredMqttConnectionDetails> orderedConnections)
	{		
		for (final ConnectionGroupReference reference : parentGroup.getSubgroups())		
		{
			final ConfiguredConnectionGroupDetails group = (ConfiguredConnectionGroupDetails) reference.getReference();						
			orderedGroups.add(group);
			
			// Recursive
			sortConnections(group, orderedGroups, orderedConnections);
		}
		
		for (final ConnectionReference reference : parentGroup.getConnections())			
		{
			final ConfiguredMqttConnectionDetails connection = (ConfiguredMqttConnectionDetails) reference.getReference();
			orderedConnections.add(connection);
		}				
	}

	public void saveUiProperties(final double width, final double height, boolean maximized, 
			final SpyPerspective selectedPerspective, final boolean resizeMessagePane)
	{
		updateUiProperty(UiProperties.WIDTH_PROPERTY, String.valueOf(width));
		updateUiProperty(UiProperties.HEIGHT_PROPERTY, String.valueOf(height));
		updateUiProperty(UiProperties.MAXIMIZED_PROPERTY, String.valueOf(maximized));
		updateUiProperty(UiProperties.PERSPECTIVE_PROPERTY, selectedPerspective.toString());
		updateUiProperty(UiProperties.MESSAGE_PANE_RESIZE_PROPERTY, String.valueOf(resizeMessagePane));
		
		// Other properties are read-only from file
		
		try
		{
			getUiPropertyFile().saveToFileSystem("mqtt-spy-ui", getUiPropertyFileObject());
		}
		catch (IOException e)
		{
			logger.error("Cannot save UI properties", e);
		}
	}

	public void createConnectionGroups()
	{						
		final List<ConnectionGroup> groupsWithoutParent = new ArrayList<>(configuration.getConnectionGroups());
		
		// Clear up resources - in case something was loaded before
		getConnectionGrops().clear();
		setRootGroup(null);
		
		// This is expected from v0.3.0
		for (final ConnectionGroup group : configuration.getConnectionGroups())
		{			
			final ConfiguredConnectionGroupDetails details = new ConfiguredConnectionGroupDetails(group, false);
			
			for (ConnectionGroupReference subgroup : group.getSubgroups())
			{
				groupsWithoutParent.remove(subgroup.getReference());
			}
			
			getConnectionGrops().add(details);						
		}
		
		// Create the root if no groups present (pre v0.3.0)
		if (getConnectionGrops().isEmpty() || groupsWithoutParent.isEmpty())
		{
			logger.debug("Creating root group called 'All connections'");
			setRootGroup(new ConfiguredConnectionGroupDetails(new ConnectionGroup(
					BaseConfigurationUtils.DEFAULT_GROUP, "All connections", new ArrayList(), new ArrayList()), false));
			
			getConnectionGrops().add(getRootGroup());
			
			// Assign all connections to the new root
			for (final ConfiguredMqttConnectionDetails connection : getConnections())
			{
				connection.setGroup(new ConnectionGroupReference(getRootGroup()));
				getRootGroup().getConnections().add(new ConnectionReference(connection));
			}
			
			getRootGroup().apply();
		}
		else
		{
			// Find the root group
			final String rootId = groupsWithoutParent.get(0).getID();
			for (final ConfiguredConnectionGroupDetails group : getConnectionGrops())
			{
				if (group.getID().equals(rootId))
				{
					setRootGroup(group);
					break;
				}
			}
			// At this point, new groups link to old connection and group objects, and old connection objects to old groups
			
			// Re-wire all connections
			updateTree(getRootGroup());
		}
	}

	private ConfiguredMqttConnectionDetails findMatchingConnection(final UserInterfaceMqttConnectionDetails connection)
	{
		for (final ConfiguredMqttConnectionDetails connectionDetails : connections)
		{
			if (connection.getID().equals(connectionDetails.getID()))
			{
				return connectionDetails;
			}
		}
		
		return null;
	}
	
	private void updateTree(final ConfiguredConnectionGroupDetails parentGroup)
	{
		final List<ConnectionGroupReference> subgroups = new ArrayList<>(parentGroup.getSubgroups());
		parentGroup.getSubgroups().clear();
		
		for (final ConnectionGroupReference reference : subgroups)			
		{
			final ConnectionGroup group = (ConnectionGroup) reference.getReference();
			final ConfiguredConnectionGroupDetails groupDetails = findMatchingGroup(group);
			parentGroup.getSubgroups().add(new ConnectionGroupReference(groupDetails));
			groupDetails.setGroup(new ConnectionGroupReference(parentGroup));
			groupDetails.apply();
			
			// Recursive
			updateTree(groupDetails);
		}
		
		final List<ConnectionReference> connections = new ArrayList<>(parentGroup.getConnections());
		parentGroup.getConnections().clear();
		
		for (final ConnectionReference reference : connections)			
		{
			final UserInterfaceMqttConnectionDetails connection = (UserInterfaceMqttConnectionDetails) reference.getReference();
			final ConfiguredMqttConnectionDetails connectionDetails = findMatchingConnection(connection);
			parentGroup.getConnections().add(new ConnectionReference(connectionDetails));
			connectionDetails.setGroup(new ConnectionGroupReference(parentGroup));	
			connectionDetails.apply();
		}
		
		parentGroup.apply();
	}

	@Override
	public List<FormatterDetails> getFormatters()
	{
		return getConfiguration().getFormatting().getFormatter();
	}

	@Override
	public boolean removeFormatter(final FormatterDetails formatter)
	{
		for (final ConfiguredMqttConnectionDetails connectionDetails : getConnections())
		{		
			if (formatter.getID().equals(((FormatterDetails) connectionDetails.getFormatter()).getID()))
			{
				connectionDetails.setFormatter(null);
			}
		}
		
		getFormatters().remove(formatter);
		
		return saveConfiguration();
	}

	@Override
	public int countFormatter(final FormatterDetails formatter)
	{
		int count = 0;
		for (final ConfiguredMqttConnectionDetails connectionDetails : getConnections())
		{
			if (connectionDetails.getFormatter() == null)
			{
				continue;
			}
			
			if (formatter.getID().equals(((FormatterDetails) connectionDetails.getFormatter()).getID()))
			{
				count++;
			}
		}
		
		return count;
	}
}
