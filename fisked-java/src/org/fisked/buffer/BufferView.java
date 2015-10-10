package org.fisked.buffer;

import org.fisked.buffer.drawing.View;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Face;
import org.fisked.renderingengine.service.models.Point;
import org.fisked.renderingengine.service.models.Range;
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
	
	public void drawSplitString(IRenderingContext context, Point point, String string, Face leftFace, Face rightFace, int split) {
		String leftString = string.substring(0, split);
		String rightString = string.substring(split, string.length());
		if (leftString.length() > 0) {
			AttributedString str = new AttributedString(leftString, leftFace);
			context.moveTo(point.getX(), point.getY());
			context.printString(str);
		}
		if (rightString.length() > 0) {
			AttributedString str = new AttributedString(rightString, rightFace);
			context.moveTo(point.getX() + split, point.getY());
			context.printString(str);
		}
	}
	
	public void drawSplitString(IRenderingContext context, Point point, String string, Face leftFace, Face middleFace, Face rightFace, int split1, int split2) {
		String leftString = string.substring(0, split1);
		String middleString = string.substring(split1, split2);
		String rightString = string.substring(split2, string.length());
		if (leftString.length() > 0) {
			AttributedString str = new AttributedString(leftString, leftFace);
			context.moveTo(point.getX(), point.getY());
			context.printString(str);
		}
		if (middleString.length() > 0) {
			AttributedString str = new AttributedString(middleString, middleFace);
			context.moveTo(point.getX() + split1, point.getY());
			context.printString(str);
		}
		if (rightString.length() > 0) {
			AttributedString str = new AttributedString(rightString, rightFace);
			context.moveTo(point.getX() + split2, point.getY());
			context.printString(str);
		}
	}
	
	public void drawInRect(Rectangle drawingRect, IRenderingContext context) {
		super.drawInRect(drawingRect, context);
		
		Color backgroundColor = getBackgroundColor();
		Color foregroundColor = ThemeManager.getThemeManager().getCurrentTheme().getForegroundColor();
		Color selectionBackgroundColor = ThemeManager.getThemeManager().getCurrentTheme().getSelectionBackgroundColor();
		Color selectionForegroundColor = ThemeManager.getThemeManager().getCurrentTheme().getSelectionForegroundColor();
		
		Range selection = _controller.getSelection();
		
		if (selection == null) {
			_controller.drawBuffer(drawingRect, (Point point, String str, int offset) -> {
				AttributedString attrString = new AttributedString(str);
				Color background = backgroundColor;
				attrString.setBackgroundColor(background);
				attrString.setForegroundColor(foregroundColor);
				context.moveTo(drawingRect.getOrigin().getX(), point.getY());
				context.printString(str);
			});
		} else {
			_controller.drawBuffer(drawingRect, (Point point, String str, int offset) -> {
				Color background = backgroundColor;
				Color foreground = foregroundColor;
				
				Point relativePoint = new Point(drawingRect.getOrigin().getX() + point.getX(), drawingRect.getOrigin().getY() + point.getY());

				if (selection.getStart() >= offset && selection.getStart() < offset + str.length()) {
					if (selection.getEnd() >= offset && selection.getEnd() < offset + str.length()) {
						drawSplitString(context, relativePoint, str, new Face(backgroundColor, foregroundColor), new Face(selectionBackgroundColor, selectionForegroundColor), 
								new Face(backgroundColor, foregroundColor), selection.getStart() - offset, selection.getEnd() - offset);
					} else {
						drawSplitString(context, relativePoint, str, new Face(backgroundColor, foregroundColor), 
								new Face(selectionBackgroundColor, selectionForegroundColor), selection.getStart() - offset);
					}
					return;
				} else if (selection.getEnd() >= offset && selection.getEnd() < offset + str.length()) {
					drawSplitString(context, relativePoint, str, new Face(selectionBackgroundColor, selectionForegroundColor), 
							new Face(backgroundColor, foregroundColor), selection.getEnd() - offset);
					return;
				} else if (offset >= selection.getStart() && offset < selection.getEnd()) {
					background = selectionBackgroundColor;
					foreground = selectionForegroundColor;
				}

				AttributedString attrString = new AttributedString(str, new Face(background, foreground));
				context.moveTo(drawingRect.getOrigin().getX(), point.getY());
				context.printString(attrString);
			});
		}
	}
}
