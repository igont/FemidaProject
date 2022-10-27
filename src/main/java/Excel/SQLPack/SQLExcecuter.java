package main.java.Excel.SQLPack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLExcecuter // TODO: Сделать логирование всех Экзекутов
{
	public static void execute(PreparedStatement preparedStatement)
	{
		try
		{
			preparedStatement.execute();
		}catch(SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static boolean execute(String str) throws SQLException // Отправляем запрос, который ничего не возвращает
	{
		return SQLMain.statement.execute(str);
	}

	public static ResultSet executeQuery(PreparedStatement preparedStatement)
	{
		try
		{
			return preparedStatement.executeQuery();
		}catch(SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static ResultSet executeQuery(String str) throws SQLException // Отправляем запрос, который что-то возвращает
	{
		return SQLMain.statement.executeQuery(str);
	}
}
