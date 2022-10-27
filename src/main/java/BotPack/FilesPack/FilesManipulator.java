package main.java.BotPack.FilesPack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import main.java.BotPack.DataTypes.UserDataToSave;
import main.java.BotPack.Processors.Processer;
import main.java.BotPack.Senders.LoggerBot;
import main.java.Config;
import main.java.Main;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

import static main.java.BotPack.FilesPack.ResourcesFiles.*;

public class FilesManipulator
{

	public static void readConfig()
	{
		LoggerBot.log("readConfig()");

		Map<String, String> map = new HashMap<>();

		String s = new MyFile(CONFIG).readOneLine();

		Gson gson = new GsonBuilder().setLenient().create();
		map = gson.fromJson(s, map.getClass());

		try
		{
			map.get("testName");
			LoggerBot.log("Конфиг найден");

		}catch(NullPointerException e)
		{
			LoggerBot.log("Конфиг не найден, собираем данные с консоли:");

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
			System.out.print("Название книги Excel: ");
			String excelFileName = in.nextLine();

			map = new HashMap<>();
			map.put("testName", testName);
			map.put("DBName", DBName);
			map.put("DBUser", DBUser);
			map.put("userPass", userPass);
			map.put("competitionsTableName", competitionsTableName);
			map.put("refereeTableName", refereeTableName);
			map.put("excelFileName", excelFileName);

			LoggerBot.log("testName", testName);
			LoggerBot.log("DBName", DBName);
			LoggerBot.log("DBUser", DBUser);
			LoggerBot.log("userPass", userPass);
			LoggerBot.log("competitionsTableName", competitionsTableName);
			LoggerBot.log("refereeTableName", refereeTableName);
			LoggerBot.log("excelFileName", excelFileName);

			new MyFile(CONFIG).write(map);
		}
		Config.testName = map.get("testName");
		Config.refereeTableName = map.get("refereeTableName");
		Config.competitionsTableName = map.get("competitionsTableName");
		Config.databaseName = map.get("DBName");
		Config.user = map.get("DBUser");
		Config.userPass = map.get("userPass");
		Config.excelFileName = map.get("excelFileName");

		LoggerBot.log("");
	}

	public static String loadSavedConnections()
	{
		LoggerBot.log("loadSavedConnections()");

		Path filePath = new MyFile(SAVED_DATA).getPath();

		if(filePath == null)
		{
			LoggerBot.log("Directory not exist");
			return "Directory not exist";
		}
		LoggerBot.log("Directory", filePath);

		File dir = new File(filePath.toUri());

		if(UserDataToSave.allUserDataToSave == null) UserDataToSave.allUserDataToSave = new ArrayList<>();
		File[] files = dir.listFiles();

		if(files != null)
		{
			LoggerBot.log("user count", files.length);
		}
		else
		{
			LoggerBot.log("user count", 0);
			LoggerBot.log("");
			return "Directory empty";
		}

		for(File file : files)
		{
			if(file.isFile())
			{
				UserDataToSave userData = new MyFile(file.toPath()).read(UserDataToSave.class);
				if(userData == null) break;

				UserDataToSave.allUserDataToSave.add(userData);

				Processer.addConnection(userData);
			}
		}
		LoggerBot.log("");
		return String.valueOf(files.length);
	}

	public static String getResourcesPath()
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

		LoggerBot.log("\n---------------------------------------------------------------------------------------------------------------------------" + new Date());
		LoggerBot.log("getResourcesPath()");
		LoggerBot.log("jar path", classJar);
		LoggerBot.log("resources path", Config.resourcesPath);
		LoggerBot.log("");

		return Config.resourcesPath;
	}
}
