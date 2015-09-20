package org.fisked.buffer.drawing;

import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.responder.Event;
import org.fisked.responder.IRawInputResponder;
import org.fisked.services.ServiceManager;
import org.fisked.theme.ITheme;
import org.fisked.theme.ThemeManager;

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
		ServiceManager sm = ServiceManager.getInstance();
		IConsoleService cs = sm.getConsoleService();
		try (IRenderingContext context = cs.getRenderingContext()) {
			ITheme theme = ThemeManager.getThemeManager().getCurrentTheme();
			context.clearScreen(theme.getBackgroundColor());
			_rootView.draw();
			drawPoint(context);
		}
	}

	public void drawPoint(IRenderingContext context) {
		
	}
	
}
