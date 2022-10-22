package main.java.BotPack.DataTypes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import main.java.BotPack.FilesPack.FilesManipulator;
import main.java.BotPack.Processors.Processer;
import main.java.BotPack.Senders.LoggerBot;
import main.java.BotPack.Senders.SendBotMessage;
import main.java.BotPack.TestingPack.Test;
import main.java.Excel.RefereeAccount;
import main.java.Excel.SQLReal;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.java.BotPack.Processors.Processer.cache;

public class Connection
{
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

	public Connection()
	{
		activeMessages = new ActiveMessages();
	}

	public void verification()
	{
		LoggerBot.logMethod("verification",getName());
		Map<String, TgBdRelation> map = new HashMap<>();

		String verificationStrings = String.join("", FilesManipulator.read(FilesManipulator.ResourcesFiles.VERIFICATION));

		Gson gson = new GsonBuilder().setLenient().create();
		map = gson.fromJson(verificationStrings, map.getClass());

		String s1 = gson.toJson(map.get(getName()));
		tgBdRelation = gson.fromJson(s1, TgBdRelation.class);

		RefereeAccount referee = new RefereeAccount();
		try
		{
			referee = SQLReal.getRefereeByID(tgBdRelation.id);
		}catch(SQLException e)
		{
			throw new RuntimeException(e);
		}catch(NullPointerException e) // Если пользователя нет в файле Verification
		{
			tgBdRelation = new TgBdRelation(-1, TgBdRelation.Position.READER);
		}

		LoggerBot.logMethod("setTgBdRelation",getName(),String.valueOf(tgBdRelation.id),tgBdRelation.position.toString());

		String verificationCallback = "";
		if(tgBdRelation.id > 0) // Если человек есть в базе Femida
		{
			verificationCallback += "Добро пожаловать, " + referee.sName + " " + referee.fName + " " + referee.mName + "\n\n";
		}
		else
		{
			verificationCallback += "Вашего аккаунта нет в Базе данных.\n\n";
		}

		switch(tgBdRelation.position)
		{
			case READER ->
			{
				verificationCallback += "У вас есть доступ к просмотру базы Femida";
			}
			case EDITOR ->
			{
				verificationCallback += "У вас есть права на редактирование базы Femida";
			}
		}
		LoggerBot.log("");
		save();
		SendBotMessage.send(verificationCallback);
	}

	public void save()
	{
		FilesManipulator.write(new UserDataToSave(this), FilesManipulator.ResourcesFiles.SAVED_DATA, true, true);
	}

	public void setMenuStep(Processer.MenuStep newStep)
	{
		LoggerBot.logMethod("setMenuStep",userName,menuStep.toString(),newStep.toString());
		if(menuStep == newStep) return;
		save();
		this.menuStep = newStep;
	}

	public enum NextMessage
	{VERIFICATION_ID}
}
