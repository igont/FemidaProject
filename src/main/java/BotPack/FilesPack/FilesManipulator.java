package main.java.BotPack.FilesPack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import main.java.BotPack.DataTypes.Connection;
import main.java.BotPack.DataTypes.UserDataToSave;
import main.java.BotPack.Processors.Processer;
import main.java.Config;
import main.java.Main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class FilesManipulator
{
	public static List<String> read(Path path)
	{
		try
		{
			return Files.readAllLines(path, StandardCharsets.UTF_8);
		}catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	public static List<String> read(ResourcesFiles file)
	{
		try
		{
			return Files.readAllLines(getFilePath(file), StandardCharsets.UTF_8);
		}catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void write(Object msg, ResourcesFiles file, boolean format, boolean clear)
	{
		Path path = getFilePath(file);
		StandardOpenOption openOption = (clear ? StandardOpenOption.WRITE : StandardOpenOption.APPEND);

		Gson gson = (format ? new GsonBuilder().setPrettyPrinting().create() : new Gson());
		String message = gson.toJson(msg) + "\n";

		try
		{
			Files.writeString(Paths.get(path.toUri()), message, openOption);
		}catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void readConfig()
	{
		Map<String, String> map = new HashMap<>();

		List<String> strings = read(ResourcesFiles.CONFIG);
		String s = String.join("", strings);

		Gson gson = new GsonBuilder().setLenient().create();
		map = gson.fromJson(s, map.getClass());

		try
		{
			map.get("testName");
		}catch(NullPointerException e)
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

			FilesManipulator.write(map, ResourcesFiles.CONFIG, true, false);
		}
		Config.testName = map.get("testName");
		Config.refereeTableName = map.get("refereeTableName");
		Config.competitionsTableName = map.get("competitionsTableName");
		Config.databaseName = map.get("DBName");
		Config.user = map.get("DBUser");
		Config.userPass = map.get("userPass");
	}

	public static Path getFilePath(ResourcesFiles type)
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
				try
				{
					dataFilePath = Path.of(Config.resourcesPath + "User_data/" + Connection.getName() + "_data.json");
				}catch(NullPointerException ignored)
				{
					dataFilePath = Path.of(Config.resourcesPath + "User_data/");
				}
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
				Files.createDirectories(dataFilePath.getParent());
				Files.createFile(dataFilePath);
			}catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		return dataFilePath;
	}

	public static int loadSavedConnections()
	{
		File dir = new File(FilesManipulator.getFilePath(ResourcesFiles.SAVED_DATA).toUri());
		if(UserDataToSave.allUserDataToSave == null) UserDataToSave.allUserDataToSave = new ArrayList<>();
		File[] files = dir.listFiles();
		for(File file : files)
		{
			if(file.isFile())
			{
				List<String> userStrings = FilesManipulator.read(file.toPath());
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				UserDataToSave userData = gson.fromJson(String.join("", userStrings), UserDataToSave.class);
				UserDataToSave.allUserDataToSave.add(userData);

				Processer.addConnection(userData);
			}
		}

		return files.length;
	}

	public static void getResourcesPath()
	{
		String className = Main.class.getName().replace('.', '/');
		String classJar = Main.class.getResource("/" + className + ".class").toString();
		String path = "";

		String[] split = classJar.split("/");
		for(int i = 1; i < split.length; i++)
		{
			path += split[i] + "/";
			if(Objects.equals(split[i], "FemidaProject")) break;
		}
		if(path.startsWith("home"))
		{
			path = "/" + path;
		}
		Config.resourcesPath = path + "Resources/";
	}

	public enum ResourcesFiles
	{
		CONFIG, SAVED_DATA, TEST_NAME, TEST_LOG, VERIFICATION
	}
}
