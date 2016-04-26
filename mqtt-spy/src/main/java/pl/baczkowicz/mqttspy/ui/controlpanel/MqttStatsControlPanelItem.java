package pl.baczkowicz.mqttspy.ui.controlpanel;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.ui.controllers.ControlPanelItemController;
import pl.baczkowicz.spy.ui.controlpanel.ControlPanelStatsUpdater;
import pl.baczkowicz.spy.ui.controlpanel.IControlPanelItem;
import pl.baczkowicz.spy.ui.controls.GettingInvolvedTooltip;

public class MqttStatsControlPanelItem implements IControlPanelItem
{
	private IKBus eventBus;
	
	public MqttStatsControlPanelItem(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}

	@Override
	public void update(ControlPanelItemController controlPanelItemController, Button button)
	{
		showMqttSpyStats(controlPanelItemController, button, eventBus);
	}
	
	public static void showMqttSpyStats(final ControlPanelItemController controller, final Button button, final IKBus eventBus)
	{
		controller.refresh();
		
		final ControlPanelStatsUpdater statsUpdater = new ControlPanelStatsUpdater(controller, button, eventBus);
		statsUpdater.show();
		
		final String text = 
				"mqtt-spy needs you! Please support the project" + System.lineSeparator()
				+ "by raising bugs, " + "helping out with testing" + System.lineSeparator()
				+ "or making a charity donation. " + System.lineSeparator()
				+ "See http://github.com/kamilfb/mqtt-spy/wiki/Getting-involved" + System.lineSeparator()
				+ "for more information on how to get involved." + System.lineSeparator();		
		
		final GettingInvolvedTooltip gettingInvolvedTooltip = new GettingInvolvedTooltip(text, "mqtt-spy-logo");				  
		button.setTooltip(gettingInvolvedTooltip);
		button.setOnMouseMoved(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				gettingInvolvedTooltip.setCurrentMousePosition(event);
				
				if (gettingInvolvedTooltip.isShowing())
				{
					gettingInvolvedTooltip.checkAndHide();
				}
			}
		});
	}
}
