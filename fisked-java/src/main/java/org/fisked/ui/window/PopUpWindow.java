package org.fisked.ui.window;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.ui.drawing.View;
import org.fisked.ui.screen.Screen;
import org.fisked.util.models.Point;
import org.fisked.util.models.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PopUpWindow extends Window {
	private final static Logger LOG = LoggerFactory.getLogger(PopUpWindow.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(PopUpWindow.class);

	protected View _containerView;
	protected View _mainView;

	private static Rectangle calculateWindowRect() {
		try (IBehaviorConnection<IWindowManager> wmBC = BEHAVIORS.getBehaviorConnection(IWindowManager.class).get()) {
			Screen screen = wmBC.getBehavior().getPrimaryScreen();
			return new Rectangle(0, 0, screen.getSize().getWidth(), screen.getSize().getHeight());
		} catch (Exception e) {
			LOG.error("Could not get window manager: ", e);
			return new Rectangle(0, 0, 0, 0);
		}
	}

	public PopUpWindow(String name) {
		super(calculateWindowRect(), name);
	}

	public void setupPopupView(View view) {
		_containerView = new View(new Rectangle(new Point(0, 0), _windowRect.getSize()));
		_mainView = view;
		_containerView.addSubview(_mainView);
		setRootView(_containerView);
	}

}
