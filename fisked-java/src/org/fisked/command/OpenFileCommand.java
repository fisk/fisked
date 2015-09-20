package org.fisked.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;

public class OpenFileCommand implements ICommandHandler {

	@Override
	public void run(BufferWindow window, String[] argv) {
		if (argv.length != 2) {
			window.getCommandController().setCommandFeedback("Wrong number of arguments.");
			window.refresh();
			return;
		}
		
		String pathString = argv[1].replaceFirst("^~", System.getProperty("user.home"));
		Path path = Paths.get(pathString);
		path = path.normalize();
		File file = path.toFile();
		
		try {
			file.createNewFile();
			Buffer buffer = new Buffer(file);
			window.setBuffer(buffer);
		} catch (IOException e) {
			window.getCommandController().setCommandFeedback("Could not open file: " + argv[1] + ".");
			window.refresh();
		}
	}

}
