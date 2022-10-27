package main.java.BotPack.DataTypes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import main.java.BotPack.FilesPack.MyFile;
import main.java.BotPack.Processors.Processer;
import main.java.BotPack.Senders.LoggerBot;
import main.java.BotPack.Senders.SendBotMessage;
import main.java.BotPack.TestingPack.Test;
import main.java.Excel.RefereeAccount;
import main.java.Excel.SQLPack.SQLReal;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.sql.SQLException;
import java.util.*;

import static main.java.BotPack.FilesPack.ResourcesFiles.SAVED_DATA;
import static main.java.BotPack.FilesPack.ResourcesFiles.VERIFICATION;
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
		LoggerBot.logMethod("verification", getName());
		Map<String, TgBdRelation> map = new HashMap<>();

		String verificationStrings = new MyFile(VERIFICATION).readOneLine();

		Gson gson = new GsonBuilder().setLenient().create();
		map = gson.fromJson(verificationStrings, map.getClass());

		if(map == null) // если в файле ничего нет
		{
			tgBdRelation = new TgBdRelation(-1, TgBdRelation.Position.READER);
			System.out.println();
			System.out.println("Необходимо заполнить файл с верификацией пользователей!!!");
			System.out.println("Иначе никто в телеграмм боте не будет обладать правами админа");
			System.out.print("Сделать это сейчас? (y/n) ");
			Scanner in = new Scanner(System.in);

			if(in.nextLine().equalsIgnoreCase("y"))
			{
				System.out.println();
				System.out.println("Для выхода нажмите Q");
				System.out.println("Введите имя аккаунта пользователя ТГ, который может вносить правки в Базу (например EgOnt)");
				System.out.println("А затем ID этого человека в базе данных Femida");
				System.out.println("");
				System.out.println("Для вызова этого диалога еще раз удалите файл: " + new MyFile(VERIFICATION).getPath());
				System.out.println();
				String s;
				map = new HashMap<>();


				System.out.print("Имя: ");
				String name = in.nextLine();

				System.out.print("id (например 21) (по умолчанию id = -1): ");
				int id = in.nextInt();

				map.put(name, new TgBdRelation(id, TgBdRelation.Position.EDITOR));

				new MyFile(VERIFICATION).append(map);

				System.out.println("Сохранено: " + name + ": " + gson.toJson(map.get(name)));
				System.out.println("Остальное сам вбей в вышеуказанном файле");
			}
		}
		else
		{
			String s1 = gson.toJson(map.get(getName()));
			tgBdRelation = gson.fromJson(s1, TgBdRelation.class);
		}


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

		LoggerBot.logMethod("setTgBdRelation", getName(), String.valueOf(tgBdRelation.id), tgBdRelation.position.toString());

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
			case ADMIN ->
			{
				verificationCallback += "Вашим правам нет границ, великий Админ";
			}
		}
		LoggerBot.log("");
		save();
		SendBotMessage.send(verificationCallback);
	}

	public void save()
	{
		new MyFile(SAVED_DATA).write(new UserDataToSave(this));
	}

	public void setMenuStep(Processer.MenuStep newStep)
	{
		LoggerBot.logMethod("setMenuStep", userName, menuStep.toString(), newStep.toString());
		LoggerBot.log("");

		if(menuStep == newStep) return;
		this.menuStep = newStep;
		save();
	}

	public enum NextMessage
	{VERIFICATION_ID}
}
