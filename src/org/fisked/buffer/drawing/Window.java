package org.fisked.buffer.drawing;

import org.fisked.responder.Event;
import org.fisked.responder.IRawInputResponder;
import org.fisked.theme.ITheme;
import org.fisked.theme.ThemeManager;

import jcurses.system.CharColor;
import jcurses.system.Toolkit;

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
		ITheme theme = ThemeManager.getThemeManager().getCurrentTheme();
		Toolkit.clearScreen(theme.getBackgroundColor().getCharColor());
		_rootView.draw();
	}
	
	public void drawPoint() {}
	
}
