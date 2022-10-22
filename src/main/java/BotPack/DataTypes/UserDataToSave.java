package main.java.BotPack.DataTypes;

import main.java.BotPack.Processors.Processer;

import java.util.List;

public class UserDataToSave
{
	public UserDataToSave(Connection con)
	{
		if(con.tgBdRelation == null)
			tgBdRelation = new TgBdRelation(-1, TgBdRelation.Position.READER);
		else tgBdRelation = con.tgBdRelation;

		userName = con.userName;
		menuStep = con.menuStep;
		countMessages = con.countMessages;
	}
	private TgBdRelation tgBdRelation;
	private String userName;

	public String getUserName()
	{
		return userName;
	}

	private Processer.MenuStep menuStep;
	private int countMessages;

	public static List<UserDataToSave> allUserDataToSave; // Та инфа, которую мы прочитали с файла
	public Connection getConnection()
	{
		Connection connection = new Connection();

		connection.tgBdRelation = tgBdRelation;
		connection.userName = userName;
		connection.menuStep = menuStep;
		connection.countMessages = countMessages;

		return connection;
	}

}
