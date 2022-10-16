package main.java.BotPack.FilesPack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import main.java.BotPack.DataTypes.Connection;
import main.java.BotPack.DataTypes.UserDataToSave;
import main.java.Config;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static main.java.BotPack.DataTypes.Connection.getDifferentFilePath;
import static main.java.BotPack.Processors.Processer.connections;

public class FilesManipulator
{
	public static void write(Object msg, Connection.ResourcesFiles file, boolean format, boolean clear)
	{
		Gson gson;

		if(format) gson = new GsonBuilder().setPrettyPrinting().create();
		else gson = new Gson();

		String s = gson.toJson(msg) + "\n";
		Path path = getDifferentFilePath(file);

		StandardOpenOption openOption;
		if(clear) openOption = StandardOpenOption.WRITE;
		else openOption = StandardOpenOption.APPEND;

		try(OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path.toFile()), StandardCharsets.UTF_8))
		{
			writer.write(s);
		}catch(FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static List<String> read(Connection.ResourcesFiles file)
	{
		Path path = getDifferentFilePath(file);
		List<String> lines;
		try
		{
			lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		}catch(IOException e)
		{
			throw new RuntimeException(e);
		}
		return lines;
	}

	public static int loadSavedConnections()
	{
		if(connections == null) connections = new ArrayList<>();

		List<String> savedConnections = read(Connection.ResourcesFiles.SAVED_DATA);

		if(connections.isEmpty())
		{
			Gson gson = new Gson();
			for(String s : savedConnections)
			{
				UserDataToSave userDataToSave = gson.fromJson(s, UserDataToSave.class);
				connections.add(new Connection(userDataToSave.name, userDataToSave.menuStep));
			}
		}
		return savedConnections.size();
	}
	public static void readConfig()
	{
		Map<String, String> map = new HashMap<>();

		List<String> strings = FilesManipulator.read(Connection.ResourcesFiles.CONFIG);
		String s = String.join("", strings);

		Gson gson = new GsonBuilder().setLenient().create();
		map = gson.fromJson(s,map.getClass());

		try
		{
			map.get("testName");
		}
		catch(NullPointerException e)
		{
			System.out.println("Не заданы параметры программы");
			Scanner in = new Scanner(System.in);
			System.out.print("Название файла с тестом: ");
			String testName = in.nextLine();
			System.out.print("Название базы данных: ");
			String DBName = in.nextLine();
			System.out.print("Имя пользователя для базы данных: ");
			String DBUser = in.nextLine();
			System.out.print("Пароль пользователя для базы данных: ");
			String userPass = in.nextLine();
			System.out.print("Название таблицы для сохранения соревнований: ");
			String competitionsTableName = in.nextLine();
			System.out.print("Название таблицы для сохранения аккаунтов судей: ");
			String refereeTableName = in.nextLine();

			map = new HashMap<>();
			map.put("testName", testName);
			map.put("DBName", DBName);
			map.put("DBUser", DBUser);
			map.put("userPass", userPass);
			map.put("competitionsTableName", competitionsTableName);
			map.put("refereeTableName", refereeTableName);

			FilesManipulator.write(map, Connection.ResourcesFiles.CONFIG,true, false);
		}
		Config.testName = map.get("testName");
		Config.refereeTableName = map.get("refereeTableName");
		Config.competitionsTableName = map.get("competitionsTableName");
		Config.databaseName = map.get("DBName");
		Config.user = map.get("DBUser");
		Config.userPass = map.get("userPass");
	}
}
