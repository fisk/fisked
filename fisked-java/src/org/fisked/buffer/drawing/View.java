package org.fisked.buffer.drawing;

import java.util.ArrayList;
import java.util.List;

import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.RecognitionState;
import org.fisked.services.ServiceManager;
import org.fisked.theme.ThemeManager;

public class View implements IInputResponder, IDrawable {
	private Rectangle _bounds;
	private View _parent;
	private List<View> _subviews = new ArrayList<>();
	private Color _backgroundColor;
	
	public View(Rectangle frame) {
		_bounds = frame;
	}
	
	public void addSubview(View subview) {
		_subviews.add(subview);
		subview._parent = this;
	}
	
	public void removeFromParent() {
		_parent._subviews.remove(this);
		_parent = null;
	}
	
	public Rectangle getClippingRect() {
		if (_parent == null) return _bounds;
		
		Rectangle parentRect = _parent.getClippingRect();
		Rectangle clipRect = new Rectangle(
				parentRect.getOrigin().getX() + _bounds.getOrigin().getX(),
				parentRect.getOrigin().getY() + _bounds.getOrigin().getY(),
				_bounds.getSize().getWidth(),
				_bounds.getSize().getHeight()
				);
		
		return clipRect;
	}

	@Override
	public RecognitionState handleInput(Event input) {
		boolean maybeRecognized = false;
		for (View view: _subviews) {
			RecognitionState state = view.handleInput(input);
			if (state == RecognitionState.Recognized) {
				return RecognitionState.Recognized;
			} else if (state == RecognitionState.MaybeRecognized) {
				maybeRecognized = true;
			}
		}
		return maybeRecognized ? RecognitionState.MaybeRecognized : RecognitionState.NotRecognized;
	}
	
	public Color getParentBackgroundColor() {
		if (_parent == null) return null;
		if (_parent._backgroundColor == null) return _parent._backgroundColor;
		return _parent.getParentBackgroundColor();
	}
	
	public Color getBackgroundColor() {
		if (_backgroundColor != null) return _backgroundColor;
		Color parentColor = getParentBackgroundColor();
		if (parentColor != null) return parentColor;
		return ThemeManager.getThemeManager().getCurrentTheme().getBackgroundColor();
	}

	@Override
	public void draw() {
		ServiceManager sm = ServiceManager.getInstance();
		IConsoleService cs = sm.getConsoleService();
		
		try (IRenderingContext context = cs.getRenderingContext()) {
			Rectangle rect = getClippingRect();
			drawInRect(rect, context);
			_subviews.forEach(subview -> subview.draw());
		}
	}
	
	public void drawInRect(Rectangle drawingRect, IRenderingContext context) {
		if (_backgroundColor != null && _backgroundColor.equals(getParentBackgroundColor())) {
			
			context.clearRect(drawingRect, _backgroundColor);
		}
	}

}
