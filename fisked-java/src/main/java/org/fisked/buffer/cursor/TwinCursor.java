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

import org.fisked.util.traverse.FilterVisitor;
import org.fisked.util.traverse.Order;
import org.fisked.util.traverse.Traversable;
import org.fisked.util.traverse.Traverser;
import org.fisked.util.traverse.Visitor;

public class TwinCursor implements Traversable {
	private Traversable _primaryCursor;
	private Traversable _otherCursor;

	public static Traverser<TwinCursor> getFilterTraverser(Order order, FilterVisitor<TwinCursor> visitor,
			Traversable root) {
		return new Traverser<TwinCursor>(visitor, root, order);
	}

	public TwinCursor(Traversable primaryCursor, Traversable otherCursor) {
		_primaryCursor = primaryCursor;
		_otherCursor = otherCursor;
	}

	public TwinCursor(Traversable cursor) {
		_primaryCursor = cursor;
		_otherCursor = cursor.clone();
	}

	@Override
	public void traverse(Order order, Visitor visitor) {
		_primaryCursor.traverse(order, visitor);
	}

	@Override
	public Traversable clone() {
		return new TwinCursor(_primaryCursor.clone(), _otherCursor.clone());
	}

	public void resetOther() {
		_otherCursor = _primaryCursor.clone();
	}

	public Traversable getOther() {
		return _otherCursor;
	}

	public void switchOther() {
		Traversable other = _otherCursor;
		_otherCursor = _primaryCursor;
		_primaryCursor = other;
	}
}
