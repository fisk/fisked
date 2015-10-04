package org.fisked.buffer;

import org.fisked.buffer.drawing.View;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.renderingengine.service.models.Rectangle;

public class ModeLineView extends View {
	
	private ModeLineController _controller;

	public ModeLineView(Rectangle frame, ModeLineController controller) {
		super(frame);
		_controller = controller;
	}
	
	public void drawInRect(Rectangle drawingRect, IRenderingContext context) {
		super.drawInRect(drawingRect, context);
		
		AttributedString attrString = _controller.getModeLineText();

		context.moveTo(drawingRect.getOrigin().getX(), drawingRect.getOrigin().getY());
		context.printString(attrString);
	}

}
