package main.java;

import main.java.BotPack.FilesPack.FilesManipulator;
import main.java.BotPack.FilesPack.MyFile;
import main.java.BotPack.MainPack.MyBot;
import main.java.BotPack.Senders.LoggerBot;
import main.java.Excel.Competition;
import main.java.Excel.GlobalCompetition;
import main.java.Excel.RefereeAccount;
import main.java.Excel.SQLPack.SQLExcecuter;
import main.java.Excel.SQLPack.SQLExcel;
import main.java.Excel.SQLPack.SQLMain;
import main.java.Excel.SQLPack.SQLReal;
import org.apache.poi.EmptyFileException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import static main.java.BotPack.FilesPack.FilesManipulator.readConfig;
import static main.java.BotPack.FilesPack.ResourcesFiles.FEMIDA_EXCEL;


public class Main
{
	public static final MyBot myBot = new MyBot();

	public static void main(String[] args) throws IOException, SQLException
	{
		Logger logger = LoggerFactory.getLogger(Main.class); // Подключаем херню для логов каких-то


		System.out.println("Starting Femida Project...");
		System.out.println("------------------------------------------------------------------");
		System.out.println("Resources path:         " + FilesManipulator.getResourcesPath());                                   // Получаем путь до файла с ресурсами
		readConfig();                                                                                                           // Читаем переменные из файла
		System.out.println("Connecting bot:         " + startBot());                                                            // Запускаем Long pooling Телеграм бота
		System.out.println("PostgreSQL:             " + SQLMain.connect(Config.databaseName, Config.user, Config.userPass));    // Подключаемся к базе данных Postgres
		System.out.println("Connections loaded:     " + FilesManipulator.loadSavedConnections());                               // Загружаем аккаунты пользователей, сохраненные с прошлых сессий
		System.out.println("------------------------------------------------------------------");
		System.out.println("");

		SQLTests();
	}

	private static String startBot()
	{
		try
		{
			TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
			botsApi.registerBot(myBot);
			return "Connected";
		}catch(TelegramApiException e)
		{
			e.printStackTrace();
			return "Error";
		}
	}

	public static void SQLTests()
	{
		boolean bookExist = bookExistTest();
		SQLTestReferee(bookExist);
		SQLTestCompetitions(bookExist);
	}

	private static void SQLTestReferee(boolean bookExist)
	{
		LoggerBot.log("SQLTestReferee()");
		ResultSet resultSet;

		try
		{
			resultSet = SQLExcecuter.executeQuery("select * from " + Config.refereeTableName + ";");
		}catch(SQLException e)
		{
			LoggerBot.logMethodReturn(Config.refereeTableName + "_exist   ", "❌");
			LoggerBot.logMethodReturn(Config.refereeTableName + "_filled  ", "❌");
			LoggerBot.log("");

			SQLMain.reCreateTableReferee();

			return;
			// обработка ошибки
		}

		try
		{
			resultSet.next();
			resultSet.getInt("id");

			LoggerBot.logMethodReturn(Config.refereeTableName + "_exist", "✔");
			LoggerBot.logMethodReturn(Config.refereeTableName + "_filled", "✔");
			LoggerBot.log("");

		}catch(SQLException e)
		{
			LoggerBot.logMethodReturn(Config.refereeTableName + "_exist", "✔");
			LoggerBot.logMethodReturn(Config.refereeTableName + "_filled", "❌");
			LoggerBot.log("");

			if(!bookExist) return;

			System.out.print("Считать аккаунты судей с таблицы? ");
			if(!new Scanner(System.in).nextLine().equalsIgnoreCase("y")) return;

			try
			{
				List<RefereeAccount> refereeAccounts = SQLExcel.readParticipants();
				SQLExcel.pullAccountsToTheTable(refereeAccounts, true);
			}catch(IOException | SQLException ex)
			{
				throw new RuntimeException(ex);
			}
			System.out.print("аккаунты считаны");
		}
	}

	private static boolean bookExistTest()
	{
		LoggerBot.log("bookExistTest()");
		try
		{
			XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(new File(new MyFile(FEMIDA_EXCEL).getPath().toUri())));
			LoggerBot.logMethodReturn(Config.excelFileName + "_exist", "✔");
			LoggerBot.logMethodReturn(Config.excelFileName + "_filled", "✔");
			LoggerBot.log("");

			return true;

		}catch(EmptyFileException e)
		{
			LoggerBot.logMethodReturn("bookTest" + "_exist", "✔");
			LoggerBot.logMethodReturn("bookTest" + "_filled", "❌");
			LoggerBot.log("");

			return false;
		}catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}


	private static void SQLTestCompetitions(boolean bookExist)
	{
		LoggerBot.log("SQLTestCompetitions()");
		ResultSet resultSet;
		try
		{
			resultSet = SQLExcecuter.executeQuery("select * from " + Config.competitionsTableName + ";");
		}catch(SQLException e)
		{
			LoggerBot.logMethodReturn(Config.competitionsTableName + "_exist", "❌");
			LoggerBot.logMethodReturn(Config.competitionsTableName + "_filled", "❌");
			LoggerBot.log("");

			SQLMain.reCreateTableCompetitions();
			return;

		}

		try
		{
			resultSet.next();
			resultSet.getInt("id");

			LoggerBot.logMethodReturn(Config.competitionsTableName + "_exist", "✔");
			LoggerBot.logMethodReturn(Config.competitionsTableName + "_filled", "✔");
			LoggerBot.log("");

		}catch(SQLException e)
		{
			LoggerBot.logMethodReturn(Config.competitionsTableName + "_exist", "✔");
			LoggerBot.logMethodReturn(Config.competitionsTableName + "_filled", "❌");
			LoggerBot.log("");

			if(!bookExist) return;

			System.out.print("Считать соревнования с таблицы? ");
			if(!new Scanner(System.in).nextLine().equalsIgnoreCase("y")) return;

			try
			{
				List<Competition> competitions = SQLExcel.readParticipantsCompetitions();
				List<GlobalCompetition> globalCompetitions = SQLExcel.convertCompetitionsToGlobalCompetitions(competitions);
				SQLReal.pullGlobalCompetitionsToTheTable(globalCompetitions, true);
			}catch(IOException | SQLException ex)
			{
				throw new RuntimeException(ex);
			}
			System.out.print("Соревнования считаны");
		}
	}
}