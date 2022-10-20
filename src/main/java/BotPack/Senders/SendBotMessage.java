package main.java.BotPack.Senders;


import main.java.BotPack.DataTypes.Connection;
import main.java.BotPack.Processors.SendDifferentMessages;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static main.java.BotPack.Processors.Processer.cache;
import static main.java.Main.myBot;

public class SendBotMessage
{
	public String msg;
	public InlineKeyboardMarkup inlineKeyboardMarkup;
	public SendDifferentMessages.ActiveMessageType messageType;

	public void send()
	{
		SendMessage message = new SendMessage();
		if(!msg.isEmpty()) message.setText(msg);
		if(!(inlineKeyboardMarkup == null)) message.setReplyMarkup(inlineKeyboardMarkup);
		message.setChatId(Connection.getChatID());
		message.setParseMode(ParseMode.MARKDOWN);

		Message result;
		try
		{
			result = myBot.execute(message);
		}catch(TelegramApiException e)
		{
			throw new RuntimeException(e);
		}

		switch(messageType)
		{
			case REQUEST_START_TEST_MESSAGE ->
			{
				cache.connection.activeMessages.requestStartTest = result;
			}
			case TEST_MESSAGE ->
			{
				cache.connection.activeMessages.lastBotQuestionMessage = result;
			}
			case REQUEST_CANCEL_TEST_MESSAGE ->
			{
				cache.connection.activeMessages.requestCancelTest = result;
			}
			case VERSION_MESSAGE ->
			{
				cache.connection.activeMessages.versionMessage = result;
			}
			case LAST_SENT_MESSAGE ->
			{
				cache.connection.activeMessages.lastSentMessage = result;
			}
			case FEMIDA_ACTIONS ->
			{

			}
		}
	}

	public static void send(String s)
	{
		SendMessage message = new SendMessage();
		message.setChatId(Connection.getChatID());
		message.setParseMode(ParseMode.MARKDOWN);
		message.setText(s);

		try
		{
			myBot.execute(message);
		}catch(TelegramApiException e)
		{
			throw new RuntimeException(e);
		}
	}
	public static void send(SendMessage message)
	{
		try
		{
			myBot.execute(message);
		}catch(TelegramApiException e)
		{
			throw new RuntimeException(e);
		}
	}
}
