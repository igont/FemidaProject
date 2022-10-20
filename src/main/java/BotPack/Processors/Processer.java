package main.java.BotPack.Processors;

import main.java.BotPack.Builders.KeyboardBuilder;
import main.java.BotPack.DataTypes.Cache;
import main.java.BotPack.DataTypes.Connection;
import main.java.BotPack.DataTypes.UserDataToSave;
import main.java.BotPack.Properties;
import main.java.BotPack.Senders.SendBotMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

		switch(connection.menuStep)
		{
			case MENU ->
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
			case PROCESS_TESTING ->
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
						connection.test.editButtons();
					}
					case "SEND_ANSWER" ->
					{
						connection.test.receiveAnswer();
					}
				}
			}
			case CHECKING_VERSION ->
			{
				String text = "";

				switch(data)
				{
					case "0.1" ->
					{
						text = """
								- Первая пробная версия, бот умеет отвечать на сообщения пользователя такими же сообщениями.
																
								- 06.09.2022""";
					}
					case "0.2" ->
					{
						text = """
								- Добавлена система логирования --> Все действия в боте видны администратору.
																
								- Добавлена обработка всех видов команд, начинающихся с "/", но без реализации этих команд
								                             
								- Добавлена обработка кнопок ботом
																
								- 11.09.2022""";
					}
					case "0.3" ->
					{
						text = """
								- Добавлена реализация команды /testing
																
								- Запущенна система тестирования V1 (вопросы идут вперемешку, ответы каждый раз перемешиваются, под сообщением бота есть кнопки с ответами).

								- Добавлена возможность боту - редактировать сообщение после взаимодействия от пользователя
																
								- 12.09.2022""";
					}
					case "0.4" ->
					{
						text = """
								- Система тестирования улучшена до версии V2 (добавлена кнопка "Отправить" и возможность изменить выбранный ответ перед отправкой).
																
								- Добавлена возможность завершить тест досрочно командой */cancel*. После нее пользователь должен будет подтвердить выбор и ему придет результат тест.
															
								- Добавлена возможность боту - редактировать сообщение после взаимодействия от пользователя
																
								- Оптимизирована работа с несколькими пользователями одновременно, бот может работать независимо от количества активных пользователей
																
								- 14.09.2022""";
					}
					case "0.5" ->
					{
						text = """								
								- Система тестирования улучшена до версии V3 (после прохождения теста, пользователю показывается его результат и время прохождения. Это же сообщение дублируется администраторам)
																
								- Увеличена стабильность бота, тест не прервется при отправке случайных сообщений или команд
																
								- 22.09.2022""";
					}
					case "0.6" ->
					{
						text = """
								- *Актуальная версия*
																
								- Полностью переработана система взаимодействия с ботом, теперь "лишние сообщения" удаляются после отправки более новых
																
								- Повышена стабильность в случае работы с кнопками (можно нажимать и присылать что угодно, бот отреагирует правильно)
																
								- 24.09.2022""";
					}
					case "CANCEL" ->
					{
						Deleter.requestForDeletion(SendDifferentMessages.ActiveMessageType.VERSION_MESSAGE);
						connection.setMenuStep(MenuStep.MENU);
						return;
					}
				}
				text += "\n\nВсе прошедшие версии:";
				SendMessage message = new SendMessage();
				message.setText(text);
				message.setReplyMarkup(cache.connection.activeMessages.versionMessage.getReplyMarkup());
				message.setChatId(Connection.getChatID());
				Deleter.requestForDeletion(SendDifferentMessages.ActiveMessageType.VERSION_MESSAGE);
				SendBotMessage.send(message);
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
						switch(connection.tgBdRelation.position)
						{
							case READER ->
							{
								String s = "Уровень доступа: читатель.\n\n";
								s += "Вам доступны следующие команды: ";

								builder.addRow(Map.ofEntries(Map.entry("\uD83C\uDF0FГлобальный рейтинг", "global_rating")));
								builder.addRow(Map.ofEntries(Map.entry("\uD83D\uDD0EПоиск по рейтингу", "rating_search")));
								builder.addRow(Map.ofEntries(Map.entry("\uD83D\uDC64Просмотр аккаунта", "rating_search")));

								SendMessage messageWithKeyboard = builder.getMessageWithKeyboard();
								messageWithKeyboard.setChatId(Connection.getChatID());
								messageWithKeyboard.setText(s);

								SendBotMessage.send(messageWithKeyboard);
							}
							case EDITOR ->
							{
							}
						}
					}

					//case ABOUT -> {SendBotMessage.send(Properties.get("bot.say.command.about"), UserMessageLogFormat.USER_MESSAGE_TEXT, LogFormat.ONE_LINE, false);}
					//case VERSION -> {SendDifferentMessages.send(SendDifferentMessages.ActiveMessageType.VERSION_MESSAGE);}
					//case FEMIDA -> {SendDifferentMessages.send(SendDifferentMessages.ActiveMessageType.FEMIDA_ACTIONS);}
				}
			}
			case CHECKING_VERSION ->
			{
				switch(command)
				{
					case CANCEL ->
					{
						Deleter.requestForDeletion(SendDifferentMessages.ActiveMessageType.VERSION_MESSAGE);
						connection.setMenuStep(MenuStep.MENU);
					}
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

	public static void processText(String text)
	{
		if(cache.connection.nextMessage != null) switch(cache.connection.nextMessage)
		{
			case VERIFICATION_ID ->
			{
//				cache.connection.verification(text);
//				cache.connection.nextMessage = null;
//				return;
			}
		}
		SendBotMessage.send(Properties.get("bot.replies.to.text.don't_know"));
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
		connections.add(connection);

		return connection;
	}
	public static Connection addConnection(UserDataToSave data)
	{
		Connection connection = data.getConnection();
		connections.add(connection);
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
	{MENU, PROCESS_TESTING, CHECKING_VERSION}

	public enum GlobalCommand
	{CANCEL, TESTING, VERIFICATION, START, MY_COMMANDS}

}

