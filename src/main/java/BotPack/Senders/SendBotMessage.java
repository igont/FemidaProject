package main.java.BotPack.Senders;


import main.java.BotPack.DataTypes.Connection;
import main.java.BotPack.Processors.SendDifferentMessages;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;

import static main.java.BotPack.Processors.Processer.cache;
import static main.java.Main.myBot;

public class SendBotMessage
{
	private String msg;
	private InlineKeyboardMarkup inlineKeyboardMarkup;
	private SendDifferentMessages.ActiveMessageType messageType;
	private File sendFile;

	public void setText(String msg)
	{
		this.msg = msg;
	}

	public void setSendFile(File sendFile)
	{
		this.sendFile = sendFile;
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
		if(sendFile != null)
		{
			return sendFileMessage();
		}

		SendMessage message = new SendMessage();
		if(!msg.isEmpty()) message.setText(msg);
		if(!(inlineKeyboardMarkup == null)) message.setReplyMarkup(inlineKeyboardMarkup);
		message.setChatId(Connection.getChatID());
		message.setParseMode(ParseMode.MARKDOWN);

		Message result;

		result = send(message);
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

	private Message sendFileMessage()
	{
		SendDocument sendDocument = new SendDocument();
		sendDocument.setChatId(Connection.getChatID());
		sendDocument.setDocument(new InputFile(sendFile));

		return send(sendDocument);
	}

	private Message send(SendDocument sendDocument)
	{
		Message result;
		try
		{
			result = myBot.execute(sendDocument);
			cache.connection.activeMessages.lastSentMessage = result;
			LoggerBot.logChatMessage(result);

		}catch(TelegramApiException e)
		{
			throw new RuntimeException(e);
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

		return result;
	}


}
