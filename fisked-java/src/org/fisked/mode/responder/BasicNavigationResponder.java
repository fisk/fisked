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
			moveCursorDownIfNeeded();
		} else if (input.isControl() && input.getControlChar() == 'y') {
			_navigator.scrollUp();
			moveCursorUpIfNeeded();
		} else if (input.getCharacter() == 'h') {
			_navigator.moveLeft();
			scrollUpIfNeeded();
		} else if (input.getCharacter() == 'l') {
			_navigator.moveRight();
			scrollDownIfNeeded();
		} else if (input.getCharacter() == 'j') {
			_navigator.moveDown();
			scrollDownIfNeeded();
		} else if (input.getCharacter() == 'k') {
			_navigator.moveUp();
			scrollUpIfNeeded();
		} else if (input.getCharacter() == '0') {
			_navigator.moveToTheBeginningOfLine();
			scrollUpIfNeeded();
		} else if (input.getCharacter() == '$') {
			_navigator.moveToTheEndOfLine();
			scrollDownIfNeeded();
		} else {
			return false;
		}
		return true;
	}
	
	private void moveCursorDownIfNeeded() {
		Point point = _window.getBuffer().getCursor().getAbsolutePoint();
		while (point.getY() < _window.getBuffer().getTextLayout().getClippingRect().getOrigin().getY()) {
			_navigator.moveDown();
			point = _window.getBuffer().getCursor().getAbsolutePoint();
		}
	}
	
	private void moveCursorUpIfNeeded() {
		Point point = _window.getBuffer().getCursor().getAbsolutePoint();
		Rectangle rect = _window.getBufferController().getTextLayout().getClippingRect();
		while (point.getY() >= rect.getOrigin().getY() + rect.getSize().getHeight()) {
			_navigator.moveUp();
			point = _window.getBuffer().getCursor().getAbsolutePoint();
			rect = _window.getBufferController().getTextLayout().getClippingRect();
		}
	}
	
	private void scrollUpIfNeeded() {
		Point point = _window.getBuffer().getCursor().getAbsolutePoint();
		while (point.getY() < _window.getBuffer().getTextLayout().getClippingRect().getOrigin().getY()) {
			_navigator.scrollUp();
			point = _window.getBuffer().getCursor().getAbsolutePoint();
		}
	}
	
	private void scrollDownIfNeeded() {
		Point point = _window.getBuffer().getCursor().getAbsolutePoint();
		Rectangle rect = _window.getBufferController().getTextLayout().getClippingRect();
		while (point.getY() >= rect.getOrigin().getY() + rect.getSize().getHeight()) {
			_navigator.scrollDown();
			point = _window.getBuffer().getCursor().getAbsolutePoint();
			rect = _window.getBufferController().getTextLayout().getClippingRect();
		}
	}

}
