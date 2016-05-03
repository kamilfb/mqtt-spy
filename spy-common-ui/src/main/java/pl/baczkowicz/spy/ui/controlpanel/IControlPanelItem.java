package pl.baczkowicz.spy.ui.controlpanel;

import javafx.scene.control.Button;
import pl.baczkowicz.spy.ui.controllers.ControlPanelItemController;

public interface IControlPanelItem
{
	void update(final ControlPanelItemController controlPanelItemController, final Button button);
}
