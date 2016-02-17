package org.fisked.buffer.drawing;

import java.util.ArrayList;
import java.util.List;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Rectangle;
import org.fisked.responder.Event;
import org.fisked.responder.IInputRecognizer;
import org.fisked.responder.RecognitionState;
import org.fisked.theme.ThemeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class View implements IInputRecognizer, IDrawable {
	private final static Logger LOG = LoggerFactory.getLogger(View.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(View.class);
	private final Rectangle _bounds;
	private View _parent;
	private final List<View> _subviews = new ArrayList<>();
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
		if (_parent == null)
			return _bounds;

		Rectangle parentRect = _parent.getClippingRect();
		Rectangle clipRect = new Rectangle(parentRect.getOrigin().getX() + _bounds.getOrigin().getX(),
				parentRect.getOrigin().getY() + _bounds.getOrigin().getY(), _bounds.getSize().getWidth(),
				_bounds.getSize().getHeight());

		return clipRect;
	}

	@Override
	public RecognitionState recognizesInput(Event input) {
		boolean maybeRecognized = false;
		for (View view : _subviews) {
			RecognitionState state = view.recognizesInput(input);
			if (state == RecognitionState.Recognized) {
				return RecognitionState.Recognized;
			} else if (state == RecognitionState.MaybeRecognized) {
				maybeRecognized = true;
			}
		}
		return maybeRecognized ? RecognitionState.MaybeRecognized : RecognitionState.NotRecognized;
	}

	public Color getParentBackgroundColor() {
		if (_parent == null)
			return null;
		if (_parent._backgroundColor == null)
			return _parent._backgroundColor;
		return _parent.getParentBackgroundColor();
	}

	public Color getBackgroundColor() {
		if (_backgroundColor != null)
			return _backgroundColor;
		Color parentColor = getParentBackgroundColor();
		if (parentColor != null)
			return parentColor;
		return ThemeManager.getThemeManager().getCurrentTheme().getBackgroundColor();
	}

	@Override
	public void draw() {
		try (IBehaviorConnection<IConsoleService> consoleBC = BEHAVIORS.getBehaviorConnection(IConsoleService.class)
				.get()) {
			try (IRenderingContext context = consoleBC.getBehavior().getRenderingContext()) {
				Rectangle rect = getClippingRect();
				drawInRect(rect, context);
				_subviews.forEach(subview -> subview.draw());
			}
		} catch (Exception e) {
			LOG.error("Can't get console service: ", e);
		}
	}

	public void drawInRect(Rectangle drawingRect, IRenderingContext context) {
		if (_backgroundColor != null && _backgroundColor.equals(getParentBackgroundColor())) {

			context.clearRect(drawingRect, _backgroundColor);
		}
	}

}
