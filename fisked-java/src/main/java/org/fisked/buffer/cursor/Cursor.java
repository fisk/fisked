/*******************************************************************************
 * Copyright (c) 2016, Erik Österlund
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
package org.fisked.buffer.cursor;

import org.fisked.renderingengine.service.models.Point;
import org.fisked.text.TextLayout;
import org.fisked.text.TextLayout.InvalidLocationException;
import org.fisked.util.traverse.FilterVisitor;
import org.fisked.util.traverse.Order;
import org.fisked.util.traverse.Traversable;
import org.fisked.util.traverse.Traverser;

public class Cursor implements Traversable {
	private int _charIndex;
	private Point _relativePoint; // relative point is point on screen with view
									// top left being 0, 0
	private Point _absolutePoint; // absolute point is absolute position of
									// cursor from where the text begins
	private int _lastColumn; // last column of any command that changes cursor
								// position side-ways
	private final TextLayout _layout;

	public static Traverser<Cursor> getFilterTraverser(Order order, FilterVisitor<Cursor> visitor, Traversable root) {
		return new Traverser<Cursor>(visitor, root, order);
	}

	protected Cursor(int charIndex, Point relativePoint, Point absolutePoint, int lastColumn, TextLayout layout) {
		_charIndex = charIndex;
		_relativePoint = relativePoint;
		_absolutePoint = absolutePoint;
		_lastColumn = lastColumn;
		_layout = layout;
	}

	public Cursor(TextLayout layout) {
		this(0, new Point(0, 0), new Point(0, 0), 0, layout);
	}

	public static Cursor makeCursorFromCharIndex(int index, TextLayout layout) {
		Point relative = layout.getRelativeLogicalPointForCharIndex(index);
		Point absolute = layout.getAbsoluteLogicalPointForCharIndex(index);
		int column = relative.getX();
		return new Cursor(index, relative, absolute, column, layout);
	}

	public int getCharIndex() {
		return _charIndex;
	}

	public Point getRelativePoint() {
		return _relativePoint;
	}

	public Point getAbsolutePoint() {
		return _absolutePoint;
	}

	public int getLastColumn() {
		return _lastColumn;
	}

	public void setCharIndex(int index, boolean changeLastColumn) {
		Point relativePoint = _layout.getRelativeLogicalPointForCharIndex(index);
		Point absolutePoint = _layout.getAbsoluteLogicalPointForCharIndex(index);
		_charIndex = index;
		_relativePoint = relativePoint;
		_absolutePoint = absolutePoint;
		if (changeLastColumn) {
			if (relativePoint != null) {
				_lastColumn = relativePoint.getX();
			} else {
				_lastColumn = 0;
			}
		}
	}

	public void setRelativePoint(Point relativePoint, boolean changeLastColumn) throws InvalidLocationException {
		int index = _layout.getCharIndexForRelativeLogicalPoint(relativePoint);
		Point absolutePoint = _layout.getAbsoluteLogicalPointForCharIndex(index);
		_charIndex = index;
		_relativePoint = relativePoint;
		_absolutePoint = absolutePoint;
		if (changeLastColumn) {
			_lastColumn = relativePoint.getX();
		}
	}

	public void setAbsolutePoint(Point absolutePoint, boolean changeLastColumn) throws InvalidLocationException {
		int index = _layout.getCharIndexForAbsoluteLogicalPoint(absolutePoint);
		Point relativePoint = _layout.getRelativeLogicalPointForCharIndex(index);
		_charIndex = index;
		_relativePoint = relativePoint;
		_absolutePoint = absolutePoint;
		if (changeLastColumn) {
			_lastColumn = relativePoint.getX();
		}
	}

	@Override
	public Traversable clone() {
		return new Cursor(_charIndex, _relativePoint, _absolutePoint, _lastColumn, _layout);
	}
}
