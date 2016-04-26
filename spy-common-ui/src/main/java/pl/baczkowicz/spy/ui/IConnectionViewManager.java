package pl.baczkowicz.spy.ui;

import java.util.Collection;

import javafx.scene.control.Tab;
import pl.baczkowicz.spy.ui.controllers.IConnectionController;

public interface IConnectionViewManager
{
	IConnectionController getControllerForTab(final Tab selectedTab);

	Collection<? extends IConnectionController> getConnectionControllers();
	
	void disconnectAll();
	
	void disconnectAndCloseAll();

	void autoOpenConnections();
}
