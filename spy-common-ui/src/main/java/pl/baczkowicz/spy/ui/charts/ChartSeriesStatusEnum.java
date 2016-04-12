package pl.baczkowicz.spy.ui.charts;

public enum ChartSeriesStatusEnum
{
	NO_MESSAGES("No messages"),
	OK("OK"), 
	ERROR("Error");

	private final String value;

	ChartSeriesStatusEnum(String v)
	{
		value = v;
	}

	public String value()
	{
		return value;
	}

	public static ChartSeriesStatusEnum fromValue(final String v)
	{
		for (ChartSeriesStatusEnum c : ChartSeriesStatusEnum.values())
		{
			if (c.value.equals(v))
			{
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}
}
