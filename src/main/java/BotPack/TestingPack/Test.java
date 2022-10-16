package main.java.BotPack.TestingPack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import main.java.BotPack.Builders.KeyboardBuilder;
import main.java.BotPack.DataTypes.Connection;
import main.java.BotPack.DataTypes.TestDataToSave;
import main.java.BotPack.FilesPack.FilesManipulator;
import main.java.BotPack.Processors.Deleter;
import main.java.BotPack.Processors.Processer;
import main.java.BotPack.Senders.Callbacker;
import main.java.BotPack.Senders.SendBotMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

import static main.java.BotPack.DataTypes.Connection.cloneListOfLists;
import static main.java.BotPack.Processors.SendDifferentMessages.ActiveMessageType.TEST_MESSAGE;
import static main.java.Main.myBot;
import static main.java.BotPack.Processors.Processer.cache;

public class Test
{
	private int currentQuestionNumber = 0;
	public int lastSelectedAnswer = 0;
	private int startTime = 0;
	private int endTime = 0;
	private int testTime = 0;
	private int maxGrade = 0;
	private int grade = 0;
	private Integer[] userAnswers;
	private List<Question> questions;
	Random random = new Random();
	private InlineKeyboardMarkup lastKeyboardMarkup;

	private InlineKeyboardMarkup getLastKeyboardMarkup()
	{
		InlineKeyboardMarkup newInlineKeyboardMarkup = new InlineKeyboardMarkup();
		newInlineKeyboardMarkup.setKeyboard(cloneListOfLists(lastKeyboardMarkup.getKeyboard()));
		return newInlineKeyboardMarkup;
	}

	public void setLastKeyboardMarkup(InlineKeyboardMarkup lastKeyboardMarkup)
	{
		this.lastKeyboardMarkup = lastKeyboardMarkup;
	}

	public void startTest()
	{
		clearTestData();
		readAllQuestions();
		shuffleQuestions();
		shuffleAnswers();
		userAnswers = new Integer[questions.size()];

		Calendar calendar = new GregorianCalendar();
		startTime = calendar.get(Calendar.SECOND) + calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60;

		sendNextQuestion();
	}

	private void sendNextQuestion()
	{
		Deleter.requestForDeletion(TEST_MESSAGE);
		sendQuestion();
	}

	public void receiveAnswer()
	{
		userAnswers[currentQuestionNumber] = lastSelectedAnswer;

		int addGrade = 0;
		if(questions.get(currentQuestionNumber).right == lastSelectedAnswer) // Если человек ответил правильно
		{
			addGrade = questions.get(currentQuestionNumber).answers.size() - 1;
		}
		grade += addGrade;
		System.out.println("Выбрано: " + lastSelectedAnswer);
		System.out.println("Правильный : " + questions.get(currentQuestionNumber).right);
		System.out.println("Начислено баллов : " + addGrade + " из " + (questions.get(currentQuestionNumber).answers.size() - 1));
		System.out.println();
		currentQuestionNumber++;
		sendNextQuestion();
	}

	private void sendQuestion()
	{
		if(questions.size() == currentQuestionNumber) // Если закончили тест
		{
			finnishTest();
			return;
		}
		Question question = questions.get(currentQuestionNumber);
		String s = "*" + "[" + (questions.size() - currentQuestionNumber) + "] " + question.question + "\n\n*";
		Map<String, String> row = new HashMap<>();
		for(int i = 0; i < question.answers.size(); i++)
		{
			s += (i + 1) + ") " + question.answers.get(i) + "\n\n";
			row.put((i + 1) + "", (i + 1) + "");
		}
		//Callbacker.sendNewCallback(s, TEST_MESSAGE);

		KeyboardBuilder keyboardBuilder = new KeyboardBuilder();
		keyboardBuilder.addRow(row);
		keyboardBuilder.addRow(Map.ofEntries(Map.entry("Отправить", "SEND_ANSWER")));

		SendBotMessage message = new SendBotMessage();
		message.msg = s;
		message.messageType = TEST_MESSAGE;
		message.inlineKeyboardMarkup = keyboardBuilder.getInlineKeyboardMarkup();

		message.send();

		setLastKeyboardMarkup(keyboardBuilder.getInlineKeyboardMarkup());
	}

	private void clearTestData() // Очищаем данные после предыдущего теста
	{
		currentQuestionNumber = 0;
		lastSelectedAnswer = 0;
		startTime = 0;
		endTime = 0;
		maxGrade = 0;
		grade = 0;
		userAnswers = null;
		questions = new ArrayList<>();
		lastKeyboardMarkup = new InlineKeyboardMarkup();
	}

