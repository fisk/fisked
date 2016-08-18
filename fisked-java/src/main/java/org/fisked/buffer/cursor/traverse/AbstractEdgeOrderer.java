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
package org.fisked.buffer.cursor.traverse;

import org.fisked.buffer.cursor.Cursor;
import org.fisked.buffer.cursor.CursorCollection;
import org.fisked.buffer.cursor.HierarchyCursor;
import org.fisked.buffer.cursor.NullCursor;
import org.fisked.buffer.cursor.TwinCursor;

public abstract class AbstractEdgeOrderer implements IEdgeOrderer {
	@Override
	public boolean traverseEdge(TwinCursor traversable, IEdgeVisitor visitor) {
		if (!visitor.visitEdge(new IEdge() {

			@Override
			public void set(ITraversable val) {
				traversable.setPrimary((Cursor) val);
			}

			@Override
			public void delete() {
				set(null);
			}

		}, traversable)) {
			return false;
		}
		return traversable.getPrimary().traverse(this, visitor);
	}

	@Override
	public boolean traverseEdge(CursorCollection traversable, IEdgeVisitor visitor) {
		if (!visitor.visitEdge(new IEdge() {

			@Override
			public void set(ITraversable val) {
				traversable.setRoot((HierarchyCursor) val);
			}

			@Override
			public void delete() {
				set(null);
			}

		}, traversable)) {
			return false;
		}
		return traversable.getRoot().traverse(this, visitor);
	}

	@Override
	public boolean traverseEdge(Cursor traversable, IEdgeVisitor visitor) {
		return true;
	}

	@Override
	public boolean traverseEdge(NullCursor traversable, IEdgeVisitor visitor) {
		return true;
	}
}
