package main.java.Excel.SQLPack;

import com.google.common.collect.Lists;
import main.java.BotPack.Senders.LoggerBot;
import main.java.Config;
import main.java.Excel.GlobalCompetition;
import main.java.Excel.Participant;
import main.java.Excel.RefereeAccount;
import org.postgresql.util.PSQLException;

import java.io.IOException;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class SQLReal
{
	private static Array array;

	public static void pullGlobalCompetitionsToTheTable(GlobalCompetition globalCompetition, boolean clear) throws SQLException, IOException
	{
		List<GlobalCompetition> globalCompetitions = Arrays.asList(globalCompetition);
		pullGlobalCompetitionsToTheTable(globalCompetitions, clear);
	}

	public static void pullGlobalCompetitionsToTheTable(List<GlobalCompetition> globalCompetitions, boolean clear) throws SQLException, IOException
	{
		String clearS = "delete from " + Config.competitionsTableName + "; ALTER SEQUENCE " + Config.competitionsTableName + "_id_seq RESTART WITH 1;";

		if(clear) SQLExcecuter.execute(clearS);

		for(GlobalCompetition g : globalCompetitions)
		{
			addGlobalCompetition(g);
		}
		SQLReal.reCountAllGrades();
	}

	public static void addGlobalCompetition(GlobalCompetition globalCompetition) throws SQLException// Добавляет соревнование в таблицу соревнований
	{
		List<Integer> ids = new ArrayList<>();
		List<Float> grades = new ArrayList<>();
		List<String> positions = new ArrayList<>();
		List<String> comments = new ArrayList<>();
		List<String> carpets = new ArrayList<>();

		for(Participant participant : globalCompetition.participants)
		{
			grades.add(participant.grade);
			positions.add(participant.position);
			comments.add(participant.comment);
			carpets.add(participant.carpet);
			ids.add(participant.id);
		}

		PreparedStatement preparedStatement = null;
		try
		{
			preparedStatement = SQLMain.connection.prepareStatement("insert into " + Config.competitionsTableName + "(name, city, _date, participants_ids, grades, positions, comments, carpets) values(?, ?, ?, ?, ?, ?, ?, ?);");
			preparedStatement.setString(1, globalCompetition.name);
			preparedStatement.setString(2, globalCompetition.city);
			preparedStatement.setDate(3, new java.sql.Date(globalCompetition.date.getTime()));
			preparedStatement.setArray(4, SQLMain.connection.createArrayOf("integer", ids.toArray()));
			preparedStatement.setArray(5, SQLMain.connection.createArrayOf("double", grades.toArray()));
			preparedStatement.setArray(6, SQLMain.connection.createArrayOf("VARCHAR", positions.toArray()));
			preparedStatement.setArray(7, SQLMain.connection.createArrayOf("VARCHAR", comments.toArray()));
			preparedStatement.setArray(8, SQLMain.connection.createArrayOf("VARCHAR", carpets.toArray()));

			preparedStatement.execute();
		}catch(SQLException e)
		{
			System.out.println(preparedStatement.toString());
			throw new RuntimeException(e);
		}
	}

	public static void editGlobalCompetition(int id, GlobalCompetition globalCompetition)
	{

	}

	public static void reCountAllGrades() throws SQLException, IOException
	{
		String s = "select count(*) from " + Config.refereeTableName;
		ResultSet resultSet = SQLExcecuter.executeQuery(s);
		resultSet.next();
		int countReferees = resultSet.getInt("count");

		List<Float> countGrades = new ArrayList<>();

		while(countReferees > 0)
		{
			float points = getTotalGradeByID(countReferees);
			countGrades.add(points);
			countReferees--;
		}

		countGrades = Lists.reverse(countGrades);
		changeCountGrades(countGrades);
	}

	private static void changeCountGrades(List<Float> countGrades) throws SQLException
	{
		for(int i = countGrades.size(); i > 0; i--)
		{
			changeCountGrade(i, countGrades.get(i - 1));
		}
	}

	private static void changeCountGrade(int id, float totalGrade) throws SQLException
	{
		String s = "update " + Config.refereeTableName + " set calc_points = " + totalGrade + " where id = " + id;
		SQLExcecuter.execute(s);
	}

	public static float getTotalGradeByID(int id) throws SQLException
	{
		String s = "select grades[num[1]] from(select grades, array_positions(participants_ids," + id + ") as num from " + Config.competitionsTableName + ") as uns;";
		ResultSet resultSet = SQLExcecuter.executeQuery(s);
		float all = 0;
		while(resultSet.next())
		{
			all += resultSet.getDouble("grades");
		}
		s = "select grades[num[2]] from(select grades, array_positions(participants_ids," + id + ") as num from " + Config.competitionsTableName + ") as uns;";
		resultSet = SQLExcecuter.executeQuery(s);
		while(resultSet.next())
		{
			all += resultSet.getDouble("grades");
		}
		s = "select grades[num[3]] from(select grades, array_positions(participants_ids," + id + ") as num from " + Config.competitionsTableName + ") as uns;";
		resultSet = SQLExcecuter.executeQuery(s);
		while(resultSet.next())
		{
			all += resultSet.getDouble("grades");
		}
		return all;
	}

	public static List<Integer> getCompetitionsIDsByRefereeID(int id) throws SQLException
	{
		String s = "select id from " + Config.competitionsTableName + " where " + id + " = any (participants_ids);";
		ResultSet resultSet = SQLExcecuter.executeQuery(s);
		List<Integer> out = new ArrayList<>();
		while(resultSet.next())
		{
			out.add(resultSet.getInt("id"));
		}
		return out;
	}

	public static GlobalCompetition getGlobalCompetitionByID(int id) throws SQLException
	{
		String s = "select * from " + Config.competitionsTableName + " where id = " + id + ";";
		ResultSet resultSet = SQLExcecuter.executeQuery(s);
		GlobalCompetition competition = new GlobalCompetition();
		resultSet.next();

		competition.name = resultSet.getString("name");
		competition.city = resultSet.getString("city");
		competition.date = resultSet.getDate("_date");

		Integer[] participants_id = (Integer[]) resultSet.getArray("participants_ids").getArray();
		Integer[] grades = (Integer[]) resultSet.getArray("grades").getArray();
		String[] positions = (String[]) resultSet.getArray("positions").getArray();
		String[] comments = (String[]) resultSet.getArray("comments").getArray();
		String[] carpets = (String[]) resultSet.getArray("carpets").getArray();

		for(int i = 0; i < grades.length; i++)
		{
			Participant participant = new Participant();

			participant.grade = grades[i];
			participant.comment = comments[i];
			participant.carpet = carpets[i];
			participant.position = positions[i];
			participant.id = participants_id[i];

			competition.participants.add(participant);
		}


		return competition;
	}

	public static RefereeAccount getRefereeByID(int id) throws SQLException
	{
		ResultSet resultSet;
		try
		{
			PreparedStatement preparedStatement = SQLMain.connection.prepareStatement("select * from " + Config.refereeTableName + " where id = ?");
			preparedStatement.setInt(1, id);
			resultSet = preparedStatement.executeQuery();
			LoggerBot.logMethodReturn("getRefereeByID",id, "Найден");
		}catch(PSQLException e)
		{
			LoggerBot.logMethodReturn("getRefereeByID",id, "Ошибка");
			e.printStackTrace();
			System.out.println("Создать таблицу " + Config.refereeTableName+"? (y/n)");

			Scanner in = new Scanner(System.in);
			if(in.nextLine().equalsIgnoreCase("y"))
			{
				LoggerBot.log("В консоли вызвана функция пересоздания таблицы",Config.refereeTableName);

				SQLMain.reCreateTableReferee();
			}
			System.out.println("Заполнить таблицу данными из файла " + Config.excelFileName+"? (y/n)");

			if(in.nextLine().equalsIgnoreCase("y"))
			{
				LoggerBot.log("В консоли вызвана функция чтения данных их файла для заполнения в базу данных",Config.excelFileName);
				try
				{
					List<RefereeAccount> refereeAccounts = SQLExcel.readParticipants();
					SQLExcel.pullAccountsToTheTable(refereeAccounts,true);
				}catch(IOException ex)
				{
					throw new RuntimeException(ex);
				}
			}
			return null;
		}
		resultSet.next();

		try
		{
			resultSet.getString("f_name");
		}catch(PSQLException e)
		{
			LoggerBot.logMethodReturn("getRefereeByID", "Аккаунт не найден");
			LoggerBot.log("");
			//e.printStackTrace();
			return null;
		}
		RefereeAccount refereeAccount = new RefereeAccount();
		refereeAccount.fName = resultSet.getString("f_name");
		refereeAccount.sName = resultSet.getString("s_name");
		refereeAccount.mName = resultSet.getString("m_name");
		refereeAccount.city = resultSet.getString("city");
		refereeAccount.phone = resultSet.getString("phone");
		refereeAccount.calc_points = resultSet.getInt("calc_points");
		refereeAccount.category = resultSet.getString("category");
		refereeAccount.birth_year = resultSet.getInt("birth_year");
		refereeAccount.clubType = resultSet.getString("club_type");
		refereeAccount.clubName = resultSet.getString("club_name");
		refereeAccount.id = id;

		LoggerBot.logMethodReturn("getRefereeByID", refereeAccount.sName, refereeAccount.fName, refereeAccount.mName);
		return refereeAccount;
	}

	public static int getRefereeIDByFullName(String fName, String sName) throws SQLException
	{
		PreparedStatement preparedStatement = SQLMain.connection.prepareStatement("select * from " + Config.refereeTableName + " where f_name = ? and s_name = ?;");
		preparedStatement.setString(1, fName);
		preparedStatement.setString(2, sName);

		ResultSet resultSet = SQLExcecuter.executeQuery(preparedStatement.toString());

		if(resultSet.next())
		{
			int id = resultSet.getInt("id");
			return id;
		}
		else
		{
			System.out.println("Ошибка!!! getRefereeIDByFullName([" + fName + "], [" + sName + "]) - ID не найден");
			return -1;
		}
	}

	public static RefereeAccount getRefereeByInitials(String s, String f, String m) throws SQLException
	{
		s += "%";
		f += "%";
		m += "%";

		PreparedStatement preparedStatement = SQLMain.connection.prepareStatement("select * from " + Config.refereeTableName + " where f_name like ? and s_name like ? and m_name like ?;");
		preparedStatement.setString(1, f);
		preparedStatement.setString(2, s);
		preparedStatement.setString(3, m);
		ResultSet resultSet = preparedStatement.executeQuery();

		resultSet.next();
		try
		{
			int id = resultSet.getInt("id");
			return SQLReal.getRefereeByID(id);
		}catch(PSQLException ignored)
		{
			return null;
		}
	}

	public static void editParticipant(int id, RefereeAccount refereeAccount) // Редактируем данные судьи
	{
/*		String s = String.format("UPDATE "+ Conditions.refereeTableName +" SET first_name='%s', last_name='%s', patronymic='%s', city='%s', phone='%s', birth=%d, club_type='%s', club_name='%s' WHERE id=%d;", refereeAccount.getFirst_name(), refereeAccount.getLast_name(), refereeAccount.getPatronymic(), refereeAccount.getCity(), refereeAccount.getPhone(), refereeAccount.getBirth_year(), refereeAccount.getClubType(), refereeAccount.getClubName(), id);
		System.out.println(s);
		try
		{
			ExcelSQLTemp.execute(s);
		}catch(SQLException e)
		{
			throw new RuntimeException(e);
		}*/
	}
}
