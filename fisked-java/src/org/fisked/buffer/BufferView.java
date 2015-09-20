package org.fisked.buffer;

import org.fisked.buffer.drawing.View;
import org.fisked.log.Log;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Point;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.theme.ThemeManager;

public class BufferView extends View {
	BufferController _controller;
	
	public BufferView(Rectangle frame) {
		super(frame);
	}
	
	public void setBufferController(BufferController controller) {
		_controller = controller;
	}
	
	public void drawInRect(Rectangle drawingRect, IRenderingContext context) {
		super.drawInRect(drawingRect, context);
		
		Color backgroundColor = getBackgroundColor();
		Color foregroundColor = ThemeManager.getThemeManager().getCurrentTheme().getForegroundColor();
		
		_controller.drawBuffer(drawingRect, (Point point, String str, int offset) -> {
			AttributedString attrString = new AttributedString(str);
			attrString.setBackgroundColor(backgroundColor);
			attrString.setForegroundColor(foregroundColor);
			context.moveTo(point.getX(), point.getY());
			context.printString(str);
		});
	}
}
