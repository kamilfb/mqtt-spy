package pl.baczkowicz.spy.ui.connections;

import pl.baczkowicz.spy.connectivity.ConnectionStatus;
import pl.baczkowicz.spy.connectivity.IConnection;

public interface IUiConnection extends IConnection
{
	// Boolean isAutoOpen();

	String getId();

	boolean isOpening();

	boolean isOpened();

	ConnectionStatus getConnectionStatus();

	String getName();
}
