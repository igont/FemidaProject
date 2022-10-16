package main.java.BotPack.Senders;

import main.java.BotPack.DataTypes.Connection;
import main.java.BotPack.Processors.SendDifferentMessages;
import main.java.BotPack.Properties;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static main.java.Main.myBot;
import static main.java.BotPack.Processors.Processer.cache;


public class Callbacker
{
	public static final String DELIMITER = "-------------------------------------------------------------------------------------------------------------------------------------------------";

	public static Message sendNewCallback(String botMessage, SendDifferentMessages.ActiveMessageType messageType)
	{
		Message message = sendNewCallback(botMessage, UserMessageLogFormat.NOTHING, LogFormat.ONE_LINE, false);

		return message;
	}
	public static Message sendNewCallback(String botMessage)
	{
		return sendNewCallback(botMessage, UserMessageLogFormat.NOTHING,LogFormat.ONE_LINE,false);
	}
	public static Message sendNewCallback(String botMessage, UserMessageLogFormat userMessageLogFormat, LogFormat logFormat, boolean botMessageFirst)
	{

		SendMessage message = new SendMessage();
		message.setChatId(getChatID());
		message.setText(botMessage);

		return sendNewCallback(message, userMessageLogFormat, logFormat, botMessageFirst);
	}

	public static Message sendNewCallback(SendMessage botMessage, UserMessageLogFormat userMessageLogFormat, LogFormat logFormat, boolean botMessageFirst)
	{
		Update update = cache.update;
		Connection connection = cache.connection;
		botMessage.enableMarkdown(true);

		if(logFormat == LogFormat.LOG_DELIMITERING || logFormat == LogFormat.LOG_ONE_LINE || logFormat == LogFormat.LOG_NO_SPACES) // если нам не надо выводить сообщение в чат, но надо сохранить
		{
			System.out.println(getFormattedLine(botMessage.getText(), logFormat));
			return null;
		}

		if(logFormat == LogFormat.LOG_COLLECTOR)
		{
			if(Properties.get("send.in.collector").equals("false")) return null;

			botMessage.setChatId(System.getenv("COLLECTOR_CHAT_ID"));
		}

		String fromName = getName();

		String userMessage = getFormattedLine(getLogMessageText(userMessageLogFormat), logFormat);
		String botMessageStr = getFormattedLine(botMessage.getText(), logFormat);
		String menuStep;

		if(connection == null) menuStep = "???";
		else menuStep = connection.menuStep.toString();

		if(botMessageFirst) // если спрашивает бот и отвечает человек
		{
			System.out.printf("Bot ---> %s\n", botMessageStr);
			if(userMessageLogFormat != UserMessageLogFormat.NOTHING) System.out.printf("%s [%s] -> %s\n", fromName, menuStep, userMessage);
		}
		else // если спрашивает человек и отвечает бот
		{
			if(userMessageLogFormat != UserMessageLogFormat.NOTHING) System.out.printf("%s [%s] ---> %s\n", fromName, menuStep, userMessage);
			System.out.printf("Bot -> %s\n", botMessageStr);
		}
		System.out.println();

		try
		{
			Message m;

			if(logFormat != LogFormat.LOG_SIMPLE)
			{
				m = myBot.execute(botMessage);
				connection.activeMessages.lastSentMessage = m;
				return m;
			}
		}catch(TelegramApiException e)
		{
			throw new RuntimeException(e);
		}
		return null;
	}

	private static String getLogMessageText(UserMessageLogFormat userMessageLogFormat) // Что отобразить от лица юзера
	{
		Update update = cache.update;
		String out = "";
		switch(userMessageLogFormat)
		{
			case USER_MESSAGE_TEXT ->
			{
				out = update.getMessage().getText();
			}
			case BOT_QUESTION_FULL_TEXT ->
			{
				out = update.getCallbackQuery().getMessage().getText();
			}
			case BOT_QUESTION_ASK ->
			{
				out = update.getCallbackQuery().getMessage().getText().split("\n")[0];
			}
			case PRESSED_BUTTON_TEXT ->
			{
				out = update.getCallbackQuery().getData();
			}
			case PRESSED_BUTTON_AND_FIRST_LINE ->
			{
				out = update.getCallbackQuery().getMessage().getText().split("\n")[0] + "\n" + update.getCallbackQuery().getData();
			}
			case NOTHING ->
			{

			}
		}
		return out;
	}

