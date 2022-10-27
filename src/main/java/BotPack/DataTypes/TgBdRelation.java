package main.java.BotPack.DataTypes;

public class TgBdRelation
{
	public int id;
	public Position position;

	public TgBdRelation(int id, Position position)
	{
		this.id = id;
		this.position = position;
	}

	public enum Position
	{
		READER,
		EDITOR,
		ADMIN
	}
}
