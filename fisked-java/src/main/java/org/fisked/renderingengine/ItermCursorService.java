package org.fisked.renderingengine;

import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.renderingengine.service.ICursorService;

public class ItermCursorService implements ICursorService {
	private IConsoleService _console;

	public ItermCursorService(IConsoleService console) {
		_console = console;
	}

	@Override
	public void changeCursor(int cursor) {
		try (IRenderingContext rc = _console.getRenderingContext()) {
			if (System.getenv().containsKey("TMUX")) {
				rc.printString("\u001bPtmux;\u001b\u001b]50;CursorShape=" + cursor + "\u0007\u001b\\");
			} else {
				rc.printString("\u001b]50;CursorShape=" + cursor + "\u0007");
			}
		}
	}

}
