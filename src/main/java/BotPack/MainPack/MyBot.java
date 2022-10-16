package main.java.BotPack.MainPack;


import main.java.BotPack.Processors.Processer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class MyBot extends TelegramLongPollingBot
{
	@Override
	public String getBotUsername()
	{
		return System.getenv("BOT_NAME");
	}

	@Override
	public String getBotToken()
	{
		return System.getenv("BOT_TOKEN");
	}

	@Override
	public void onUpdateReceived(Update update)
	{
		Processer.processUpdate(update);
	}
}
