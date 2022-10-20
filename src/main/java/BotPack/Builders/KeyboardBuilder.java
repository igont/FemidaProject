package main.java.BotPack.Builders;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyboardBuilder
{
	private InlineKeyboardMarkup inlineKeyboardMarkup;
	private List<List<InlineKeyboardButton>> rows;
	public KeyboardBuilder()
	{
		inlineKeyboardMarkup = new InlineKeyboardMarkup();
		rows = new ArrayList<>();
	}
	public KeyboardBuilder addRow(Map<String,String> map)
	{
		List<InlineKeyboardButton> row1 = new ArrayList<>();
		InlineKeyboardButton button;

		for(Map.Entry<String, String> entry : map.entrySet())
		{
			String name = entry.getKey();
			String callback = entry.getValue();

			row1.add(newButton(name, callback));
		}
		rows.add(row1);
		return this;
	}
	public InlineKeyboardMarkup getInlineKeyboardMarkup()
	{
		inlineKeyboardMarkup.setKeyboard(rows);
		return inlineKeyboardMarkup;
	}
	public SendMessage getMessageWithKeyboard()
	{
		SendMessage message = new SendMessage();
		message.setParseMode(ParseMode.MARKDOWN);
		message.setReplyMarkup(getInlineKeyboardMarkup());
		return message;
	}
	public static InlineKeyboardButton newButton(String name, String callback)
	{
		InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
		inlineKeyboardButton.setText(name);
		inlineKeyboardButton.setCallbackData(callback);

		return inlineKeyboardButton;
	}
}
