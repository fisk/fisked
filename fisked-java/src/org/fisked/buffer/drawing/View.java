package org.fisked.buffer.drawing;

import java.util.ArrayList;
import java.util.List;

import org.fisked.responder.Event;
import org.fisked.responder.IRawInputResponder;
import org.fisked.theme.ThemeManager;

import jcurses.system.Toolkit;

public class View implements IRawInputResponder, IDrawable {
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
	public boolean handleInput(Event input) {
		for (View view: _subviews) {
			if (view.handleInput(input)) {
				return true;
			}
		}
		return false;
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
		Toolkit.startPainting();
		Rectangle rect = getClippingRect();
		Toolkit.setClipRectangle(rect.toJcursesRectangle());
		drawInRect(rect);
		Toolkit.unsetClipRectangle();
		_subviews.forEach(subview -> subview.draw());
		Toolkit.endPainting();
	}
	
	public void drawInRect(Rectangle drawingRect) {
		if (_backgroundColor != null && _backgroundColor.equals(getParentBackgroundColor())) {
			Toolkit.drawRectangle(
					drawingRect.getOrigin().getX(), 
					drawingRect.getOrigin().getY(), 
					drawingRect.getSize().getWidth(), 
					drawingRect.getSize().getHeight(), 
					_backgroundColor.getCharColor()
					);
		}
	}

}
