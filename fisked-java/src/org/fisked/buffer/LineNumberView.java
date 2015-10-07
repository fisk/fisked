package org.fisked.buffer;

import java.util.List;

import org.fisked.buffer.drawing.View;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.renderingengine.service.models.Rectangle;

public class LineNumberView extends View {

	private LineNumberController _controller;

	public LineNumberView(Rectangle frame, LineNumberController controller) {
		super(frame);
		_controller = controller;
	}
	
	public void drawInRect(Rectangle drawingRect, IRenderingContext context) {
		super.drawInRect(drawingRect, context);

		// TODO: some more work here..
		List<AttributedString> attrStrings = _controller.getLineNumberText();
		for (int i = 0; i < attrStrings.size(); i++) {
			context.moveTo(drawingRect.getOrigin().getX(), drawingRect.getOrigin().getY() + i);
			context.printString(attrStrings.get(i));
		}
	}

}
