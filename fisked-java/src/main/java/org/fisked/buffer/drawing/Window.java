package org.fisked.buffer.drawing;

import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.RecognitionState;
import org.fisked.services.ServiceManager;
import org.fisked.theme.ITheme;
import org.fisked.theme.ThemeManager;

public class Window implements IInputResponder, IDrawable {
	protected View _rootView;

	public Window(Rectangle windowRect) {
	}

	public void setRootView(View rootView) {
		_rootView = rootView;
	}

	public View getRootView() {
		return _rootView;
	}

	@Override
	public RecognitionState recognizesInput(Event input) {
		return RecognitionState.NotRecognized;
	}

	@Override
	public void onRecognize() {

	}

	@Override
	public void draw() {
		ServiceManager sm = ServiceManager.getInstance();
		IConsoleService cs = sm.getConsoleService();
		try (IRenderingContext context = cs.getRenderingContext()) {
			ITheme theme = ThemeManager.getThemeManager().getCurrentTheme();

			if (_needsFullRedraw) {
				context.clearScreen(theme.getBackgroundColor());
				_rootView.draw();
			}
			_needsLineRedraw = false;
			_needsFullRedraw = false;

			drawPoint(context);
		}
	}

	public void drawPoint(IRenderingContext context) {

	}

	protected boolean _needsFullRedraw = true;
	protected boolean _needsLineRedraw = false;

	public void setNeedsFullRedraw() {
		_needsFullRedraw = true;
	}

	public void setNeedsLineRedraw() {
		_needsLineRedraw = true;
	}

	public void refresh() {
		draw();
	}

}
