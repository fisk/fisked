package org.fisked.buffer.drawing;

import org.fisked.responder.Event;
import org.fisked.responder.IRawInputResponder;

public class Window implements IRawInputResponder, IDrawable {
	protected View _rootView;
	
	public Window(Rectangle windowRect) {}
	
	public void setRootView(View rootView) {
		_rootView = rootView;
	}
	
	public View getRootView() {
		return _rootView;
	}

	@Override
	public boolean handleInput(Event input) {
		return true;
	}

	@Override
	public void draw() {
		_rootView.draw();
	}
	
	public void drawPoint() {}
	
}
