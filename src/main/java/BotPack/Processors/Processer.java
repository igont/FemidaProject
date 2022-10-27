package main.java.BotPack.Processors;

import main.java.BotPack.Builders.KeyboardBuilder;
import main.java.BotPack.DataTypes.Cache;
import main.java.BotPack.DataTypes.Connection;
import main.java.BotPack.DataTypes.TgBdRelation;
import main.java.BotPack.DataTypes.UserDataToSave;
import main.java.BotPack.FilesPack.MyFile;
import main.java.BotPack.Properties;
import main.java.BotPack.Senders.LoggerBot;
import main.java.BotPack.Senders.SendBotMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static main.java.BotPack.FilesPack.FilesManipulator.readConfig;
import static main.java.BotPack.FilesPack.ResourcesFiles.*;

public class Processer
{

	public static List<Connection> connections;
	public static Cache cache;

	static
	{
		connections = new ArrayList<>();
	}

	public static void processUpdate(Update update)// Добавляет зависимости и возвращает тип поведения бота
	{
		cache = new Cache();
		cache.update = update;

		if(update.hasMessage()) processMessage();
		if(update.hasCallbackQuery()) processButton();
	}

	public static void processButton()
	{
		checkConnections();
		Update update = cache.update;
		Connection connection = cache.connection;

		String data = update.getCallbackQuery().getData().toUpperCase();
		LoggerBot.logMethod("processButton", Connection.getName(), data);
		LoggerBot.log("");

		switch(connection.menuStep)
		{
			case MENU ->
			{
				processButonsFromMenu(data);
				processPersonalCommands(data);
			}
			case PROCESS_TESTING ->
			{
				processButtonsFromTesting(connection, data);
			}
		}
	}

	enum PersonalCommands
	{
		DOWNLOAD_LOGS, RE_READ_CONFIG, BOT_TURN_OFF, EDIT_COMPETITION, ADD_COMPETITION, EDIT_ACCOUNT, ADD_ACCOUNT, ACCOUNT_SEARCH, RATING_SEARCH, GLOBAL_RATING
	}

	private static void processPersonalCommands(String data)
	{
		PersonalCommands personalCommands;
		try
		{
			personalCommands = Enum.valueOf(PersonalCommands.class, data);

		}catch(IllegalArgumentException e)
		{
			return;
		}

		switch(personalCommands)
		{

			case DOWNLOAD_LOGS ->
			{
				SendBotMessage sendBotMessage = new SendBotMessage();
				sendBotMessage.setSendFile(new MyFile(SYSTEM_LOG).getFile());
				sendBotMessage.sendPreparedMessage();

				sendBotMessage.setSendFile(new MyFile(TEST_LOG).getFile());
				sendBotMessage.sendPreparedMessage();
			}
			case RE_READ_CONFIG ->
			{
				readConfig();
				SendBotMessage.send("OK");
			}
			case BOT_TURN_OFF ->
			{
				SendBotMessage.send("Goodbye");
				LoggerBot.log("Выключение бота из чата");
				System.exit(0);
			}

			case ADD_COMPETITION ->
			{
			}
			case EDIT_COMPETITION ->
			{
			}
			case ADD_ACCOUNT ->
			{
			}
			case EDIT_ACCOUNT ->
			{
			}

			case ACCOUNT_SEARCH ->
			{
			}
			case RATING_SEARCH ->
			{
			}
			case GLOBAL_RATING ->
			{
				showGlobalRating();
			}
		}

	}

	private static void showGlobalRating()
	{

	}

	private static void processButtonsFromTesting(Connection connection, String data)
	{
		switch(data)
		{
			case "CANCEL_STARTED_TEST" ->
			{
				Deleter.requestForDeletion(SendDifferentMessages.ActiveMessageType.REQUEST_CANCEL_TEST_MESSAGE);
				Deleter.requestForDeletion(SendDifferentMessages.ActiveMessageType.TEST_MESSAGE);

				connection.setMenuStep(MenuStep.MENU);

				SendBotMessage.send("Тест прерван");

			}
			case "CONTINUE" ->
			{
				Deleter.requestForDeletion(SendDifferentMessages.ActiveMessageType.REQUEST_CANCEL_TEST_MESSAGE);
			}
			case "1", "2", "3", "4", "5", "6", "7" ->
			{
				connection.test.lastSelectedAnswer = Integer.parseInt(data);
				connection.test.editSelectedAnswer();
			}
			case "SEND_ANSWER" ->
			{
				connection.test.receiveAnswer();
			}
		}
	}

