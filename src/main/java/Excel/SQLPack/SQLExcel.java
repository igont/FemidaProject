package main.java.Excel.SQLPack;

import main.java.BotPack.Senders.LoggerBot;
import main.java.Config;
import main.java.Excel.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;

public class SQLExcel // Функции для связи SQL и EXCEL
{
	public static final String DELIMETR = "-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n";

	public static boolean filter(String s)
	{
		String[] items = new String[]{";", "\'", " -- "};
		return Arrays.stream(items).anyMatch(s::contains);
	}

	public static void addRefereeAccount(RefereeAccount refereeAccount) throws SQLException // Добавляем нового судью
	{
		PreparedStatement preparedStatement = SQLMain.connection.prepareStatement("insert into "+ Config.refereeTableName +"(f_name, s_name, m_name, city, phone, calc_points, category, birth_year, club_type, club_name) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
		preparedStatement.setString(1, refereeAccount.fName);
		preparedStatement.setString(2, refereeAccount.sName);
		preparedStatement.setString(3, refereeAccount.mName);
		preparedStatement.setString(4, refereeAccount.city);
		preparedStatement.setString(5, refereeAccount.phone);
		preparedStatement.setDouble(6, refereeAccount.calc_points);
		preparedStatement.setString(7, refereeAccount.category);
		preparedStatement.setInt(8, refereeAccount.birth_year);
		preparedStatement.setString(9, refereeAccount.clubType);
		preparedStatement.setString(10, refereeAccount.clubName);

		preparedStatement.execute();
	}
	public static List<RefereeAccount> readParticipants() throws IOException, SQLException // Читаем все данные о судьях в файле
	{
		//SQLMain.connect(Config.databaseName, Config.user, Config.userPass);

		List<RefereeAccount> refereeAccounts = new ArrayList<>(); // Тот массив, который будем возвращать

		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream fis = classloader.getResourceAsStream(Config.excelFileName); // Читаем файл

		XSSFWorkbook wb = new XSSFWorkbook(fis); // Записываем книгу в переменную
		XSSFSheet mySheet; // Объявляем лист

		for(int sheet = 3; sheet < wb.getNumberOfSheets(); sheet++) // Проходим по всем листам книги
		{
			mySheet = wb.getSheetAt(sheet); // Достаем из книги лист под номером

			RefereeAccount refereeAccount = new RefereeAccount();
			Calendar calendar = new GregorianCalendar();
			try
			{

				String fName = mySheet.getRow(2).getCell(5).getStringCellValue().trim();
				refereeAccount.fName = fName;
				String sName = mySheet.getRow(1).getCell(5).getStringCellValue().trim();
				refereeAccount.sName = sName;
				String mName = mySheet.getRow(3).getCell(5).getStringCellValue().trim();
				refereeAccount.mName = mName;

				int birth = 0;
				try
				{
					if(mySheet.getRow(4).getCell(5).getCellType() == CellType.STRING)
					{
						calendar.setTime(mySheet.getRow(4).getCell(5).getDateCellValue());
						birth = calendar.get(Calendar.YEAR);
					}

					if(mySheet.getRow(4).getCell(5).getCellType() == CellType.NUMERIC)
					{
						birth = (int) mySheet.getRow(4).getCell(5).getNumericCellValue();
					}

				}catch(NullPointerException e)
				{
					birth = 0;
				}
				refereeAccount.birth_year = birth;


				String city = mySheet.getRow(12).getCell(5).getStringCellValue().trim();
				refereeAccount.city = city;

				String phone = "???";
				if(mySheet.getRow(13).getCell(1).getCellType() == CellType.STRING)
					phone = mySheet.getRow(13).getCell(1).getStringCellValue().trim();

				if(mySheet.getRow(13).getCell(1).getCellType() == CellType.NUMERIC)
				{
					phone = String.valueOf((long) mySheet.getRow(13).getCell(1).getNumericCellValue());
				}
				phone = phone.replaceAll("\\*номер телефона\\*", "???");

				char[] c = phone.toCharArray();
				if(c.length == 11) phone = c[0] + "-" + c[1] + c[2] + c[3] + "-" + c[4] + c[5] + c[6] + "-" + c[7] + c[8]+ "-" + c[9] + c[10];
				else if (!phone.equals("???")) phone += " (Ошибка)";
				refereeAccount.phone = phone;

				String clubType = mySheet.getRow(13).getCell(4).getStringCellValue().trim();
				refereeAccount.clubType = clubType;
				String clubName = mySheet.getRow(13).getCell(5).getStringCellValue().trim();
				refereeAccount.clubName = clubName;
				refereeAccount.id = SQLReal.getRefereeIDByFullName(fName, sName);


				refereeAccounts.add(refereeAccount);

			}catch(NullPointerException ignored)
			{

			}
			catch(IllegalStateException e)
			{
				System.out.println("Ошибка!!! readParticipants() - IllegalStateException Фамилия: ["+refereeAccount.sName+"]");
				e.printStackTrace();//
			}
		}
		LoggerBot.logMethodReturn("readParticipants",refereeAccounts.size());
		return refereeAccounts;
	}
	public static List<Competition> readParticipantsCompetitions() throws IOException// Читает все соревнования, в которых учавствовали люди в EXCEL
	{
		//SQLMain.connect(Config.databaseName, Config.user, Config.userPass);
		List<Competition> competitions = new ArrayList<>(); // Тот массив, который будем возвращать

		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream fis = classloader.getResourceAsStream(Config.excelFileName); // Читаем файл

		XSSFWorkbook wb = new XSSFWorkbook(fis); // Записываем книгу в переменную
		XSSFSheet mySheet; // Объявляем лист
		Competition competition;

		for(int sheet = 3; sheet < wb.getNumberOfSheets(); sheet++) // Проходим по всем листам книги
		{
			mySheet = wb.getSheetAt(sheet); // Достаем из книги лист под номером
			int row = 2; // Начальный ряд, с которого начинаются соревнования

			try
			{
				while(mySheet.getRow(row++ + 1).getCell(10).getStringCellValue() != "") // Проходим по всем соревнованиям на листе
				{
					competition = new Competition();
					String lastCell = "";
					try
					{
						lastCell = "FirstName";
						String fName = mySheet.getRow(2).getCell(5).getStringCellValue().trim();
						competition.setParticipantFName(fName);

						lastCell = "SecondName";
						String sName = mySheet.getRow(1).getCell(5).getStringCellValue().trim();
						competition.setParticipantSName(sName);

						lastCell = "MidName";
						String mName = mySheet.getRow(3).getCell(5).getStringCellValue().trim();
						competition.setParticipantMName(mName);

						lastCell = "Date";
						competition.setDate(mySheet.getRow(row).getCell(8).getDateCellValue());

						lastCell = "City";
						competition.setCity(mySheet.getRow(row).getCell(9).getStringCellValue());

						lastCell = "Excel.Competition Name";
						competition.setCompetitionName(mySheet.getRow(row).getCell(10).getStringCellValue());

						lastCell = "Excel.Competition Post";
						competition.setPosition(mySheet.getRow(row).getCell(11).getStringCellValue());

						lastCell = "Excel.Competition Grade (float)";
						competition.setGrade((float) mySheet.getRow(row).getCell(12).getNumericCellValue());

						lastCell = "Carpet Chief (string)";
						competition.setCarpetChief(mySheet.getRow(row).getCell(13).getStringCellValue());

						lastCell = "Carpet Number (string)";
						competition.setCarpetNumber(String.valueOf(Math.round(mySheet.getRow(row).getCell(14).getNumericCellValue())));
						competition.id = SQLReal.getRefereeIDByFullName(fName, sName);

						competitions.add(competition);

					}catch(IllegalStateException e)
					{
						String s = String.format("Ошибка!!!     Лист: %s", mySheet.getSheetName());

						int len = s.length();

						for(int i = 0; i < (50 - len); i++)
						{
							s += " ";
						}

						s += String.format("Строка №%s    Причина: %s\n", (row + 1), lastCell);
						System.out.printf(s);
					}catch(SQLException e)
					{
						throw new RuntimeException(e);
					}
				}
			}catch(NullPointerException ignored)
			{

			}
		}
		return competitions;
	}
	public static void pullAccountsToTheTable(RefereeAccount refereeAccount,boolean clear) throws SQLException
	{
		List<RefereeAccount> refereeAccounts = Arrays.asList(refereeAccount);
		pullAccountsToTheTable(refereeAccounts,clear);
	}
	public static void pullAccountsToTheTable(List<RefereeAccount> refereeAccounts,boolean clear) throws SQLException
	{
		String clearS = "delete from "+ Config.refereeTableName +"; ALTER SEQUENCE "+ Config.refereeTableName+"_id_seq RESTART WITH 1;";

		if(clear)
		{
			SQLExcecuter.execute(clearS);
			LoggerBot.log("clearRefereesTable()");
		}

		for(RefereeAccount refereeAccount : refereeAccounts)
		{
			addRefereeAccount(refereeAccount); // Добавляем весь список
		}
		LoggerBot.logMethodReturn("addParticipants",refereeAccounts.size());
	}
	public static List<GlobalCompetition> convertCompetitionsToGlobalCompetitions(List<Competition> competitions) throws SQLException
	{
		List<GlobalCompetition> globalCompetitions = new ArrayList<>();

		for(Competition competition : competitions) // Пробегаем по всем соревнованиям
		{
			boolean isFindSame = false;
			for(GlobalCompetition globalCompetition : globalCompetitions) // Проверяем уже созданные Глобальные соревнования, чтобы добавить туда обычное соревнование с таким же названием
			{
				if(Objects.equals(globalCompetition.name.toLowerCase(), competition.competitionName.toLowerCase()) & Objects.equals(globalCompetition.date.getTime(), competition.date.getTime())) // Необходимо добавить в него
				{
					isFindSame = true;
					Participant participant = new Participant(competition);
					globalCompetition.participants.add(participant);
					break;
				}
			}
			if(!isFindSame)
			{
				GlobalCompetition globalCompetitionNew = new GlobalCompetition(competition);
				globalCompetitions.add(globalCompetitionNew);
			}
		}
		return globalCompetitions;
	}
	public static String formatArray(List list)
	{
		StringBuilder out = new StringBuilder("{");

		for(Object el : list)
		{
			out.append(el.toString());
			out.append(", ");
		}
		out.delete(out.length() - 2, out.length());
		out.append("}");
		return out.toString();
	}

	public static String formatDate(Date date)
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

		return dateFormat.format(calendar.getTime());
	}
}
