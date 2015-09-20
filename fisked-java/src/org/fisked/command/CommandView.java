package org.fisked.command;

import org.fisked.buffer.drawing.View;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.theme.ThemeManager;

public class CommandView extends View {
	private CommandController _controller;

	public CommandView(Rectangle frame, CommandController controller) {
		super(frame);
		_controller = controller;
	}
	
	public void drawInRect(Rectangle drawingRect, IRenderingContext context) {
		super.drawInRect(drawingRect, context);
		
		Color backgroundColor = getBackgroundColor();
		Color foregroundColor = ThemeManager.getThemeManager().getCurrentTheme().getCommandForegroundColor();
		
		String string = _controller.getString(drawingRect);
		AttributedString attrString = new AttributedString(string);
		attrString.setBackgroundColor(backgroundColor);
		attrString.setForegroundColor(foregroundColor);
		
		context.printString(attrString);
	}

}
