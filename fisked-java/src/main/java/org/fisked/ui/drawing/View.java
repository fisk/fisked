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
import org.fisked.responder.IInputResponder;
import org.fisked.responder.RecognitionState;
import org.fisked.theme.ThemeManager;
import org.fisked.util.models.Color;
import org.fisked.util.models.Rectangle;
import org.fisked.util.models.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class View implements IInputResponder, IDrawable {
	private final static Logger LOG = LoggerFactory.getLogger(View.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(View.class);
	private Rectangle _bounds;
	private Rectangle _frame;
	private Rectangle _oldFrame;
	private View _parent;
	private Size _parentSize;
	private final List<View> _subviews = new ArrayList<>();
	private Color _backgroundColor;

	public View(Rectangle frame) {
		_frame = frame;
		_oldFrame = frame;
		_bounds = new Rectangle(0, 0, frame.getSize().getWidth(), frame.getSize().getHeight());
	}

	private void setParent(View view) {
		if (view != null) {
			_parentSize = view._bounds.getSize();
		} else {
			_parentSize = null;
		}
		_parent = view;
	}

	public void addSubview(View subview) {
		_subviews.add(subview);
		subview.setParent(this);
	}

	public void removeFromParent() {
		if (_parent != null) {
			_parent._subviews.remove(this);
			setParent(null);
		}
	}

	public Rectangle getClippingRect() {
		if (_parent == null)
			return _frame;

		Rectangle parentRect = _parent.getClippingRect();
		Rectangle clipRect = new Rectangle(parentRect.getOrigin().getX() + _frame.getOrigin().getX(),
				parentRect.getOrigin().getY() + _frame.getOrigin().getY(), _frame.getSize().getWidth(),
				_frame.getSize().getHeight());

		return clipRect;
	}

	private View _lastRecognized;

	@Override
	public RecognitionState recognizesInput(Event input) {
		boolean maybeRecognized = false;
		for (View view : _subviews) {
			_lastRecognized = view;
			RecognitionState state = view.recognizesInput(input);
			if (state == RecognitionState.Recognized) {
				return RecognitionState.Recognized;
			} else if (state == RecognitionState.MaybeRecognized) {
				maybeRecognized = true;
			}
		}
		return maybeRecognized ? RecognitionState.MaybeRecognized : RecognitionState.NotRecognized;
	}

	@Override
	public void onRecognize() {
		_lastRecognized.onRecognize();
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
		LOG.debug("Draw view " + this + " rect before layout: " + getClippingRect());
		layoutIfNeeded();
		LOG.debug("Draw rect " + this + " after layout: " + getClippingRect());
		try (IBehaviorConnection<IConsoleService> consoleBC = BEHAVIORS.getBehaviorConnection(IConsoleService.class)
				.get()) {
			try (IRenderingContext context = consoleBC.getBehavior().getRenderingContext(getClippingRect())) {
				drawInRect(_bounds, context);
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
	public static final int AUTORESIZE_MASK_BOTTOM = 1 << 5;

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

	private void layoutSubviews(Size oldParentSize, Size newParentSize) {
		int left, right, top, bottom;
		boolean hasLeft = false, hasRight = false, hasTop = false, hasBottom = false;
		Rectangle oldFrame = _oldFrame;
		double ratio;

		left = oldFrame.getOrigin().getX();
		right = oldParentSize.getWidth() - oldFrame.getOrigin().getX() - oldFrame.getSize().getWidth();

		if (hasAutoResizeMask(AUTORESIZE_MASK_LEFT)
				&& (hasAutoResizeMask(AUTORESIZE_MASK_HORIZONTAL) || !hasAutoResizeMask(AUTORESIZE_MASK_RIGHT))) {
			hasLeft = true;
		} else {
			ratio = (double) left / (double) oldParentSize.getWidth();
			left = (int) Math.round(ratio * newParentSize.getWidth());
		}
		if (hasAutoResizeMask(AUTORESIZE_MASK_RIGHT)
				&& (hasAutoResizeMask(AUTORESIZE_MASK_HORIZONTAL) || !hasAutoResizeMask(AUTORESIZE_MASK_LEFT))) {
			hasRight = true;
		} else {
			ratio = (double) right / (double) oldParentSize.getWidth();
			right = (int) Math.round(ratio * newParentSize.getWidth());
		}

		if (!hasAutoResizeMask(AUTORESIZE_MASK_HORIZONTAL)) {
			if (!hasLeft && !hasRight) {
				int center = oldFrame.getOrigin().getX() + oldFrame.getSize().getWidth() / 2;
				ratio = (double) center / (double) oldParentSize.getWidth();
				center = (int) Math.round(ratio * newParentSize.getWidth());
				left = center - oldFrame.getSize().getWidth() / 2;
				right = center + oldFrame.getSize().getWidth() / 2;
			} else if (hasRight) {
				left = newParentSize.getWidth() - oldFrame.getSize().getWidth() - right;
			} else {
				right = newParentSize.getWidth() - oldFrame.getSize().getWidth() - left;
			}
		}

		top = oldFrame.getOrigin().getY();
		bottom = oldParentSize.getHeight() - top - oldFrame.getSize().getHeight();
		LOG.debug("View " + this + ", top: " + top);
		LOG.debug("View " + this + ", bottom: " + bottom);

		if (hasAutoResizeMask(AUTORESIZE_MASK_TOP)
				&& (hasAutoResizeMask(AUTORESIZE_MASK_VERTICAL) || !hasAutoResizeMask(AUTORESIZE_MASK_BOTTOM))) {
			hasTop = true;
			LOG.debug("View " + this + "has top");
		} else {
			ratio = (double) top / (double) oldParentSize.getHeight();
			top = (int) Math.round(ratio * newParentSize.getHeight());
			LOG.debug("View " + this + ", top#2: " + top + ", ratio: " + ratio);
		}
		if (hasAutoResizeMask(AUTORESIZE_MASK_BOTTOM)
				&& (hasAutoResizeMask(AUTORESIZE_MASK_VERTICAL) || !hasAutoResizeMask(AUTORESIZE_MASK_TOP))) {
			hasBottom = true;
			LOG.debug("View " + this + "has bottom");
		} else {
			ratio = (double) bottom / (double) oldParentSize.getHeight();
			bottom = (int) Math.round(ratio * newParentSize.getHeight());
			LOG.debug("View " + this + ", bottom#2: " + bottom + ", ratio: " + ratio);
		}

		if (!hasAutoResizeMask(AUTORESIZE_MASK_VERTICAL)) {
			if (!hasTop && !hasBottom) {
				int center = oldFrame.getOrigin().getY() + oldFrame.getSize().getHeight() / 2;
				LOG.debug("View#3 " + this + ", center: " + center);
				ratio = (double) center / (double) oldParentSize.getHeight();
				LOG.debug("View#3 " + this + ", ratio: " + ratio);
				center = (int) Math.round(ratio * newParentSize.getHeight());
				LOG.debug("View#4 " + this + ", center: " + center);
				top = center - newParentSize.getHeight() / 2;
				bottom = center + newParentSize.getHeight() / 2;
				LOG.debug("View#4 " + this + ", top: " + top);
				LOG.debug("View#4 " + this + ", bottom: " + bottom);
			} else if (hasBottom) {
				top = newParentSize.getHeight() - oldFrame.getSize().getHeight() - bottom;
				LOG.debug("View#4 " + this + ", hasBottom, top: " + top);
			} else {
				bottom = newParentSize.getHeight() - oldFrame.getSize().getHeight() - top;
				LOG.debug("View#4 " + this + ", !hasBottom, bottom: " + top);
			}
		}

		Rectangle newFrame = new Rectangle(left, top, newParentSize.getWidth() - left - right,
				newParentSize.getHeight() - top - bottom);

		LOG.debug("Layout " + this + ", oldParentSize: " + oldParentSize + ", newParentSize: " + newParentSize
				+ ", oldFrame: " + oldFrame + ", newFrame: " + newFrame);
		LOG.debug("Left: " + left + ", right: " + right + ", top: " + top + ", bottom: " + bottom);

		if (!newFrame.equals(_frame)) {
			_frame = newFrame;
			_bounds = new Rectangle(_bounds.getOrigin(), newFrame.getSize());
			setNeedsDrawing();
			for (View subview : _subviews) {
				subview.setNeedsLayout();
			}
		}
	}

	protected void layoutSubviews() {
		if (_parent == null || _parentSize == null) {
			setNeedsDrawing();
			for (View subview : _subviews) {
				subview.setNeedsLayout();
			}
			return;
		}

		Size oldParentSize = _parentSize;
		Size newParentSize = _parent._bounds.getSize();
		layoutSubviews(oldParentSize, newParentSize);
		_parentSize = newParentSize;
		_oldFrame = _frame;
	}

	public void layoutIfNeeded() {
		if (!_needsLayout) {
			return;
		}
		_needsLayout = false;
		layoutSubviews();
	}

	public void setFrame(Rectangle frame) {
		_frame = frame;
		_bounds = new Rectangle(_bounds.getOrigin(), frame.getSize());
		setNeedsLayout();
		setNeedsDrawing();
	}

	public Rectangle getFrame() {
		return _frame;
	}

	public Rectangle getBounds() {
		return _bounds;
	}

}
