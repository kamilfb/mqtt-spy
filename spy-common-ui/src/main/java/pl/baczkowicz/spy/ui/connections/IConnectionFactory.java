package pl.baczkowicz.spy.ui.connections;

import java.util.Collection;
import java.util.List;

import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.layout.AnchorPane;
import pl.baczkowicz.spy.ui.configuration.ConfiguredConnectionGroupDetails;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;
import pl.baczkowicz.spy.ui.properties.ConnectionListItemProperties;
import pl.baczkowicz.spy.ui.properties.ModifiableConnection;

public interface IConnectionFactory
{
	static final String MQTT = "MQTT";
	
	void populateProtocolCell(final TableCell<ConnectionListItemProperties, String> cell, final String item);
	
	// String getIconNameForProtocol(final String protocol);
	
	ConnectionListItemProperties createConnectionListItemProperties(final ModifiableConnection connection);
	
	void findConnections(final ConfiguredConnectionGroupDetails parentGroup, final List<ModifiableConnection> connections);
	
	Collection<MenuItem> createMenuItems();
	
	ModifiableConnection newConnection(final String protocol);
	
	ModifiableConnection duplicateConnection(final ModifiableConnection copyFrom);
	
	Collection<AnchorPane> loadControllers(final Object parent);
	
	void editConnection(final ModifiableConnection connection);
	
	void openConnection(final ModifiableConnection connection);
	
	void setRecordModifications(final boolean record);

	void setPerspective(SpyPerspective perspective);

	void setEmptyConnectionListMode(boolean empty);

	// void readAndOpenConnection(final String protocol) throws ConfigurationException;

	void setVisible(boolean groupSelected);	
}
