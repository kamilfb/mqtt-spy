package pl.baczkowicz.spy.ui;

import java.util.Collection;

import javafx.scene.control.Tab;
import pl.baczkowicz.spy.exceptions.ConfigurationException;
import pl.baczkowicz.spy.ui.connections.IUiConnection;
import pl.baczkowicz.spy.ui.controllers.IConnectionController;
import pl.baczkowicz.spy.ui.properties.ModifiableConnection;

public interface IConnectionViewManager
{
	IConnectionController getControllerForTab(final Tab selectedTab);

	Collection<? extends IConnectionController> getConnectionControllers();
	
	void disconnectAll();
	
	void disconnectAndCloseAll();

	void autoOpenConnections();

	//void disconnectAndCloseTab(IUiConnection connection);

	//void disconnectFromBroker(IUiConnection connection);

	//boolean connectToBroker(IUiConnection connection);

	void openConnection(ModifiableConnection connectionDetails) throws ConfigurationException;

	Collection<IUiConnection> getConnections();

	// void connectToBroker(IUiConnection connection);
}