	private static void processButonsFromMenu(String data)
	{
		switch(data)
		{
			case "START_TEST" ->
			{
				Deleter.requestForDeletion(SendDifferentMessages.ActiveMessageType.REQUEST_START_TEST_MESSAGE);
				SendDifferentMessages.send(SendDifferentMessages.ActiveMessageType.TEST_MESSAGE);
				cache.connection.setMenuStep(MenuStep.PROCESS_TESTING);
			}
			case "CANCEL_TEST" ->
			{
				Deleter.requestForDeletion(SendDifferentMessages.ActiveMessageType.REQUEST_START_TEST_MESSAGE);
			}
		}
	}

	public static void processMessage()
	{
		Update update = cache.update;
		checkConnections();

		//if(spamFilter()) return;

		GlobalCommand command;
		char firstSymbol = update.getMessage().getText().toCharArray()[0];
		if(firstSymbol == '/') // Если мы обрабатываем команду
		{
			try
			{
				String text = Connection.getMessageText();
				text = text.replaceAll("@ARC_Femida_bot", "");

				String[] params = text.split(" ");

				command = Enum.valueOf(GlobalCommand.class, params[0].substring(1).toUpperCase());
				processCommand(command, params);

			}catch(IllegalArgumentException e)
			{
				SendBotMessage.send(Properties.get("bot.say.command.don't_know"));
			}
		}
		else // Если это обычное сообщение
		{
			processText(update.getMessage().getText());
		}
	}

	public static void processCommand(GlobalCommand command, String[] param)
	{
		Connection connection = cache.connection;
		LoggerBot.logMethod("processCommand", Connection.getName(), command.toString());
		LoggerBot.log("");

		switch(connection.menuStep)
		{
			case MENU ->
			{
				switch(command)
				{
					case START ->
					{
						SendBotMessage.send(Properties.get("bot.say.command.start"));
					}
					case CANCEL ->
					{
						SendBotMessage.send(Properties.get("bot.say.command.cancel"));
					}
					case TESTING ->
					{
						SendDifferentMessages.send(SendDifferentMessages.ActiveMessageType.REQUEST_START_TEST_MESSAGE);
					}
					case VERIFICATION ->
					{
						connection.verification();
					}
					case MY_COMMANDS ->
					{
						if(connection.tgBdRelation == null)
						{
							SendBotMessage.send("Пройдите верификацию для определения уровня доступа /verification");
							return;
						}
						KeyboardBuilder builder = new KeyboardBuilder();

						TgBdRelation.Position position = connection.tgBdRelation.position;


						switch(connection.tgBdRelation.position)
						{
							case READER ->
							{
								sendReaderCommands();
							}
							case EDITOR ->
							{
								sendEditorCommands();
								sendReaderCommands();
							}
							case ADMIN ->
							{
								sendAdminCommands();
								sendEditorCommands();
								sendReaderCommands();
							}
						}
					}

					//case ABOUT -> {SendBotMessage.send(Properties.get("bot.say.command.about"), UserMessageLogFormat.USER_MESSAGE_TEXT, LogFormat.ONE_LINE, false);}
					//case VERSION -> {SendDifferentMessages.send(SendDifferentMessages.ActiveMessageType.VERSION_MESSAGE);}
					//case FEMIDA -> {SendDifferentMessages.send(SendDifferentMessages.ActiveMessageType.FEMIDA_ACTIONS);}
				}
			}
			case PROCESS_TESTING ->
			{
				switch(command)
				{
					case CANCEL ->
					{
						SendDifferentMessages.send(SendDifferentMessages.ActiveMessageType.REQUEST_CANCEL_TEST_MESSAGE);
						connection.activeMessages.requestCancelTest = connection.activeMessages.lastSentMessage;
					}
					default ->
					{
						//Callbacker.sendCallback("Во время теста нельзя взаимодействовать с командами бота!");
					}
				}
			}
		}
	}

	private static void sendReaderCommands()
	{
		KeyboardBuilder builder = new KeyboardBuilder();
		String s = "Уровень доступа: читатель.\n\n";
		s += "Вам доступны следующие команды: ";

		builder.addRow(Map.ofEntries(Map.entry("\uD83C\uDF0F Глобальный рейтинг", "global_rating")));
		builder.addRow(Map.ofEntries(Map.entry("\uD83D\uDD0E Поиск по рейтингу", "rating_search")));
		builder.addRow(Map.ofEntries(Map.entry("\uD83D\uDC64 Просмотр аккаунта", "account_search")));

		SendMessage messageWithKeyboard = builder.getMessageWithKeyboard();
		messageWithKeyboard.setChatId(Connection.getChatID());
		messageWithKeyboard.setText(s);

		SendBotMessage.send(messageWithKeyboard);
	}

