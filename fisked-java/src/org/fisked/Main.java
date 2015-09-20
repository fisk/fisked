package org.fisked;

import org.fisked.log.Log;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.services.ServiceManager;

public class Main {
	public static void main(String[] args) {
		Application application = Application.getApplication();
		application.start();
		ServiceManager sm = ServiceManager.getInstance();
		IConsoleService cs = sm.getConsoleService();
		
		AttributedString str = new AttributedString("Hello world!");
		str.setForegroundColor(Color.BLUE);
		str.setBackgroundColor(Color.CYAN);
		str.setBold();
		
		try (IRenderingContext context = cs.getRenderingContext()) {
			context.clearScreen();
			System.out.println(str.toString());
			context.moveTo(3, 5);
		}
		
		int i = 0;
		StringBuilder stringBuilder = new StringBuilder();
		int event;
		while ((event = cs.getChar()) != 'q') {
			stringBuilder.append((char)event);
			try (IRenderingContext context = cs.getRenderingContext()) {
				context.clearScreen();
				context.moveTo(0, 0);
				context.printString(str.toString());
				context.moveTo(0, 1);
				context.printString(stringBuilder.toString());
				context.moveTo(0 + i, 5);
			}
			i++;
		}
		System.exit(0);
	}
/*
		try {
			reader = new ConsoleReader();
			reader.clearScreen();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
        do {
            try {
            	PrintWriter out = new PrintWriter(reader.getOutput());
				character = reader.readCharacter();
				out.print((char)character);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        while (character != (int)'q');
	}*/
}
