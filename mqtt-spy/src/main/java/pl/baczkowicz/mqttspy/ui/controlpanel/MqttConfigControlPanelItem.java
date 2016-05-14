package pl.baczkowicz.mqttspy.ui.controlpanel;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.ui.configuration.BaseConfigurationManager;
import pl.baczkowicz.spy.ui.configuration.IConfigurationManager;
import pl.baczkowicz.spy.ui.controllers.ControlPanelItemController;
import pl.baczkowicz.spy.ui.controlpanel.IControlPanelItem;
import pl.baczkowicz.spy.ui.controlpanel.ItemStatus;
import pl.baczkowicz.spy.ui.events.LoadConfigurationFileEvent;
import pl.baczkowicz.spy.ui.utils.DialogFactory;

public class MqttConfigControlPanelItem implements IControlPanelItem
{
	private IConfigurationManager configurationManager;

	private IKBus eventBus;

	public MqttConfigControlPanelItem(final IConfigurationManager configurationManager, final IKBus eventBus)
	{
		this.configurationManager = configurationManager;
		this.eventBus = eventBus;
	}
	
	@Override
	public void update(final ControlPanelItemController controlPanelItemController, final Button button)
	{
		showConfigurationFileStatus(controlPanelItemController, button, configurationManager, eventBus);		
	}
	
	public void showConfigurationFileStatus(
			final ControlPanelItemController controller, final Button button, final IConfigurationManager configurationManager, final IKBus eventBus)
	{
		if (configurationManager.getLoadedConfigurationFile() == null)
		{
			controller.setTitle("No configuration file found.");
			controller.setDetails("Click here display all available options for resolving missing configuration file.");
			controller.setStatus(ItemStatus.WARN);
			
			button.setOnAction(new EventHandler<ActionEvent>()
			{			
				@Override
				public void handle(ActionEvent event)
				{
					if (DialogFactory.showDefaultConfigurationFileMissingChoice("Configuration file not found", button.getScene()))
					{
						eventBus.publish(new LoadConfigurationFileEvent(BaseConfigurationManager.getDefaultConfigurationFileObject()));
					}					
				}
			});
		}
		else
		{
			button.setOnAction(null);
			
			if (configurationManager.isConfigurationReadOnly())
			{
				controller.setTitle("Configuration file loaded, but it's read-only.");
				controller.setDetails("The configuration that has been loaded from " + configurationManager.getLoadedConfigurationFile().getAbsolutePath() + " is read-only.");
				controller.setStatus(ItemStatus.WARN);
			}
			else
			{
				controller.setTitle("Configuration file loaded successfully.");
				controller.setDetails("The configuration has been loaded from " + configurationManager.getLoadedConfigurationFile().getAbsolutePath() + ".");				
				controller.setStatus(ItemStatus.OK);
			}
		}
		
		controller.refresh();		
	}	
}
