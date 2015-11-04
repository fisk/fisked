package org.fisked.renderingengine;

import java.io.IOException;
import java.util.Stack;

import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.ICursorService;
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
			_reader.getTerminal().setEchoEnabled(false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void print(String string) {
		try {
			_reader.print(string);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void csi() {
		print("\u001B[");
	}

	@Override
	public void flush() {
		try {
			_reader.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
				AttributedString attrStr = new AttributedString(str.toString());
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
			print(string.toANSIString());
		}

		
		@Override
		public void moveTo(int x, int y) {
			csi();
			print((y + 1) + ";" + (x + 1) + "H");
		}
		
		private void showCursor() {
			csi();
			print("?25h");
			flush();
		}
		
		private void hideCursor() {
			csi();
			print("?25l");
			flush();
		}

		@Override
		public void close() {
			_renderingContexts.pop();
			if (_renderingContexts.isEmpty()) {
				showCursor();
				flush();
			}
		}
		
	}

	@Override
	public IRenderingContext getRenderingContext() {
		RenderingContext context = new RenderingContext();
		_renderingContexts.push(context);
		if (_renderingContexts.size() == 1) {
			context.hideCursor();
		}
		return context;
	}

	@Override
	public void activate() {
		csi();
		print("?1049h");
		print("\u001B%G");
		flush();
	}

	@Override
	public void deactivate() {
		csi();
		print("?1049l");
		_reader.shutdown();
		flush();
	}
	
	private ICursorService _cursor = null;
	
	@Override
	public ICursorService getCursorService() {
		if (_cursor != null) return _cursor;
		if (System.getenv().containsKey("ITERM_PROFILE")) {
			_cursor = new ItermCursorService(this);
		} else {
			_cursor = new DefaultCursorService();
		}

		return _cursor;
	}

}
