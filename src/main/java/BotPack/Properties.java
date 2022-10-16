package main.java.BotPack;

import java.io.IOException;
import java.io.InputStream;

public class Properties
{
	private static final java.util.Properties PROPERTIES = new java.util.Properties();

	static
	{
		loadProperties();
	}

	public static String get(String key)
	{
		return PROPERTIES.getProperty(key);
	}
	private static void loadProperties()
	{
		try(InputStream inputStream = Properties.class.getClassLoader().getResourceAsStream("application.properties"))
		{
			PROPERTIES.load((inputStream));
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
