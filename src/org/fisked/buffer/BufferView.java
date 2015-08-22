package org.fisked.buffer;

import org.fisked.buffer.drawing.Color;
import org.fisked.buffer.drawing.Rectangle;
import org.fisked.buffer.drawing.View;
import org.fisked.theme.ThemeManager;

import jcurses.system.CharColor;
import jcurses.system.Toolkit;

public class BufferView extends View {
	private Buffer _buffer;
	
	public BufferView(Buffer buffer, Rectangle frame) {
		super(frame);
		_buffer = buffer;
	}
	
	public void drawInRect(Rectangle drawingRect) {
		super.drawInRect(drawingRect);
		
		Color backgroundColor = getBackgroundColor();
		CharColor charColor = new CharColor(
				backgroundColor.getRawColor(), 
				ThemeManager.getThemeManager().getCurrentTheme().getForegroundColor().getRawColor()
				);
		
		Toolkit.printString(_buffer.toString(), drawingRect.toJcursesRectangle(), charColor);
	}
}
