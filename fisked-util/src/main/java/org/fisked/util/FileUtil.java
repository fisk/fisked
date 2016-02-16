package org.fisked.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {
	public static File getFile(String string) {
		String pathString = string.replaceFirst("^~", System.getProperty("user.home"));
		Path path = Paths.get(pathString);
		path = path.normalize();
		File file = path.toFile();

		return file;
	}

	public static File getFiskedHome() {
		File file = getFile("~/.fisked");
		if (!file.exists()) {
			if (!file.mkdir()) {
				throw new RuntimeException("Could not get fisked home directory.");
			}
		}
		return file;
	}
}
