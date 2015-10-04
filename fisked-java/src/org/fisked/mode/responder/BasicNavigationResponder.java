package org.fisked.mode.responder;

import org.fisked.buffer.BufferWindow;
import org.fisked.log.Log;
import org.fisked.renderingengine.service.models.Point;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;
import org.fisked.text.TextNavigator;

public class BasicNavigationResponder implements IInputResponder {
	private BufferWindow _window;
	private TextNavigator _navigator;
	
	public BasicNavigationResponder(BufferWindow window) {
		_window = window;
		_navigator = new TextNavigator(_window.getBuffer());
	}
	
	@Override
	public boolean handleInput(Event input) {
		if (input.isControl() && input.getControlChar() == 'e') {
			_navigator.scrollDown();
		} else if (input.isControl() && input.getControlChar() == 'y') {
			_navigator.scrollUp();
		} else if (input.getCharacter() == 'h') {
			_navigator.moveLeft();
			Point point = _window.getBuffer().getCursor().getAbsolutePoint();
			if (point.getY() < _window.getBuffer().getTextLayout().getClippingRect().getOrigin().getY()) {
				_navigator.scrollUp();
			}
		} else if (input.getCharacter() == 'l') {
			_navigator.moveRight();
			Point point = _window.getBuffer().getCursor().getAbsolutePoint();
			Rectangle rect = _window.getBufferController().getTextLayout().getClippingRect();
			if (point.getY() >= rect.getOrigin().getY() + rect.getSize().getHeight()) {
				_navigator.scrollDown();
			}
		} else if (input.getCharacter() == 'j') {
			_navigator.moveDown();
			Point point = _window.getBuffer().getCursor().getAbsolutePoint();
			Rectangle rect = _window.getBufferController().getTextLayout().getClippingRect();
			Log.println("POINT: " + point.getY());
			Log.println("CEILING: " + (rect.getOrigin().getY() + rect.getSize().getHeight()));
			if (point.getY() >= rect.getOrigin().getY() + rect.getSize().getHeight()) {
				_navigator.scrollDown();
			}
		} else if (input.getCharacter() == 'k') {
			_navigator.moveUp();
			Point point = _window.getBuffer().getCursor().getAbsolutePoint();
			if (point.getY() < _window.getBuffer().getTextLayout().getClippingRect().getOrigin().getY()) {
				_navigator.scrollUp();
			}
		} else {
			return false;
		}
		return true;
	}

}
