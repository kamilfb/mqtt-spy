package pl.baczkowicz.spy.ui.charts;

public enum ChartSeriesTypeEnum
{
	PAYLOAD_PLAIN("Payload"),
	
	PAYLOAD_XML("XPath"),
	
	PAYLOAD_JSON("JSONPath"),
	
	// PAYLOAD_JAVASCRIPT("JavaScript"),
	
	SIZE("Message size");

	private final String value;

	ChartSeriesTypeEnum(String v)
	{
		value = v;
	}

	public String value()
	{
		return value;
	}

	public static ChartSeriesTypeEnum fromValue(final String v)
	{
		for (ChartSeriesTypeEnum c : ChartSeriesTypeEnum.values())
		{
			if (c.value.equals(v))
			{
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}
}
