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

import java.util.concurrent.atomic.AtomicReference;

import org.fisked.buffer.cursor.traverse.CursorDFSOrderer;
import org.fisked.buffer.cursor.traverse.CursorPrimaryOrderer;
import org.fisked.buffer.cursor.traverse.CursorReverseDFSOrderer;
import org.fisked.buffer.cursor.traverse.IFilterVisitor;
import org.fisked.buffer.cursor.traverse.ITraversable;
import org.fisked.buffer.cursor.traverse.IVertexOrderer;
import org.fisked.buffer.cursor.traverse.IVisitor;
import org.fisked.buffer.cursor.traverse.Traverser;
import org.fisked.text.TextLayout;

public class CursorCollection implements ITraversable {
	private final TextLayout _layout;
	private HierarchyCursor _root;

	public CursorCollection(TextLayout layout) {
		_layout = layout;
	}

	public void init(int charIndex) {
		Cursor cursor = Cursor.makeCursorFromCharIndex(charIndex, _layout);
		TwinCursor twin = new TwinCursor(cursor);
		HierarchyCursor hierarchy = new HierarchyCursor(twin);
		_root = hierarchy;
	}

	public void init() {
		init(0);
	}

	public void collapseCursors(int charIndex) {
		init(charIndex);
	}

	public ITraversable getRoot() {
		return _root;
	}

	public <T extends ITraversable, O extends IVertexOrderer> void doFiltered(IFilterVisitor<T> visitor, O orderer) {
		Traverser<TwinCursor, O> traverser = new Traverser<>(visitor, this, orderer);
		traverser.traverse();
	}

	public <T extends ITraversable> void doFiltered(IFilterVisitor<T> visitor) {
		doFiltered(visitor, new CursorDFSOrderer());
	}

	public <T extends ITraversable> void doFilteredReverse(IFilterVisitor<T> visitor) {
		doFiltered(visitor, new CursorReverseDFSOrderer());
	}

	public Cursor getPrimaryCursor() {
		AtomicReference<Cursor> cursorRef = new AtomicReference<>();
		IFilterVisitor<Cursor> visitor = new IFilterVisitor<Cursor>() {
			@Override
			public boolean visit(Cursor cursor) {
				cursorRef.set(cursor);
				return false;
			}
		};
		doFiltered(visitor, new CursorPrimaryOrderer());
		return cursorRef.get();
	}

	public interface DoTwinTransplantClosure {
		public void transplant(HierarchyCursor parent, TwinCursor child, int childIndex);
	}

	public void doTwinTransplant(DoTwinTransplantClosure closure) {

	}

	@Override
	public boolean traverse(IVertexOrderer orderer, IVisitor visitor) {
		return orderer.traverse(this, visitor);
	}

	@Override
	public ITraversable clone() {
		CursorCollection collection = new CursorCollection(_layout);
		collection._root = (HierarchyCursor) _root.clone();
		return collection;
	}
}
