package main.java.BotPack.Senders;

import main.java.BotPack.DataTypes.MessageDataToSave;
import main.java.BotPack.FilesPack.MyFile;
import main.java.BotPack.Processors.Processer;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Date;

import static main.java.BotPack.FilesPack.ResourcesFiles.MESSAGE_LOG;
import static main.java.BotPack.FilesPack.ResourcesFiles.SYSTEM_LOG;

public class LoggerBot
{
	public static void logChatMessage(Message message)
	{
		MessageDataToSave messageDataToSave = new MessageDataToSave();
		messageDataToSave.date = new Date();
		messageDataToSave.botText = message.getText();

		if(Processer.cache.update.hasMessage())
			messageDataToSave.userText = Processer.cache.update.getMessage().getText();
		else if(Processer.cache.update.hasCallbackQuery())
			messageDataToSave.userText = Processer.cache.update.getCallbackQuery().getData();

		new MyFile(MESSAGE_LOG).append(messageDataToSave);
	}
	public static void log(Object var, Object val)
	{
		String s = var + " = [" + val + "]";
		new MyFile(SYSTEM_LOG).append(s);
	}

	public static void log(Object s)
	{
		new MyFile(SYSTEM_LOG).append(s.toString());
	}
	public static void logMethod(String method, Object...variables)
	{
		String s = method + "() <--";
		for(Object var: variables)
		{
			s += String.format(" [%s],",var.toString());
		}
		s = s.substring(0, s.length() - 1);

		new MyFile(SYSTEM_LOG).append(s);
	}
	public static void logMethodReturn(String method, Object...variables)
	{
		String s = method + "() -->";
		for(Object var: variables)
		{
			s += String.format(" [%s],",var.toString());
		}
		s = s.substring(0, s.length() - 1);

		new MyFile(SYSTEM_LOG).append(s);
	}

}
