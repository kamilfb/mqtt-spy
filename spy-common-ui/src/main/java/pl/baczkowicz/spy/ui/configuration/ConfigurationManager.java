package pl.baczkowicz.spy.ui.configuration;

import java.util.Collection;
import java.util.List;

import pl.baczkowicz.spy.ui.properties.ModifiableItem;

public interface ConfigurationManager
{
	ConfiguredConnectionGroupDetails getRootGroup();

	String generateConnectionGroupId();

	boolean saveConfiguration();

	boolean isConfigurationWritable();

	List<ConfiguredConnectionGroupDetails> getConnectionGrops();

	Collection<? extends ModifiableItem> getConnections();
}
