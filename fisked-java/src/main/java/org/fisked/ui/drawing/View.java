/*******************************************************************************
 * Copyright (c) 2017, Erik Österlund
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL ERIK ÖSTERLUND BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.fisked.ui.drawing;

import java.util.ArrayList;
import java.util.List;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.responder.Event;
import org.fisked.responder.IInputRecognizer;
import org.fisked.responder.RecognitionState;
import org.fisked.theme.ThemeManager;
import org.fisked.util.models.Color;
import org.fisked.util.models.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class View implements IInputRecognizer, IDrawable {
	private final static Logger LOG = LoggerFactory.getLogger(View.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(View.class);
	private Rectangle _bounds;
	private View _parent;
	private Rectangle _parentBounds;
	private final List<View> _subviews = new ArrayList<>();
	private Color _backgroundColor;

	public View(Rectangle frame) {
		_bounds = frame;
	}

	private void setParent(View view) {
		if (view != null) {
			_parentBounds = view._bounds;
		} else {
			_parentBounds = null;
		}
		_parent = view;
	}

	public void addSubview(View subview) {
		_subviews.add(subview);
		subview.setParent(this);
	}

	public void removeFromParent() {
		_parent._subviews.remove(this);
		setParent(null);
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
		layoutIfNeeded();
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

	public static final int AUTORESIZE_MASK_HORIZONTAL = 1 << 0;
	public static final int AUTORESIZE_MASK_VERTICAL = 1 << 1;
	public static final int AUTORESIZE_MASK_LEFT = 1 << 2;
	public static final int AUTORESIZE_MASK_RIGHT = 1 << 3;
	public static final int AUTORESIZE_MASK_TOP = 1 << 4;
	public static final int AUTORESIZE_MASK_BOTTOM = 1 << 4;

	private int _autoresizeMask = AUTORESIZE_MASK_HORIZONTAL | AUTORESIZE_MASK_VERTICAL | AUTORESIZE_MASK_LEFT
			| AUTORESIZE_MASK_RIGHT | AUTORESIZE_MASK_TOP | AUTORESIZE_MASK_BOTTOM;
	private boolean _needsLayout = false;
	private boolean _needsDrawing = false;

	public void setNeedsDrawing() {
		_needsDrawing = true;
		if (_parent != null) {
			_parent.setNeedsDrawing();
		}
	}

	public void setNeedsLayout() {
		_needsLayout = true;
	}

	public void setAutoResizeMask(int mask) {
		_autoresizeMask = mask;
	}

	public void addAutoResizeMask(int mask) {
		_autoresizeMask = _autoresizeMask | mask;
	}

	protected boolean hasAutoResizeMask(int mask) {
		return (_autoresizeMask & mask) != 0;
	}

	protected void layoutSubviews() {
		Rectangle oldBounds = _bounds;
		Rectangle oldParentBounds = _parentBounds;
		Rectangle newParentBounds = _parent._bounds;

		int left, right, top, bottom;

		// left
		left = oldBounds.getOrigin().getX();
		if (!hasAutoResizeMask(AUTORESIZE_MASK_LEFT)) {
			double ratio = (double) left / (double) oldParentBounds.getSize().getWidth();
			left = (int) Math.round(ratio * newParentBounds.getSize().getWidth());
		}

		// right
		right = oldParentBounds.getSize().getWidth() - oldBounds.getOrigin().getX() - oldBounds.getSize().getWidth();
		if (!hasAutoResizeMask(AUTORESIZE_MASK_RIGHT)) {
			double ratio = (double) right / (double) oldParentBounds.getSize().getWidth();
			right = (int) Math.round(ratio * newParentBounds.getSize().getWidth());
		}

		// top
		top = oldBounds.getOrigin().getY();
		if (!hasAutoResizeMask(AUTORESIZE_MASK_TOP)) {
			double ratio = (double) top / (double) oldParentBounds.getSize().getHeight();
			top = (int) Math.round(ratio * newParentBounds.getSize().getHeight());
		}

		// bottom
		bottom = oldParentBounds.getSize().getHeight() - oldBounds.getOrigin().getY() - oldBounds.getSize().getHeight();
		if (!hasAutoResizeMask(AUTORESIZE_MASK_BOTTOM)) {
			double ratio = (double) bottom / (double) oldParentBounds.getSize().getHeight();
			bottom = (int) Math.round(ratio * newParentBounds.getSize().getHeight());
		}

		Rectangle newBounds = new Rectangle(left, top, newParentBounds.getSize().getWidth() - left - right,
				newParentBounds.getSize().getHeight() - top - bottom);

		if (!newBounds.equals(_bounds)) {
			setNeedsDrawing();
			for (View subview : _subviews) {
				subview.setNeedsLayout();
			}
		}
	}

	public void layoutIfNeeded() {
		if (!_needsLayout) {
			return;
		}
		_needsLayout = false;
		layoutSubviews();
	}

	public void setBounds(Rectangle bounds) {
		_bounds = bounds;
		setNeedsLayout();
	}

	public Rectangle getBounds() {
		return _bounds;
	}

}
