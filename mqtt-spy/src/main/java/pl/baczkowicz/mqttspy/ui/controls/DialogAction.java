package pl.baczkowicz.mqttspy.ui.controls;

public class DialogAction
{
	private String heading;
	private String longText;

	public DialogAction(final String heading, final String longText)
	{
		this.setHeading(heading);
		this.setLongText(longText);
	}

	/**
	 * @return the heading
	 */
	public String getHeading()
	{
		return heading;
	}

	/**
	 * @param heading the heading to set
	 */
	public void setHeading(String heading)
	{
		this.heading = heading;
	}

	/**
	 * @return the longText
	 */
	public String getLongText()
	{
		return longText;
	}

	/**
	 * @param longText the longText to set
	 */
	public void setLongText(String longText)
	{
		this.longText = longText;
	}
}
