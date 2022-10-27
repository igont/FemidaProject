package main.java.BotPack.FilesPack;

import java.io.File;
import java.nio.file.Path;

public class Directory
{
	private Path path;

	public Directory(Path path)
	{
		this.path = path;
	}
	public Directory(ResourcesFiles resourcesFiles)
	{
		new main.java.BotPack.FilesPack.File(resourcesFiles).getDirectory();
	}

	public File[] getInnerFiles()
	{
		File file = new File(path.toUri());
		return file.listFiles();
	}
}
