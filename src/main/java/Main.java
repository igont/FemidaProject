package main.java;

import main.java.BotPack.FilesPack.FilesManipulator;
import main.java.BotPack.MainPack.MyBot;
import main.java.Excel.ExcelSQLTemp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

import static main.java.BotPack.FilesPack.FilesManipulator.readConfig;


public class Main
{
	public static final MyBot myBot = new MyBot();

	public static void main(String[] args) throws TelegramApiException, IOException, SQLException
	{
		Logger logger = LoggerFactory.getLogger(Main.class);
		System.out.println("Starting Femida Project...");
		System.out.println("------------------------------------------------------------------");

		TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
		botsApi.registerBot(myBot);
		System.out.println("Telegram bot:           Connected");

		//-------------------------------------------------------------------------------------------------------------------------
		getResourcesPath();
		readConfig(); // Читаем переменные из файла
		ExcelSQLTemp.connect();
		System.out.println("PostgreSQL:             Connected");

		System.out.println("Resources path:         " + Config.resourcesPath);

		System.out.println("Connections loaded:     " + FilesManipulator.loadSavedConnections());
		System.out.println("------------------------------------------------------------------");
		System.out.println("");




		/*RefereeAccount refereeAccount = new RefereeAccount();
		refereeAccount.fName = "Игорь";
		refereeAccount.sName = "Гонтаренко";
		refereeAccount.mName = "Алексеевич";

		ExcelSQLTemp.pullAccountsToTheTable(refereeAccount, true);

		refereeAccount = new RefereeAccount();
		refereeAccount.fName = "Алексей";
		refereeAccount.sName = "Гонтаренко";
		refereeAccount.mName = "Анатольевич";

		ExcelSQLTemp.pullAccountsToTheTable(refereeAccount, false);


		GlobalCompetition globalCompetition = new GlobalCompetition();
		globalCompetition.setCity("Мухосранск");
		globalCompetition.setDate(Date.valueOf("2003-07-23"));
		globalCompetition.setName("Пиздатое соревнование");

		globalCompetition.addParticipant(1,5,"Крутой чел", "1", "Без комментариев", "Еще более крутой чел");
		globalCompetition.addParticipant(2,4,"Крутой чел 2", "2", "Без комментариев", "Еще более крутой чел");

		SQLReal.pullGlobalCompetitionsToTheTable(globalCompetition, true);*/

	}

	private static void getResourcesPath()
	{
		String className = Main.class.getName().replace('.', '/');
		String classJar = Main.class.getResource("/" + className + ".class").toString();
		String path = "";

		String[] split = classJar.split("/");
		for(int i = 1; i < split.length; i++)
		{
			path += split[i] + "/";
			if(Objects.equals(split[i], "FemidaProject")) break;
		}
		if(path.startsWith("home"))
		{
			path = "/" + path;
		}
		Config.resourcesPath = path + "Resources/";
	}
}