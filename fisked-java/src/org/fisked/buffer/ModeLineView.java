package org.fisked.buffer;

import org.fisked.buffer.drawing.Color;
import org.fisked.buffer.drawing.Rectangle;
import org.fisked.buffer.drawing.View;
import org.fisked.theme.ThemeManager;

import jcurses.system.CharColor;
import jcurses.system.Toolkit;

public class ModeLineView extends View {
	
	private ModeLineController _controller;

	public ModeLineView(Rectangle frame, ModeLineController controller) {
		super(frame);
		_controller = controller;
	}
	
	public void drawInRect(Rectangle drawingRect) {
		super.drawInRect(drawingRect);
		
		Color backgroundColor = getBackgroundColor();
		CharColor charColor = new CharColor(
				backgroundColor.getRawColor(), 
				ThemeManager.getThemeManager().getCurrentTheme().getForegroundColor().getRawColor()
				);
		
		Toolkit.printString(_controller.getModeLineText(), drawingRect.toJcursesRectangle(), charColor);
	}

}
