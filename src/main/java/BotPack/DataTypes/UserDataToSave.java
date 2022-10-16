package main.java.BotPack.DataTypes;

import main.java.BotPack.Processors.Processer;

public class UserDataToSave
{
	public UserDataToSave(Connection connection)
	{
		femidaID = connection.femidaID;
		name = connection.userName;
		menuStep = connection.menuStep;
	}
	public String femidaID;
	public String name;
	public Processer.MenuStep menuStep;
}
