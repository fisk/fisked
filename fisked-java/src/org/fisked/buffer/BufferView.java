package org.fisked.buffer;

import org.fisked.buffer.drawing.Color;
import org.fisked.buffer.drawing.Rectangle;
import org.fisked.buffer.drawing.View;
import org.fisked.theme.ThemeManager;

import jcurses.system.CharColor;
import jcurses.system.Toolkit;

public class BufferView extends View {
	BufferController _controller;
	
	public BufferView(Rectangle frame) {
		super(frame);
	}
	
	public void setBufferController(BufferController controller) {
		_controller = controller;
	}
	
	public void drawInRect(Rectangle drawingRect) {
		super.drawInRect(drawingRect);
		
		Color backgroundColor = getBackgroundColor();
		CharColor charColor = new CharColor(
				backgroundColor.getRawColor(), 
				ThemeManager.getThemeManager().getCurrentTheme().getForegroundColor().getRawColor()
				);
		
		String string = _controller.getString(drawingRect);
		Toolkit.printString(string, drawingRect.toJcursesRectangle(), charColor);
	}
}