	private static void sendEditorCommands()
	{
		KeyboardBuilder builder = new KeyboardBuilder();

		String s = "Уровень доступа: редактор.\n\n";
		s += "Вам доступны следующие команды: ";

		builder.addRow(Map.ofEntries(Map.entry("Добавить аккаунт", "add_account")));
		builder.addRow(Map.ofEntries(Map.entry("Изменить аккаунт", "edit_account")));
		builder.addRow(Map.ofEntries(Map.entry("Добавить соревнование", "add_competition")));
		builder.addRow(Map.ofEntries(Map.entry("Изменить соревнование", "edit_competition")));

		SendMessage messageWithKeyboard = builder.getMessageWithKeyboard();
		messageWithKeyboard.setChatId(Connection.getChatID());
		messageWithKeyboard.setText(s);

		SendBotMessage.send(messageWithKeyboard);
	}

	private static void sendAdminCommands()
	{
		KeyboardBuilder builder = new KeyboardBuilder();

		String s = "Уровень доступа: Админ.\n\n";
		s += "Вам доступны следующие команды: ";

		builder.addRow(Map.ofEntries(Map.entry("Выключить бота", "bot_turn_off")));
		builder.addRow(Map.ofEntries(Map.entry("Перечитать конфиг", "re_read_config")));
		builder.addRow(Map.ofEntries(Map.entry("Скачать логи", "download_logs")));

		SendMessage messageWithKeyboard = builder.getMessageWithKeyboard();
		messageWithKeyboard.setChatId(Connection.getChatID());
		messageWithKeyboard.setText(s);

		SendBotMessage.send(messageWithKeyboard);
	}

	public static void processText(String text)
	{
		SendBotMessage.send(Properties.get("bot.replies.to.text.don't_know"));
		LoggerBot.logMethod("processText", Connection.getName(), text);
	}

	public static void checkConnections()
	{
		boolean isFind = false;// Ищем пользователя в списке существующих

		String name = Connection.getName(); // Получаем ID пользователя

		for(Connection c : connections) // Ищем в уже зарегистрированных пользователях
		{
			if(Objects.equals(c.userName, name))
			{
				isFind = true;
				break;
			}
		}

		// Если не нашли - добавляем
		if(!isFind)
		{
			cache.connection = addConnection(name, MenuStep.MENU);
			String message = String.format("log: Новый пользователь {UserName = %s; ChatID = %s} (%s)", Connection.getName(), Connection.getChatID(), connections.size());
			System.out.println(message);
		}

		for(Connection c : connections)
		{
			if(Objects.equals(c.userName, name))
			{
				cache.connection = c;
				c.countMessages++;
			}
		}

	}


	public static Connection addConnection(String name, MenuStep menuStep)
	{
		Connection connection = new Connection(name, menuStep);
		UserDataToSave data = new UserDataToSave(connection);

		addConnection(data);
		connection.save();

		return connection;
	}

	public static Connection addConnection(UserDataToSave data)
	{
		Connection connection = data.getConnection();
		connections.add(connection);
		LoggerBot.logMethod("addConnection", data.getUserName());
		return connection;
	}

	public static boolean spamFilter()
	{
		Connection connection = cache.connection;

/*		if(!connection.startedCountdown)
		{
			startCountdown();
			connection.startedCountdown = true;
		}*/

		if(connection.isBanned)
		{
			String msg = cache.update.getMessage().getText().toLowerCase();
			if(msg.contains("прости") || msg.contains("извини") || msg.contains("не буду") || msg.contains("sorry") || msg.contains("сори") || msg.contains("сорри"))
			{
				cache.connection.isBanned = false;
				cache.connection.countMessages = 0;
				SendBotMessage.send("Ладно, так уж и быть, прощаю :))");
				return true;
			}
		}

		if(!connection.isBanned)
		{
			if(connection.countMessages >= 10) connection.isBanned = true;

			if(connection.isBanned) SendBotMessage.send("Я устал, хватит спамить и попроси прощения!");
		}
		if(connection.isBanned) return true;
		return false;
	}

	public enum MenuStep
	{MENU, PROCESS_TESTING}

	public enum GlobalCommand
	{CANCEL, TESTING, VERIFICATION, START, MY_COMMANDS}

}

