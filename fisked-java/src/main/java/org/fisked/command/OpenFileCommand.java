package org.fisked.command;

import java.io.File;
import java.io.IOException;

import org.fisked.buffer.BufferWindow;
import org.fisked.command.api.ICommandHandler;
import org.fisked.util.FileUtil;

public class OpenFileCommand implements ICommandHandler {

	@Override
	public void run(BufferWindow window, String[] argv) {
		if (argv.length != 2) {
			window.getCommandController().setCommandFeedback("Wrong number of arguments.");
			window.refresh();
			return;
		}
		
		File file = FileUtil.getFile(argv[1]);
		
		try {
			file.createNewFile();
			window.openFile(file);
		} catch (IOException e) {
			window.getCommandController().setCommandFeedback("Could not open file: " + argv[1] + ".");
			window.refresh();
		}
	}

}
