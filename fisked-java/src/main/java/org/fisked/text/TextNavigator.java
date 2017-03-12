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
package org.fisked.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.cursor.Cursor;
import org.fisked.buffer.cursor.traverse.CursorStatus;
import org.fisked.buffer.cursor.traverse.IFilterVertexVisitor;
import org.fisked.text.TextLayout.InvalidLocationException;
import org.fisked.ui.buffer.BufferWindow;
import org.fisked.util.models.Point;
import org.fisked.util.models.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextNavigator {
	private final TextLayout _layout;
	private final Buffer _buffer;
	private final BufferWindow _window;

	private final static Logger LOG = LoggerFactory.getLogger(TextNavigator.class);

	private Cursor getPrimaryCursor() {
		return _buffer.getCursorCollection().getPrimaryCursor();
	}

	private boolean isPrimary(Cursor cursor) {
		return cursor == getPrimaryCursor();
	}

	private Buffer getBuffer() {
		return _buffer;
	}

	private int getIndex(Cursor cursor) {
		return cursor.getCharIndex();
	}

	private Point getAbsolutePoint(Cursor cursor) {
		return cursor.getAbsolutePoint();
	}

	private void setAbsolutePoint(Cursor cursor, Point point, boolean updateLastColumn) {
		try {
			cursor.setAbsolutePoint(point, updateLastColumn);
		} catch (InvalidLocationException e) {
			LOG.error("Invalid location: " + point.toString());
		}
	}

	private void setIndex(Cursor cursor, int index, boolean updateLastColumn) {
		cursor.setCharIndex(index, false);
		if (isPrimary(cursor)) {
			scrollDownIfNeeded();
			scrollUpIfNeeded();
		}
		cursor.setCharIndex(index, updateLastColumn);
		_window.setNeedsFullRedraw();
	}

	public void moveToIndexAndScroll(Cursor cursor, int index) {
		setIndex(cursor, index, true);
	}

	private int getLastColumn(Cursor cursor) {
		return cursor.getLastColumn();
	}

	public TextNavigator(BufferWindow window) {
		_window = window;
		_buffer = window.getBuffer();
		_layout = _buffer.getTextLayout();
	}

	public void scrollUp() {
		Rectangle rect = _layout.getClippingRect();
		int y = Math.max(rect.getOrigin().getY() - 1, 0);
		Rectangle newRect = new Rectangle(new Point(rect.getOrigin().getX(), y), rect.getSize());
		_layout.setClippingRect(newRect);
		_layout.setNeedsLayout();
	}

	public void scrollDown() {
		Buffer buffer = getBuffer();
		Rectangle rect = _layout.getClippingRect();
		int prevY = rect.getOrigin().getY();
		int maxY = _layout.getAbsoluteLogicalPointForCharIndex(buffer.length()).getY();
		if (prevY == maxY) {
			// Can't scroll below last line.
			return;
		}
		int y = prevY + 1;
		Rectangle newRect = new Rectangle(new Point(rect.getOrigin().getX(), y), rect.getSize());
		_layout.setClippingRect(newRect);
		_layout.setNeedsLayout();
	}

	public void moveLeft() {
		IFilterVertexVisitor<Cursor> visitor = new IFilterVertexVisitor<Cursor>() {
			@Override
			public boolean visit(Cursor cursor) {
				int newIndex = getIndex(cursor) - 1;
				if (newIndex >= 0) {
					setIndex(cursor, newIndex, true);
				}
				return true;
			}
		};
		_window.getBuffer().getCursorCollection().doFiltered(visitor, CursorStatus.ACTIVE);
	}

	public void moveRight() {
		IFilterVertexVisitor<Cursor> visitor = new IFilterVertexVisitor<Cursor>() {
			@Override
			public boolean visit(Cursor cursor) {
				int newIndex = getIndex(cursor) + 1;
				if (newIndex <= _buffer.getCharSequence().length()) {
					setIndex(cursor, newIndex, true);
				}
				return true;
			}
		};
		_window.getBuffer().getCursorCollection().doFiltered(visitor, CursorStatus.ACTIVE);
	}

	public void moveDown() {
		IFilterVertexVisitor<Cursor> visitor = new IFilterVertexVisitor<Cursor>() {
			@Override
			public boolean visit(Cursor cursor) {
				int lastLine = getAbsolutePoint(cursor).getY();
				int lastColumn = getLastColumn(cursor);
				Point newPoint = new Point(lastColumn, lastLine + 1);
				int charIndex = _buffer.length();
				try {
					charIndex = _layout.getCharIndexForAbsoluteLogicalPoint(newPoint);
				} catch (InvalidLocationException e) {
				}
				setIndex(cursor, charIndex, false);
				return true;
			}
		};
		_window.getBuffer().getCursorCollection().doFiltered(visitor, CursorStatus.ACTIVE);
	}

	public void moveUp() {
		IFilterVertexVisitor<Cursor> visitor = new IFilterVertexVisitor<Cursor>() {
			@Override
			public boolean visit(Cursor cursor) {
				int lastLine = getAbsolutePoint(cursor).getY();
				int lastColumn = getLastColumn(cursor);
				Point newPoint = new Point(lastColumn, lastLine - 1);
				int charIndex = 0;
				try {
					charIndex = _layout.getCharIndexForAbsoluteLogicalPoint(newPoint);
				} catch (InvalidLocationException e) {
				}
				setIndex(cursor, charIndex, false);
				return true;
			}
		};
		_window.getBuffer().getCursorCollection().doFiltered(visitor, CursorStatus.ACTIVE);
	}

	public void moveToTheBeginningOfLine() {
		IFilterVertexVisitor<Cursor> visitor = new IFilterVertexVisitor<Cursor>() {
			@Override
			public boolean visit(Cursor cursor) {
				int newIndex = getIndex(cursor);
				if (newIndex == 0) {
					return true;
				}
				if (!String.valueOf(_buffer.getCharSequence().charAt(newIndex - 1)).matches(".")) {
					return true;
				}
				newIndex--;
				while (newIndex >= 0 && String.valueOf(_buffer.getCharSequence().charAt(newIndex)).matches(".")) {
					newIndex--;
				}
				setIndex(cursor, ++newIndex, true);
				return true;
			}
		};
		_window.getBuffer().getCursorCollection().doFiltered(visitor, CursorStatus.ACTIVE);
	}

	public void moveToTheEndOfLine() {
		IFilterVertexVisitor<Cursor> visitor = new IFilterVertexVisitor<Cursor>() {
			@Override
			public boolean visit(Cursor cursor) {
				int newIndex = getIndex(cursor);
				if (newIndex == _buffer.getCharSequence().length()) {
					return true;
				}
				if (!String.valueOf(_buffer.getCharSequence().charAt(newIndex)).matches(".")) {
					return true;
				}
				newIndex++;
				while (newIndex < _buffer.getCharSequence().length()
						&& String.valueOf(_buffer.getCharSequence().charAt(newIndex)).matches(".")) {
					newIndex++;
				}
				setIndex(cursor, newIndex, true);
				return true;
			}
		};
		_window.getBuffer().getCursorCollection().doFiltered(visitor, CursorStatus.ACTIVE);
	}

	Pattern _nextWordPattern = Pattern.compile("\\s([^\\s]+)");

	public void moveToNextWord() {
		IFilterVertexVisitor<Cursor> visitor = new IFilterVertexVisitor<Cursor>() {
			@Override
			public boolean visit(Cursor cursor) {
				int index = getIndex(cursor);
				CharSequence string = _buffer.getCharSequence();
				Matcher matcher = _nextWordPattern.matcher(string);
				if (matcher.find(index)) {
					int newIndex = matcher.start(1);
					setIndex(cursor, newIndex, true);
				}
				return true;
			}
		};
		_window.getBuffer().getCursorCollection().doFiltered(visitor, CursorStatus.ACTIVE);
	}

	Pattern _endOfWordPattern = Pattern.compile("([^\\s]+)(\\s|$)");

	public void moveToEndOfWord() {
		IFilterVertexVisitor<Cursor> visitor = new IFilterVertexVisitor<Cursor>() {
			@Override
			public boolean visit(Cursor cursor) {
				int index = getIndex(cursor) + 1;
				CharSequence string = _buffer.getCharSequence();
				if (index >= string.length())
					return true;
				Matcher matcher = _endOfWordPattern.matcher(string);
				if (matcher.find(index)) {
					int newIndex = matcher.end(1) - 1;
					if (newIndex > 0) {
						setIndex(cursor, newIndex, true);
					}
				}
				return true;
			}
		};
		_window.getBuffer().getCursorCollection().doFiltered(visitor, CursorStatus.ACTIVE);
	}

	Pattern _previousWordPattern = Pattern.compile("([^\\s]+)(\\s|$)");

	public void moveToPreviousWord() {
		IFilterVertexVisitor<Cursor> visitor = new IFilterVertexVisitor<Cursor>() {
			@Override
			public boolean visit(Cursor cursor) {
				int index = getIndex(cursor);
				CharSequence string = _buffer.getCharSequence();
				StringBuilder reverse = new StringBuilder(_buffer.getCharSequence()).reverse();
				int length = string.length();
				Matcher matcher = _previousWordPattern.matcher(reverse);
				if (matcher.find(length - index)) {
					int newIndex = matcher.end(1);
					setIndex(cursor, length - newIndex, true);
				}
				return true;
			}
		};
		_window.getBuffer().getCursorCollection().doFiltered(visitor, CursorStatus.ACTIVE);
	}

	public void moveCursorDownIfNeeded() {
		Point point = getPrimaryCursor().getAbsolutePoint();
		while (point.getY() < getBuffer().getTextLayout().getClippingRect().getOrigin().getY()) {
			moveDown();
			point = getPrimaryCursor().getAbsolutePoint();
		}
	}

	public void moveCursorUpIfNeeded() {
		Point point = getPrimaryCursor().getAbsolutePoint();
		Rectangle rect = _layout.getClippingRect();
		while (point.getY() >= rect.getOrigin().getY() + rect.getSize().getHeight()) {
			moveUp();
			point = getPrimaryCursor().getAbsolutePoint();
			rect = _layout.getClippingRect();
		}
	}

	public void scrollUpIfNeeded() {
		Point point = getPrimaryCursor().getAbsolutePoint();
		while (point.getY() < getBuffer().getTextLayout().getClippingRect().getOrigin().getY()) {
			scrollUp();
			point = getPrimaryCursor().getAbsolutePoint();
			_window.setNeedsFullRedraw();
		}
	}

	public void scrollDownIfNeeded() {
		Point point = getPrimaryCursor().getAbsolutePoint();
		Rectangle rect = _layout.getClippingRect();
		while (point.getY() >= rect.getOrigin().getY() + rect.getSize().getHeight()) {
			scrollDown();
			point = getPrimaryCursor().getAbsolutePoint();
			rect = _layout.getClippingRect();
			_window.setNeedsFullRedraw();
		}
	}

	public void moveToStart() {
		IFilterVertexVisitor<Cursor> visitor = new IFilterVertexVisitor<Cursor>() {
			@Override
			public boolean visit(Cursor cursor) {
				setIndex(cursor, 0, true);
				return true;
			}
		};
		_window.getBuffer().getCursorCollection().doFiltered(visitor, CursorStatus.ACTIVE);
	}

	public void moveToEnd() {
		IFilterVertexVisitor<Cursor> visitor = new IFilterVertexVisitor<Cursor>() {
			@Override
			public boolean visit(Cursor cursor) {
				setIndex(cursor, getBuffer().getCharSequence().length(), true);
				return true;
			}
		};
		_window.getBuffer().getCursorCollection().doFiltered(visitor, CursorStatus.ACTIVE);
	}
}
