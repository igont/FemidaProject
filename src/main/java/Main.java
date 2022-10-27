package main.java;

import main.java.BotPack.FilesPack.FilesManipulator;
import main.java.BotPack.MainPack.MyBot;
import main.java.Excel.SQLPack.SQLMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import static main.java.BotPack.FilesPack.FilesManipulator.readConfig;


public class Main
{
	public static final MyBot myBot = new MyBot();

	public static void main(String[] args)
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
}