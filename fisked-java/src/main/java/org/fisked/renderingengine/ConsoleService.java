package org.fisked.renderingengine;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Stack;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.ICursorService;
import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Range;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.settings.Settings;
import org.fisked.util.FileUtil;
import org.fisked.util.shell.ShellCommandExecution;

import jline.console.ConsoleReader;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
public class ConsoleService implements IConsoleService {
	private ConsoleReader _jline;
	private InputStream _in;
	private OutputStream _out;
	private Reader _reader;
	private final Stack<RenderingContext> _renderingContexts = new Stack<>();
	private String _original_stty;

	public ConsoleService() {
		try {
			System.setProperty("jline.configuration", FileUtil.getFiskedFile("jlinerc").getAbsolutePath());
			System.setProperty("jline.inputrc", FileUtil.getFiskedFile("inputrc").getAbsolutePath());
			_in = new FileInputStream(FileDescriptor.in);
			_out = System.out;
			_reader = new InputStreamReader(_in, "UTF-8");

			_jline = new ConsoleReader("fisked", _in, _out, null);
			_jline.getTerminal().setEchoEnabled(false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void print(String string) {
		try {
			_jline.print(string);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void csi() {
		print("\u001B[");
	}

	private void esc() {
		print("\u001B");
	}

	@Override
	public void flush() {
		try {
			_jline.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getChar() throws IOException {
		return _reader.read();
	}

	@Override
	public int getScreenWidth() {
		return _jline.getTerminal().getWidth();
	}

	@Override
	public int getScreenHeight() {
		return _jline.getTerminal().getHeight();
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
			print(y + 1 + ";" + (x + 1) + "H");
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
		ShellCommandExecution execution = new ShellCommandExecution("stty", "-g");
		execution.redirectInput();
		_original_stty = execution.executeSync().getResult();
		execution = new ShellCommandExecution("stty", "raw");
		execution.redirectInput();
		execution.executeSync();

		csi();
		print("?1049h");
		print("\u001B%G");
		flush();
	}

	@Override
	public void deactivate() {
		csi();
		print("?1049l");
		_jline.shutdown();
		flush();
		ShellCommandExecution execution = new ShellCommandExecution("stty", _original_stty);
		execution.redirectInput();
		execution.executeSync();
	}

	private ICursorService _cursor = null;

	@Override
	public ICursorService getCursorService() {
		if (_cursor != null)
			return _cursor;
		if (Settings.getInstance().getTerminalType() == Settings.TerminalType.iTerm) {
			_cursor = new ItermCursorService(this);
		} else {
			_cursor = new DefaultCursorService(this);
		}

		return _cursor;
	}

	private void setScrollRegion(Range range) {
		if (range == null) {
			csi();
			print("r");
		} else {
			csi();
			print("" + range.getStart() + ";" + (range.getEnd() - 1) + "r");
		}
	}

	@Override
	public void scrollTextRegionUp(Range range) {
		setScrollRegion(range);
		esc();
		print("D");
		flush();
	}

	@Override
	public void scrollTextRegionDown(Range range) {
		setScrollRegion(range);
		esc();
		print("M");
		flush();
	}

}
