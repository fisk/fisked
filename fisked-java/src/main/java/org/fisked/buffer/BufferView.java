package org.fisked.buffer;

import org.fisked.buffer.drawing.View;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Point;
import org.fisked.renderingengine.service.models.Range;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.text.IBufferDecorator;
import org.fisked.theme.ThemeManager;

public class BufferView extends View {
	BufferController _controller;

	public BufferView(Rectangle frame) {
		super(frame);
	}

	public void setBufferController(BufferController controller) {
		_controller = controller;
	}

	@Override
	public void drawInRect(Rectangle drawingRect, IRenderingContext context) {
		super.drawInRect(drawingRect, context);

		Buffer buffer = _controller.getBuffer();
		IBufferDecorator decorator = buffer.getSourceDecorator();

		AttributedString attributedString = buffer.getBufferTextState().decorate(decorator).copy();

		Color selectionBackgroundColor = ThemeManager.getThemeManager().getCurrentTheme().getSelectionBackgroundColor();
		Color selectionForegroundColor = ThemeManager.getThemeManager().getCurrentTheme().getSelectionForegroundColor();

		Range selection = _controller.getSelection();

		if (selection == null) {
			_controller.drawBuffer(drawingRect, (Point point, String str, int offset) -> {
				AttributedString attributedSubstring = attributedString.substring(offset, offset + str.length());
				context.moveTo(drawingRect.getOrigin().getX(), point.getY());
				context.printString(attributedSubstring);
			});
		} else {
			_controller.drawBuffer(drawingRect, (Point point, String str, int offset) -> {
				AttributedString attributedSubstring = attributedString.substring(offset, offset + str.length());

				int relativeSelectionStart = Math.max(selection.getStart() - offset, 0);
				int relativeSelectionEnd = Math.min(selection.getEnd() - offset, str.length());

				if (relativeSelectionEnd - relativeSelectionStart > 0) {
					attributedSubstring.setForegroundColor(selectionForegroundColor, relativeSelectionStart,
							relativeSelectionEnd);
					attributedSubstring.setBackgroundColor(selectionBackgroundColor, relativeSelectionStart,
							relativeSelectionEnd);
				}

				context.moveTo(drawingRect.getOrigin().getX(), point.getY());
				context.printString(attributedSubstring);
			});
		}
	}
}
