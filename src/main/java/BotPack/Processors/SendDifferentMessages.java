package main.java.BotPack.Processors;

import main.java.BotPack.Builders.KeyboardBuilder;
import main.java.BotPack.DataTypes.Connection;
import main.java.BotPack.Properties;
import main.java.BotPack.Senders.SendBotMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static main.java.BotPack.Processors.Processer.cache;

public class SendDifferentMessages
{
	public static void send(ActiveMessageType activeMessageType)
	{
		Deleter.requestForDeletion(activeMessageType); // Удаляем сообщение данного типа, если оно уже было отправлено

		switch(activeMessageType)
		{
			case REQUEST_START_TEST_MESSAGE -> requestStartTest();
			case TEST_MESSAGE -> cache.connection.test.startTest();
			case REQUEST_CANCEL_TEST_MESSAGE -> cancelTest();
			case FEMIDA_ACTIONS -> femidaActions();
		}
	}
	private static void femidaActions()
	{
		String s;
		if(cache.connection.femidaID == null)
		{
			SendBotMessage.send("Для редактирования Рейтинговой базы Femida, вам необходимо пройти верификацию /verification");
			cache.connection.femidaID = "Неизвестный пользователь";
		}
		System.out.println(cache.connection.femidaID);
		if(Objects.equals(cache.connection.femidaID.split(" ")[0], "Редактор"))
		{
			SendBotMessage.send("Для вашего уровня \"Редактор\" Доступны следующие команды:");

		}else
		{
			SendBotMessage.send("Для вашего уровня \"Пользователь\" Доступны следующие команды:");

		}
	}
	private static void requestStartTest()
	{
		KeyboardBuilder keyboardBuilder = new KeyboardBuilder();
		Connection connection = cache.connection;

		Map<String, String> map = new HashMap<>();
		map.put("Начать тест!", "Start_test");
		map.put("Отмена", "Cancel_test");

		InlineKeyboardMarkup inlineKeyboardMarkup = keyboardBuilder.addRow(map).getInlineKeyboardMarkup();

		SendMessage message = new SendMessage();
		message.setReplyMarkup(inlineKeyboardMarkup);
		message.setText(Properties.get("bot.say.command.testing"));
		message.setChatId(Connection.getChatID());

		SendBotMessage.send(message);
		connection.activeMessages.requestStartTest = connection.activeMessages.lastSentMessage;
	}



	private static void cancelTest()
	{
		KeyboardBuilder keyboardBuilder = new KeyboardBuilder();
		SendMessage message = new SendMessage();
		InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
		Map<String, String> map = new HashMap<>();

		map.put("Завершить", "Cancel_Started_Test");
		map.put("Продолжить", "Continue");

		inlineKeyboardMarkup = keyboardBuilder.addRow(map).getInlineKeyboardMarkup();
		message.setReplyMarkup(inlineKeyboardMarkup);
		message.setChatId(Connection.getChatID());
		System.out.println(Properties.get("bot.say.warning_сancel_started_test"));
		message.setText(Properties.get("bot.say.warning_сancel_started_test"));

		SendBotMessage.send(message);
	}


	public enum ActiveMessageType
	{
		REQUEST_START_TEST_MESSAGE,                 // Запрос на начало теста
		TEST_MESSAGE,                               // Сообщение в процессе тестирования
		REQUEST_CANCEL_TEST_MESSAGE,                // Сообщение об отмене тестирования
		LAST_SENT_MESSAGE,                           // Последнее присланное сообщение
		FEMIDA_ACTIONS,
	}
}
