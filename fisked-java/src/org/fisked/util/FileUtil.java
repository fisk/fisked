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
}
