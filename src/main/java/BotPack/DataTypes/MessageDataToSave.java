package main.java.BotPack.DataTypes;

import java.util.Date;

public class MessageDataToSave
{
	public Date date;
	public String userText;
	public String botText;

	public enum From {BOT, USER}
}
