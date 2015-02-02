function publish()
{
	var Thread = Java.type("java.lang.Thread");

	for (i = 0; i < 20; i++)
	{
		// "<temp>" + 
		mqttspy.publish("/home/office/current", (20 + Math.floor((Math.random() * 20) + 1) / 1), 0, false);
		// + "</temp>"
		
		if (i == 10)
		{
			Thread.sleep(10000);
		}

		try 
		{
			Thread.sleep(1000);
		}
		catch(err) 
		{
			return false;				
		}
	}

	return true;
}

publish();
