package main.java.BotPack.FilesPack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import main.java.BotPack.DataTypes.Connection;
import main.java.BotPack.Senders.LoggerBot;
import main.java.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static main.java.BotPack.FilesPack.FileType.DIRECTORY;
import static main.java.BotPack.FilesPack.FileType.FILE;
import static main.java.BotPack.FilesPack.LocalJsonFormat.PRETTY;

public class MyFile
{
	private Path path;

	private FileType fileType;

	private LocalJsonFormat localJsonFormat;

	public MyFile(Path path)
	{
		this.path = path;
		this.localJsonFormat = PRETTY;
		this.fileType = FILE;
		createIfNoExist();
	}

	public MyFile(ResourcesFiles resourcesFiles)
	{
		this.localJsonFormat = PRETTY;
		this.fileType = FILE;
		setFilePath(resourcesFiles);
		createIfNoExist();
	}

	public boolean fileExist()
	{
		return Files.exists(path);
	}

	public boolean directoryExist()
	{
		return Files.exists(path.getParent());
	}

	public void createIfNoExist()
	{
		if(!directoryExist()) createDirectory();

		if(fileType == FILE)
		{
			if(!fileExist()) createFile();
		}
	}

	public List<String> read()
	{
		try
		{
			return Files.readAllLines(path, StandardCharsets.UTF_8);
		}catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public String readOneLine()
	{
		return String.join("", read());
	}

	public <T> T read(Class<T> origin)
	{
		return getGsonClass().fromJson(readOneLine(), origin);
	}

	public void write(Object msg)
	{
		if(msg.getClass() == String.class) write(msg.toString());
		else writeOrAppend(msg, StandardOpenOption.WRITE);
	}

	public void append(Object msg)
	{
		if(msg.getClass() == String.class) append(msg.toString());
		else writeOrAppend(msg, StandardOpenOption.APPEND);
	}

	public void setFilePath(ResourcesFiles type)
	{

		path = switch(type)
				{
					case SAVED_DATA ->
					{
						try
						{
							yield Path.of(Config.resourcesPath + "User_data/" + Connection.getName() + "_data.json");
						}catch(NullPointerException e)
						{
							setFileType(DIRECTORY);
							yield Path.of(Config.resourcesPath + "User_data/");
						}
					}
					case MESSAGE_LOG ->
					{
						try
						{
							yield Path.of(Config.resourcesPath + "User_messages/" + Connection.getName() + "_messages.txt"); // txt потому, что в json всё будет красное от ошибок
						}catch(NullPointerException e)
						{
							setFileType(DIRECTORY);
							yield Path.of(Config.resourcesPath + "User_messages/");
						}
					}
					case TEST_NAME -> Path.of(Config.resourcesPath + Config.testName);
					case CONFIG -> Path.of(Config.resourcesPath + "config.json");
					case TEST_LOG -> Path.of(Config.resourcesPath + "test_log.json");
					case VERIFICATION -> Path.of(Config.resourcesPath + "verification.json");
					case SYSTEM_LOG -> Path.of(Config.resourcesPath + "system_log.txt");
					case FEMIDA_EXCEL -> Path.of(Config.resourcesPath + "Femida.xlsx");
				};
	}

	public MyFile clear()
	{
		PrintWriter writer;
		try
		{
			writer = new PrintWriter(path.toFile());
			writer.print("");
			writer.close();
		}catch(FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		return this;
	}

	private void createFile()
	{
		try(PrintWriter writer = new PrintWriter(path.toFile()))
		{
			writer.print("");

			LoggerBot.logMethodReturn("createFile", path);
			LoggerBot.log("");

		}catch(FileNotFoundException e)
		{
			LoggerBot.logMethodReturn("createFile", "FileNotFoundException", path);
			LoggerBot.log("");
			throw new RuntimeException(e);
		}
	}

	private void createDirectory()
	{
		try
		{
			Files.createDirectories(path.getParent());
			LoggerBot.logMethodReturn("createDirectory", path.getParent());
			LoggerBot.log("");

		}catch(IOException e)
		{
			LoggerBot.logMethodReturn("createDirectory", "IOException", path);
			LoggerBot.log("");

			throw new RuntimeException(e);
		}
	}

	private void write(String msg)
	{
		writeOrAppend(msg, StandardOpenOption.WRITE);
	}

	private void append(String msg)
	{
		writeOrAppend(msg, StandardOpenOption.APPEND);
	}

	private void writeOrAppend(Object msg, StandardOpenOption standardOpenOption)
	{
		String message = gsonToString(msg);

		try
		{
			Files.writeString(path, message, standardOpenOption);
		}catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private String gsonToString(Object msg)
	{
		return getGsonClass().toJson(msg) + "\n";
	}

	private void writeOrAppend(String msg, StandardOpenOption standardOpenOption)
	{
		msg += "\n";

		try
		{
			Files.writeString(Paths.get(path.toUri()), msg, standardOpenOption);
		}catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private Gson getGsonClass()
	{
		if(localJsonFormat == PRETTY) return new GsonBuilder().setPrettyPrinting().create();
		return new Gson();
	}

	public MyFile setJsonFormat(LocalJsonFormat localJsonFormat)
	{
		this.localJsonFormat = localJsonFormat;
		return this;
	}

	public void setFileType(FileType fileType)
	{
		this.fileType = fileType;
	}

	public Path getPath()
	{
		return path;
	}
	public Path getDirectory()
	{
		return path.getParent();
	}
	public File[] getInnerFiles()
	{
		if(fileType == FILE) return null;

		File file = new File(getDirectory().toUri());
		return file.listFiles();
	}
}
