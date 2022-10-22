package main.java.BotPack.Senders;

import main.java.BotPack.DataTypes.MessageDataToSave;
import main.java.BotPack.FilesPack.FilesManipulator;
import main.java.BotPack.Processors.Processer;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.Date;

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

		FilesManipulator.write(messageDataToSave, FilesManipulator.ResourcesFiles.MESSAGE_LOG, true, false);
	}
	public static void log(String var, String val)
	{
		String s = var + " = [" + val + "]";
		FilesManipulator.write(s, FilesManipulator.ResourcesFiles.SYSTEM_LOG,false);
	}
	public static void log(String var, int val)
	{
		String s = var + " = [" + val + "]";
		FilesManipulator.write(s, FilesManipulator.ResourcesFiles.SYSTEM_LOG,false);
	}
	public static void log(String s)
	{
		FilesManipulator.write(s, FilesManipulator.ResourcesFiles.SYSTEM_LOG,false);
	}
	public static void logMethod(String method, int...variables)
	{
		String s = Arrays.toString(variables);
		String[] strings = s.substring(1,s.length()-1).split(", ");

		logMethod(method, strings);
	}
	public static void logMethod(String method, String...variables)
	{
		String s = method + "() <--";
		for(String var: variables)
		{
			s += String.format(" [%s],",var);
		}
		s = s.substring(0, s.length() - 1);
		FilesManipulator.write(s, FilesManipulator.ResourcesFiles.SYSTEM_LOG,false);
	}
	public static void logMethodReturn(String method, String...variables)
	{
		String s = method + "() -->";
		for(String var: variables)
		{
			s += String.format(" [%s],",var);
		}
		s = s.substring(0, s.length() - 1);
		FilesManipulator.write(s, FilesManipulator.ResourcesFiles.SYSTEM_LOG,false);
	}

}
