package org.fisked.buffer.drawing;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.responder.Event;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.RecognitionState;
import org.fisked.theme.ITheme;
import org.fisked.theme.ThemeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Window implements IInputResponder, IDrawable {
	private final static Logger LOG = LoggerFactory.getLogger(Window.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(Window.class);

	protected View _rootView;
	protected Rectangle _windowRect;
	protected IInputResponder _primaryResponder;

	public Window(Rectangle windowRect) {
		_windowRect = windowRect;
	}

	public void setRootView(View rootView) {
		_rootView = rootView;
	}

	public View getRootView() {
		return _rootView;
	}

	@Override
	public RecognitionState recognizesInput(Event input) {
		IInputResponder responder = _primaryResponder;
		if (responder == null)
			return RecognitionState.NotRecognized;
		RecognitionState status = responder.recognizesInput(input);
		if (status == RecognitionState.Recognized) {
			setNeedsFullRedraw();
		}
		return status;
	}

	@Override
	public void onRecognize() {
		IInputResponder responder = _primaryResponder;
		if (responder != null)
			responder.onRecognize();
	}

	@Override
	public void draw() {
		try (IBehaviorConnection<IConsoleService> consoleBC = BEHAVIORS.getBehaviorConnection(IConsoleService.class)
				.get()) {
			try (IRenderingContext context = consoleBC.getBehavior().getRenderingContext()) {
				ITheme theme = ThemeManager.getThemeManager().getCurrentTheme();

				if (_needsFullRedraw) {
					context.clearScreen(theme.getBackgroundColor());
					_rootView.draw();
				}
				_needsLineRedraw = false;
				_needsFullRedraw = false;

				drawPoint(context);
			}
		} catch (Exception e) {
			LOG.error("Can't get console service: ", e);
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
