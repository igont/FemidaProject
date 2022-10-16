package main.java.BotPack.DataTypes;

import java.util.Date;

public class FileLogIngo
{
	public String msg;
	public Date date;
	public String user;

	public FileLogIngo(String msg, Date date, String user)
	{
		this.msg = msg;
		this.date = date;
		this.user = user;
	}
}
