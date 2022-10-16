package main.java.BotPack.DataTypes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import main.java.BotPack.FilesPack.FilesManipulator;
import main.java.BotPack.Processors.Processer;
import main.java.BotPack.Senders.Callbacker;
import main.java.BotPack.Senders.SendBotMessage;
import main.java.BotPack.TestingPack.Test;
import main.java.Config;
import main.java.Excel.RefereeAccount;
import main.java.Excel.SQLReal;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.*;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.*;

public class Connection
{
	public static class ActiveMessages
	{
		public Message lastBotQuestionMessage;      // Сообщение с вопросом и кнопками для ответов
		public Message requestStartTest;            // Запрос на прохождение теста
		public Message requestCancelTest;           // Запрос на завершение теста
		public Message versionMessage;              // Сообщение о просмотре версии
		public Message lastSentMessage;             // Просто последнее сообщение бота
	}

	public NextMessage nextMessage;
	public int countMessages = 0;
	public boolean isBanned = false;
	public ActiveMessages activeMessages;
	public String userName;
	public Processer.MenuStep menuStep;
	public String femidaID;
	public Test test = new Test();
	public TgBdRelation tgBdRelation;

	public SendMessage getCopyOfLastBotQuestionMessage()
	{
		InlineKeyboardMarkup replyMarkup = activeMessages.lastBotQuestionMessage.getReplyMarkup();

		InlineKeyboardMarkup newInlineKeyboardMarkup = new InlineKeyboardMarkup();
		newInlineKeyboardMarkup.setKeyboard(cloneListOfLists(replyMarkup.getKeyboard()));

		SendMessage message = new SendMessage();
		message.setChatId(activeMessages.lastBotQuestionMessage.getChatId());
		message.setText(activeMessages.lastBotQuestionMessage.getText());
		message.setReplyMarkup(newInlineKeyboardMarkup);

		return message;
	}

	public static List<List<InlineKeyboardButton>> cloneListOfLists(List<List<InlineKeyboardButton>> keyboard)
	{

		List<List<InlineKeyboardButton>> out = new ArrayList<>();
		for(List<InlineKeyboardButton> inner : keyboard)
		{
			List<InlineKeyboardButton> row = new ArrayList<>();
			for(InlineKeyboardButton butt : inner)
			{
				InlineKeyboardButton b = new InlineKeyboardButton();
				b.setText(butt.getText());
				b.setCallbackData(butt.getCallbackData());

				row.add(b);
			}
			out.add(row);
		}
		return out;
	}

	public Connection(String userName, Processer.MenuStep menuStep)
	{
		this.userName = userName;
		this.menuStep = menuStep;
		activeMessages = new ActiveMessages();
	}

	public void verification()
	{
		Map<String, TgBdRelation> map = new HashMap<>();

		List<String> strings = FilesManipulator.read(ResourcesFiles.VERIFICATION);
		String s = String.join("", strings);

		Gson gson = new GsonBuilder().setLenient().create();
		map = gson.fromJson(s,map.getClass());

		String s1 = gson.toJson(map.get(Callbacker.getName()));
		tgBdRelation = gson.fromJson(s1, TgBdRelation.class);

		RefereeAccount referee;
		try
		{
			referee = SQLReal.getRefereeByID(tgBdRelation.id);
		}catch(SQLException e)
		{
			throw new RuntimeException(e);
		}
		switch(tgBdRelation.position)
		{
			case READER ->
			{
				SendBotMessage.send("У вас есть доступ к просмотру базы Femida");
			}
			case EDITOR ->
			{
				String s2 = "Добро пожаловать, " + referee.sName + " " + referee.fName + " " + referee.mName;
				SendBotMessage.send(s2);
				SendBotMessage.send("У вас есть права на редактирование базы Femida");
			}
		}
	}

	public void save()
	{
		save(new UserDataToSave(this));
	}

	public void save(UserDataToSave userData)
	{
		Gson gson = new Gson();
		String s = gson.toJson(userData) + "\n";
		Path path = getDifferentFilePath(ResourcesFiles.SAVED_DATA);

		try
		{
			Files.write(path, s.getBytes(), StandardOpenOption.APPEND);
		}catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public enum ResourcesFiles
	{
		CONFIG,
		SAVED_DATA,
		TEST_NAME,
		TEST_LOG,
		VERIFICATION
	}

	public static Path getDifferentFilePath(ResourcesFiles type)
	{
		Path dataFilePath;
		switch(type)
		{
			case CONFIG ->
			{
				dataFilePath = Path.of(Config.resourcesPath + "config.json");
			}
			case SAVED_DATA ->
			{
				dataFilePath = Path.of(Config.resourcesPath + "saved_data.txt");
			}
			case TEST_NAME ->
			{
				dataFilePath = Path.of(Config.resourcesPath + Config.testName);
			}
			case TEST_LOG ->
			{
				dataFilePath = Path.of(Config.resourcesPath + "test_log.json");
			}
			case VERIFICATION ->
			{
				dataFilePath = Path.of(Config.resourcesPath + "verification.json");
			}
			default ->
			{
				dataFilePath = null;
			}
		}
		if(!Files.exists(dataFilePath))
		{
			try
			{
				Files.createDirectories(Path.of(Config.resourcesPath));
				Files.createFile(dataFilePath);
			}catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		return dataFilePath;
	}

	public void setMenuStep(Processer.MenuStep step)
	{
		if(menuStep == step) return;

		this.menuStep = step;
		Callbacker.sendNewCallback("Change step", Callbacker.UserMessageLogFormat.NOTHING, Callbacker.LogFormat.LOG_SIMPLE, false);
	}

	public enum NextMessage
	{VERIFICATION_ID}
}
