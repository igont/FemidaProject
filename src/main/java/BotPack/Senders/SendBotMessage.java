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
	private String msg;
	private InlineKeyboardMarkup inlineKeyboardMarkup;
	private SendDifferentMessages.ActiveMessageType messageType;

	public void setText(String msg)
	{
		this.msg = msg;
	}

	public void setInlineKeyboardMarkup(InlineKeyboardMarkup inlineKeyboardMarkup)
	{
		this.inlineKeyboardMarkup = inlineKeyboardMarkup;
	}

	public void setMessageType(SendDifferentMessages.ActiveMessageType messageType)
	{
		this.messageType = messageType;
	}

	public Message sendPreparedMessage()
	{
		SendMessage message = new SendMessage();
		if(!msg.isEmpty()) message.setText(msg);
		if(!(inlineKeyboardMarkup == null)) message.setReplyMarkup(inlineKeyboardMarkup);
		message.setChatId(Connection.getChatID());
		message.setParseMode(ParseMode.MARKDOWN);

		Message result;

		result = send(message);
		System.out.println(result);

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
		return result;
	}

	public static Message send(String s) // Отправляет текст в чат
	{
		SendMessage message = new SendMessage();
		message.setChatId(Connection.getChatID());
		message.setParseMode(ParseMode.MARKDOWN);
		message.setText(s);

		return send(message);
	}

	public static Message send(SendMessage message, SendDifferentMessages.ActiveMessageType...messageType)
	{
		Message result;
		try
		{
			result = myBot.execute(message);
			cache.connection.activeMessages.lastSentMessage = result;
			LoggerBot.logChatMessage(result);

		}catch(TelegramApiException e)
		{
			throw new RuntimeException(e);
		}
		if(messageType.length>0)
		{

		}
		return result;
	}
}
