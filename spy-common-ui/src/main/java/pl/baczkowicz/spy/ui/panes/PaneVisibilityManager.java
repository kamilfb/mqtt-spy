package pl.baczkowicz.spy.ui.panes;

import java.util.Map;

import javafx.scene.control.TitledPane;

public interface PaneVisibilityManager
{
	Map<TitledPane, TitledPaneStatus> getPaneToStatusMapping();

	void setPaneVisiblity(TitledPaneStatus titledPaneStatus, PaneVisibilityStatus detached);
}