	private static String getFormattedLine(String s, LogFormat logFormat)
	{
		switch(logFormat)
		{
			case NORMAL, LOG_SIMPLE ->
			{

			}
			case FIRST_LINE ->
			{
				if(s.contains("\n")) s = s.split("\n")[0] + " ...";
			}
			case ONE_LINE, LOG_ONE_LINE ->
			{
				s = s.replaceAll("\\n\\n", "; \t ");
			}
			case LOG_DELIMITERING ->
			{
				s = DELIMITER + "\n" + s.replaceAll("\\n\\n", "; \t ") + "\n" + DELIMITER;
			}
			case LOG_NO_SPACES ->
			{
				s = s.replaceAll("\\n\\n", "\n");
			}
		}
		return s;
	}

	public static void editMessage(SendMessage message, int id) // Заменяет последнее сообщение на новое
	{
		if(!message.getText().equals(""))
		{
			EditMessageText messageText = new EditMessageText();
			messageText.setMessageId(id);
			messageText.setText(message.getText());
			messageText.setChatId(getChatID());
			messageText.setParseMode(ParseMode.MARKDOWN);
			try
			{
				myBot.execute(messageText);
			}
			catch(TelegramApiException e)
			{
				throw new RuntimeException(e);
			}
		}

		if(message.getReplyMarkup() != null)
		{
			EditMessageReplyMarkup messageReplyMarkup = new EditMessageReplyMarkup();
			messageReplyMarkup.setReplyMarkup((InlineKeyboardMarkup) message.getReplyMarkup());
			messageReplyMarkup.setChatId(getChatID());
			messageReplyMarkup.setMessageId(id);
			try
			{
				myBot.execute(messageReplyMarkup);
			}
			catch(TelegramApiException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	public static String getName()
	{
		Update update = cache.update;
		String fromName = "???"; // Получаем Имя пользователя разными способами
		if(update.hasMessage()) fromName = update.getMessage().getFrom().getUserName();
		else
			if(update.hasCallbackQuery()) fromName = update.getCallbackQuery().getFrom().getUserName();

		return fromName;
	}

	public static String getMessageText()
	{
		Update update = cache.update;
		String fromText = "???"; // Получаем Текст пользователя разными способами
		if(update.hasMessage()) fromText = update.getMessage().getText();
		else
			if(update.hasCallbackQuery()) fromText = update.getCallbackQuery().getMessage().getText();

		fromText = fromText.replaceAll("@ARC_Femida_bot", "");

		return fromText;
	}

	public static Long getChatID()
	{
		Update update = cache.update;
		Long chatId = -1L; // Получаем ID пользователя разными способами
		if(update.hasMessage()) chatId = update.getMessage().getChatId();
		else
			if(update.hasCallbackQuery()) chatId = update.getCallbackQuery().getMessage().getChatId();

		return chatId;
	}

	public enum UserMessageLogFormat
	{USER_MESSAGE_TEXT, BOT_QUESTION_FULL_TEXT, BOT_QUESTION_ASK, PRESSED_BUTTON_TEXT, PRESSED_BUTTON_AND_FIRST_LINE, NOTHING}

	public enum LogFormat
	{
		//--------------------------------------------------------------Вывод в личные сообщения с человеком
		NORMAL, // Выводит сообщение как оно есть
		FIRST_LINE, // Выводит только первую строчку сообщения
		ONE_LINE, // Всё сообщение склеивается в одну строку
		//--------------------------------------------------------------Вывод в консоль
		LOG_SIMPLE, // Выводит сообщение как оно есть
		LOG_ONE_LINE, // Всё сообщение склеивается в одну строку
		LOG_DELIMITERING, // Обособляет сообщение палочками,
		LOG_NO_SPACES, // Из сообщения убираются пустые строки
		//--------------------------------------------------------------Вывод в Collector
		LOG_COLLECTOR
	}
}
