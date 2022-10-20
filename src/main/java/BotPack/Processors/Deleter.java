package main.java.BotPack.Processors;

import main.java.BotPack.DataTypes.Connection;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static main.java.BotPack.Processors.Processer.cache;
import static main.java.BotPack.DataTypes.Connection.getChatID;
import static main.java.Main.myBot;



public class Deleter
{
	public static void requestForDeletion(SendDifferentMessages.ActiveMessageType type) // Все сообщения удаляются через этот метод
	{
		Connection connection = cache.connection;
		int id;
		switch(type)
		{
			case REQUEST_START_TEST_MESSAGE ->
			{
				if(connection.activeMessages.requestStartTest != null) // Если нам есть что удалять
				{
					id = connection.activeMessages.requestStartTest.getMessageId();
					connection.activeMessages.requestStartTest = null;
					deleteBYID(id);
				}
			}
			case TEST_MESSAGE ->
			{
				if(connection.activeMessages.lastBotQuestionMessage != null) // Если нам есть что удалять
				{
					id = connection.activeMessages.lastBotQuestionMessage.getMessageId();
					connection.activeMessages.lastBotQuestionMessage = null;
					deleteBYID(id);
				}
			}
			case REQUEST_CANCEL_TEST_MESSAGE ->
			{
				if(connection.activeMessages.requestCancelTest != null) // Если нам есть что удалять
				{
					id = connection.activeMessages.requestCancelTest.getMessageId();
					connection.activeMessages.requestCancelTest = null;
					deleteBYID(id);
				}
			}
			case VERSION_MESSAGE ->
			{
				if(connection.activeMessages.versionMessage != null) // Если нам есть что удалять
				{
					id = connection.activeMessages.versionMessage.getMessageId();
					connection.activeMessages.versionMessage = null;
					deleteBYID(id);
				}
			}
			case LAST_SENT_MESSAGE ->
			{
				if(connection.activeMessages.lastSentMessage != null) // Если нам есть что удалять
				{
					id = connection.activeMessages.lastSentMessage.getMessageId();
					connection.activeMessages.lastSentMessage = null;
					deleteBYID(id);
				}
			}
		}
	}

	private static void deleteBYID(int id) // Просто удаляет сообщение по ID
	{
		DeleteMessage deleteMessage = new DeleteMessage();
		deleteMessage.setChatId(getChatID());
		deleteMessage.setMessageId(id);

		try
		{
			myBot.execute(deleteMessage);
		}catch(TelegramApiException e)
		{
			throw new RuntimeException(e);
		}
	}
}