	private void readAllQuestions() // Заполняем массив вопросов нужными данными
	{
		if(questions.isEmpty())
		{
			Question question = new Question();

			List<String> lines = FilesManipulator.read(Connection.ResourcesFiles.TEST_NAME);

			int numRight = 0;
			String first;

			for(String line : lines)
			{
				if(line.length() == 0)
				{
					questions.add(question);
					question = new Question();
					numRight = 0;
				}
				else
				{
					first = line.split(" ")[0];
					line = line.substring(first.indexOf(' ') + 1, line.length());
					switch(first)
					{
						case "-" ->
						{
							numRight++;
							question.answers.add(line.substring(2));
							maxGrade++;
						}
						case "+" ->
						{
							numRight++;
							question.right = numRight;
							question.answers.add(line.substring(2));
						}
						default ->
						{
							question.question = line;
						}
					}
				}
			}
		}
	}

	public void editButtons()
	{
		Connection connection = cache.connection;

		SendMessage message = connection.getCopyOfLastBotQuestionMessage(); // Получаем клонированное сообщение бота c вопросом и кнопками
		InlineKeyboardMarkup oldReplyMarkup = getLastKeyboardMarkup();

		oldReplyMarkup.getKeyboard().get(0).get(lastSelectedAnswer - 1).setText("(" + lastSelectedAnswer + ")");
		message.setReplyMarkup(oldReplyMarkup);

		EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
		editMessageReplyMarkup.setReplyMarkup((InlineKeyboardMarkup) message.getReplyMarkup());
		editMessageReplyMarkup.setMessageId(connection.activeMessages.lastBotQuestionMessage.getMessageId());
		editMessageReplyMarkup.setChatId(Callbacker.getChatID());

		try
		{
			myBot.execute(editMessageReplyMarkup);
		}catch(TelegramApiException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void shuffleQuestions()
	{
		Collections.shuffle(questions);
	}

	private void shuffleAnswers()
	{
		for(Question question : questions)
		{
			List<String> newAnswers = new ArrayList<>();
			int num;

			boolean taken = false;
			while(!question.answers.isEmpty())
			{
				num = random.nextInt(0, question.answers.size());
				newAnswers.add(question.answers.get(num));
				question.answers.remove(num);

				if(num == 0 && !taken)
				{
					taken = true;
					question.right = newAnswers.size();
					//question.question +=" "+ question.right;
				}
			}
			question.answers = newAnswers;
		}
	}

	private void finnishTest()
	{
		Calendar calendar = new GregorianCalendar();
		endTime = calendar.get(Calendar.SECOND) + calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60;

		testTime = endTime - startTime;

		int resultGrade = Math.round((float)grade / (float)maxGrade * 100);
		SendBotMessage.send("Тест завершен, Результат: " + resultGrade + "%, Времени потрачено: " + testTime + " сек");

		System.out.println("----------------");
		System.out.println("Максимум баллов: " + maxGrade);
		System.out.println("Набрано баллов: " + grade);
		cache.connection.setMenuStep(Processer.MenuStep.MENU);

		TestDataToSave data = new TestDataToSave();
		data.testName = "Обычный тест";
		data.testTime = testTime;
		data.questions = questions;
		data.userAnswers = userAnswers;
		data.userName = Callbacker.getName();
		data.date = new Date();
		data.totalGrade = resultGrade;

		List<TestDataToSave> datas = new ArrayList<>();
		List<String> strings = FilesManipulator.read(Connection.ResourcesFiles.TEST_LOG);
		String s = String.join("", strings);

		Gson gson = new GsonBuilder().setLenient().create();
		datas = gson.fromJson(s,datas.getClass());

		if(datas == null)
		{
			datas = new ArrayList<>();
		}
		datas.add(data);

		FilesManipulator.write(datas, Connection.ResourcesFiles.TEST_LOG, true, true);
	}

	public class Question
	{
		public String question; // Вопрос самого вопроса
		public List<String> answers; // Список ответов
		public int right; // Номер правильного
		public InlineKeyboardMarkup markup;

		Question()
		{
			answers = new ArrayList<>();
		}

		@Override
		public String toString()
		{
			String out = "";
			out += "*" + question + "*" + "\n\n";
			int ansNum = 1;

			for(String ans : answers)
			{
				out += ansNum++ + ") " + ans + "\n\n";
			}
			return out;
		}
	}
}
