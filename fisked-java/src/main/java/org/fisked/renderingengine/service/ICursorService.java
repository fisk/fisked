package org.fisked.renderingengine.service;

public interface ICursorService {
	final int CURSOR_BLOCK = 0;
	final int CURSOR_VERTICAL_BAR = 1;
	final int CURSOR_UNDERLINE = 2;

	// TODO: Support more terminals than iterm: http://vim.wikia.com/wiki/Change_cursor_shape_in_different_modes
	
	void changeCursor(int cursor);
}
