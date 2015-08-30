package org.fisked.command;

import java.io.File;
import java.io.IOException;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;
import org.fisked.log.Log;

public class OpenFileCommand implements ICommandHandler {

	@Override
	public void run(BufferWindow window, String[] argv) {
		if (argv.length != 2) {
			window.getCommandController().setCommandFeedback("Wrong number of arguments.");
			window.refresh();
			return;
		}
		String path = argv[1];
		path = path.replaceFirst("^~",System.getProperty("user.home"));
		File file = new File(path);
		if (!file.isAbsolute()) {
			path = System.getProperty("user.dir") + File.separator + path;
			Log.println(path);
			file = new File(path);
		}
		try {
			Buffer buffer = new Buffer(file);
			window.setBuffer(buffer);
		} catch (IOException e) {
			window.getCommandController().setCommandFeedback("Could not open file: " + argv[1] + ".");
			window.refresh();
		}
	}

}
