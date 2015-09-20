package org.fisked.buffer;

import org.fisked.buffer.drawing.View;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.theme.ThemeManager;

public class ModeLineView extends View {
	
	private ModeLineController _controller;

	public ModeLineView(Rectangle frame, ModeLineController controller) {
		super(frame);
		_controller = controller;
	}
	
	public void drawInRect(Rectangle drawingRect, IRenderingContext context) {
		super.drawInRect(drawingRect, context);
		
		Color backgroundColor = getBackgroundColor();
		Color foregroundColor = ThemeManager.getThemeManager().getCurrentTheme().getForegroundColor();
		
		AttributedString attrString = new AttributedString(_controller.getModeLineText());
		attrString.setBackgroundColor(backgroundColor);
		attrString.setForegroundColor(foregroundColor);

		context.moveTo(drawingRect.getOrigin().getX(), drawingRect.getOrigin().getY());
		context.printString(attrString);
	}

}
