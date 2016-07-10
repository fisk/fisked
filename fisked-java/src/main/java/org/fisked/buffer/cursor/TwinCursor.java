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

import org.fisked.buffer.cursor.traverse.ITraversable;
import org.fisked.buffer.cursor.traverse.IVertexOrderer;
import org.fisked.buffer.cursor.traverse.IVisitor;
import org.fisked.util.models.Range;

public class TwinCursor implements ITraversable {
	private Cursor _primaryCursor;
	private Cursor _otherCursor;

	public TwinCursor(Cursor primaryCursor, Cursor otherCursor) {
		_primaryCursor = primaryCursor;
		_otherCursor = otherCursor;
	}

	public TwinCursor(Cursor cursor) {
		_primaryCursor = cursor;
		_otherCursor = null;
	}

	@Override
	public ITraversable clone() {
		return new TwinCursor((Cursor) _primaryCursor.clone(), (Cursor) _otherCursor.clone());
	}

	public void resetOther() {
		_otherCursor = (Cursor) _primaryCursor.clone();
	}

	public void clearOtherSorted() {
		Range range = getOtherRange();
		_primaryCursor.setCharIndex(range.getStart(), true);
		_otherCursor = null;
	}

	public void clearOther() {
		_otherCursor = null;
	}

	public Cursor getOther() {
		return _otherCursor;
	}

	public Cursor getPrimary() {
		return _primaryCursor;
	}

	public Range getOtherRange() {
		Cursor primary = _primaryCursor;
		if (_otherCursor == null) {
			return new Range(primary.getCharIndex(), 0);
		}
		Cursor other = _otherCursor;
		if (other == null) {
			return null;
		}
		int start = primary.getCharIndex();
		int end = other.getCharIndex();
		return new Range(start, end - start);
	}

	public Range getSortedOtherRange() {
		Cursor primary = _primaryCursor;
		if (_otherCursor == null) {
			return new Range(primary.getCharIndex(), 0);
		}
		Cursor other = _otherCursor;
		if (other == null) {
			return null;
		}
		int start = Math.min(primary.getCharIndex(), other.getCharIndex());
		int end = Math.max(primary.getCharIndex(), other.getCharIndex());
		return new Range(start, end - start);
	}

	public void switchOther() {
		Cursor other = _otherCursor;
		_otherCursor = _primaryCursor;
		_primaryCursor = other;
	}

	@Override
	public boolean traverse(IVertexOrderer orderer, IVisitor visitor) {
		return orderer.traverse(this, visitor);
	}
}
