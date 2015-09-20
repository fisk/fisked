package org.fisked.renderingengine;

import java.io.IOException;
import java.util.Stack;

import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Rectangle;

import jline.console.ConsoleReader;

public class ConsoleService implements IConsoleService {
	private ConsoleReader _reader;
	private Stack<RenderingContext> _renderingContexts = new Stack<>();
	
	public ConsoleService() {
		try {
			_reader = new ConsoleReader();
		} catch (IOException e) {}
	}
	
	private void print(String string) {
		System.out.print(string);
	}

	private void csi() {
		print("\u001B[");
	}

	@Override
	public void flush() {
		try {
			_reader.flush();
		} catch (IOException e) {}
	}

	@Override
	public int getChar() {
		try {
			return _reader.readCharacter();
		} catch (IOException e) {}
		return 0;
	}

	@Override
	public int getScreenWidth() {
		return _reader.getTerminal().getWidth();
	}

	@Override
	public int getScreenHeight() {
		return _reader.getTerminal().getHeight();
	}
	
	class RenderingContext implements IRenderingContext {

		@Override
		public void clearScreen(Color color) {
			clearScreen();
		}

		@Override
		public void clearRect(Rectangle rect, Color color) {
			int line = rect.getOrigin().getY();
			for (int j = 0; j < rect.getSize().getHeight(); j++) {
				moveTo(rect.getOrigin().getX(), line);
				StringBuilder str = new StringBuilder();
				for (int i = 0; i < rect.getSize().getWidth(); i++) {
					str.append(' ');
				}
				AttributedString attrStr = new AttributedString(str);
				attrStr.setBackgroundColor(color);
				printString(attrStr);
				line++;
			}
		}

		@Override
		public void clearScreen() {
			csi();
			print("2J");
		}

		@Override
		public void printString(String string) {
			print(string);
		}

		@Override
		public void printString(AttributedString string) {
			print(string.toString());
		}

		
		@Override
		public void moveTo(int x, int y) {
			csi();
			print((y + 1) + ";" + (x + 1) + "H");
		}

		@Override
		public void close() {
			_renderingContexts.pop();
			if (_renderingContexts.isEmpty()) {
				flush();
			}
		}
		
	}

	@Override
	public IRenderingContext getRenderingContext() {
		RenderingContext context = new RenderingContext();
		_renderingContexts.push(context);
		return context;
	}

}
