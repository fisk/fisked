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
package org.fisked.buffer.cursor.test;

import org.fisked.buffer.cursor.Cursor;
import org.fisked.buffer.cursor.CursorCollection;
import org.fisked.buffer.cursor.traverse.CursorStatus;
import org.fisked.buffer.cursor.traverse.IFilterVertexVisitor;
import org.fisked.text.TextLayout;
import org.fisked.util.models.Size;
import org.junit.Assert;
import org.junit.Test;

public class CursorTraversalTest {

	private static class Value<T> {
		private T _value;

		public Value(T value) {
			_value = value;
		}

		public T get() {
			return _value;
		}

		public void set(T value) {
			_value = value;
		}
	}

	@Test
	public void testCursorTraversal() {
		TextLayout layout = new TextLayout("a", new Size(1, 1));
		CursorCollection collection = new CursorCollection(layout);
		collection.init(0);
		Value<Boolean> found = new Value<>(false);
		IFilterVertexVisitor<Cursor> visitor = new IFilterVertexVisitor<Cursor>() {
			@Override
			public boolean visit(Cursor traversable) {
				found.set(true);
				return true;
			}
		};
		collection.doFiltered(visitor, CursorStatus.ALL);
		Assert.assertTrue(found.get());
	}
}
