package org.fisked.renderingengine;

import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.renderingengine.service.ICursorService;

public class DefaultCursorService implements ICursorService {

	private final IConsoleService _console;

	public DefaultCursorService(IConsoleService console) {
		_console = console;
	}

	@Override
	public void changeCursor(int cursor) {
		int translation;
		if (cursor == ICursorService.CURSOR_BLOCK) {
			translation = 1;
		} else if (cursor == ICursorService.CURSOR_VERTICAL_BAR) {
			translation = 3;
		} else {
			translation = 5;
		}
		try (IRenderingContext rc = _console.getRenderingContext()) {
			rc.printString("\u001B[" + translation + " q");
			_console.flush();
		}
	}

}
