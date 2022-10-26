package main.java.Excel.SQLPack;

import main.java.BotPack.Senders.LoggerBot;
import main.java.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLMain
{
	public static final String BD_URL = "jdbc:postgresql://localhost:5432/";
	public static Statement statement;
	public static Connection connection;

	public static String connect(String databaseName, String user, String pass)
	{
		LoggerBot.logMethod("Connect Database", databaseName,user,pass);
		try
		{
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection((BD_URL + databaseName), user, pass);
			statement = connection.createStatement();
			LoggerBot.logMethodReturn("Connect Postgres", "Connected");
			LoggerBot.log("");
			return "Connected";
		}catch(ClassNotFoundException e)
		{
			e.printStackTrace();
			LoggerBot.logMethodReturn("Connect Postgres", "PostgreSQL JDBC driver not found");
			LoggerBot.log("");
			return "PostgreSQL JDBC driver not found";
		}catch(SQLException e)
		{
			//e.printStackTrace();
			LoggerBot.logMethodReturn("Connect Postgres", "Базы не существует", "Создаю новую базу" );
			LoggerBot.log("");

			connect("postgres", "postgres", "postgres");
			createDataBase();
			connect(Config.databaseName, Config.user, Config.userPass);
			return "Connected";
		}
	}

	public static void createDataBase()
	{
		String creation = "create database " + Config.databaseName + " with encoding \"UTF-8\";";
		try
		{
			SQLExcecuter.execute(creation);
			LoggerBot.logMethodReturn("Create Database", Config.databaseName, true);

		}catch(SQLException e)
		{
			LoggerBot.logMethodReturn("Create Database", Config.databaseName, false);
			throw new RuntimeException(e);
		}
	}

	public static void reCreateTableReferee()
	{
		String drop = "drop table " + Config.refereeTableName;
		String createNew = "create table " + Config.refereeTableName + " (" + "id smallserial, " + "f_name text, " + "s_name text, " + "m_name text, " + "city text, " + "phone text, " + "calc_points real, " + "category text, " + "birth_year integer, " + "club_type text, " + "club_name text);";

		try
		{
			SQLExcecuter.execute(drop);
			LoggerBot.logMethodReturn("Drop Table", Config.refereeTableName, true);
		}catch(SQLException e)
		{
			LoggerBot.logMethodReturn("Drop Table", Config.refereeTableName, false);
		}

		try
		{
			SQLExcecuter.execute(createNew);
			LoggerBot.logMethodReturn("Create Table", Config.refereeTableName, true);
		}catch(SQLException e)
		{
			LoggerBot.logMethodReturn("Create Table", Config.refereeTableName, false);
		}
	}

	public static void reCreateTableCompetitions()
	{
		String drop = "drop table " + Config.competitionsTableName + "";
		String createNew = "create table " + Config.competitionsTableName + " (id smallserial, " + "name text, " + "city text, " + "_date date, " + "participants_ids integer array, " + "comments text array, " + "carpets text array, " + "grades real array, " + "positions text array);";

		try
		{
			SQLExcecuter.execute(drop);
			LoggerBot.logMethodReturn("Drop Table", Config.competitionsTableName, true);
		}catch(SQLException e)
		{
			LoggerBot.logMethodReturn("Drop Table", Config.competitionsTableName, false);
			throw new RuntimeException(e);
		}

		try
		{
			SQLExcecuter.execute(createNew);
			LoggerBot.logMethodReturn("Create Table", Config.competitionsTableName, true);
		}catch(SQLException e)
		{
			LoggerBot.logMethodReturn("Create Table", Config.competitionsTableName, false);
			throw new RuntimeException(e);
		}
	}
}
