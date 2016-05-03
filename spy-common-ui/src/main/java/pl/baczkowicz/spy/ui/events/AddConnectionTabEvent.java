package pl.baczkowicz.spy.ui.events;

import javafx.scene.control.Tab;

public class AddConnectionTabEvent
{
	private final Tab tab;

	public AddConnectionTabEvent(final Tab tab)
	{
		this.tab = tab;
	}

	/**
	 * @return the tab
	 */
	public Tab getTab()
	{
		return tab;
	}
}
