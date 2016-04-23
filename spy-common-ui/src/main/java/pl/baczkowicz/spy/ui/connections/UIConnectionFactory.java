package pl.baczkowicz.spy.ui.connections;

import java.util.Collection;
import java.util.List;

import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.layout.AnchorPane;
import pl.baczkowicz.spy.ui.configuration.ConfiguredConnectionGroupDetails;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;
import pl.baczkowicz.spy.ui.properties.ConnectionListItemProperties;
import pl.baczkowicz.spy.ui.properties.ModifiableItem;

public interface UIConnectionFactory
{
	static final String MQTT = "MQTT";
	
	void populateProtocolCell(final TableCell<ConnectionListItemProperties, String> cell, final String item);
	
	// String getIconNameForProtocol(final String protocol);
	
	ConnectionListItemProperties createConnectionListItemProperties(final ModifiableItem connection);
	
	void findConnections(final ConfiguredConnectionGroupDetails parentGroup, final List<ModifiableItem> connections);
	
	Collection<MenuItem> createMenuItems();
	
	ModifiableItem newConnection(final String protocol);
	
	ModifiableItem duplicateConnection(final ModifiableItem copyFrom);
	
	Collection<AnchorPane> loadControllers(final Object parent);
	
	void editConnection(final ModifiableItem connection);
	
	void openConnection(final ModifiableItem connection);
	
	void setRecordModifications(final boolean record);

	void setPerspective(SpyPerspective perspective);

	void setEmptyConnectionListMode(boolean empty);

	// void readAndOpenConnection(final String protocol) throws ConfigurationException;

	void setVisible(boolean groupSelected);	
}
