package org.fisked.command;

import org.fisked.buffer.drawing.Color;
import org.fisked.buffer.drawing.Rectangle;
import org.fisked.buffer.drawing.View;
import org.fisked.theme.ThemeManager;

import jcurses.system.CharColor;
import jcurses.system.Toolkit;

public class CommandView extends View {
	private CommandController _controller;

	public CommandView(Rectangle frame, CommandController controller) {
		super(frame);
		_controller = controller;
	}
	
	public void drawInRect(Rectangle drawingRect) {
		super.drawInRect(drawingRect);
		
		Color backgroundColor = getBackgroundColor();
		CharColor charColor = new CharColor(
				backgroundColor.getRawColor(), 
				ThemeManager.getThemeManager().getCurrentTheme().getCommandForegroundColor().getRawColor()
				);
		
		String string = _controller.getString(drawingRect);
		Toolkit.printString(string, drawingRect.toJcursesRectangle(), charColor);
	}

}
