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

import org.fisked.buffer.cursor.traverse.CursorEdgeDFSOrderer;
import org.fisked.buffer.cursor.traverse.CursorStatus;
import org.fisked.buffer.cursor.traverse.CursorVertexDFSOrderer;
import org.fisked.buffer.cursor.traverse.CursorVertexPrimaryOrderer;
import org.fisked.buffer.cursor.traverse.CursorVertexReverseDFSOrderer;
import org.fisked.buffer.cursor.traverse.EdgeTraverser;
import org.fisked.buffer.cursor.traverse.IEdgeOrderer;
import org.fisked.buffer.cursor.traverse.IEdgeVisitor;
import org.fisked.buffer.cursor.traverse.IFilterEdgeVisitor;
import org.fisked.buffer.cursor.traverse.IFilterVertexVisitor;
import org.fisked.buffer.cursor.traverse.ITraversable;
import org.fisked.buffer.cursor.traverse.IVertexOrderer;
import org.fisked.buffer.cursor.traverse.IVertexVisitor;
import org.fisked.buffer.cursor.traverse.VertexTraverser;
import org.fisked.text.TextLayout;
import org.fisked.util.Wrapper;

public class CursorCollection implements ITraversable {
	private final TextLayout _layout;
	private HierarchyCursor _root;

	private CursorStatus _cursorStatus = CursorStatus.ACTIVE;

	@Override
	public CursorStatus getCursorStatus() {
		return _cursorStatus;
	}

	@Override
	public void setCursorStatus(CursorStatus cursorStatus) {
		_cursorStatus = cursorStatus;
	}

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

	public HierarchyCursor getRoot() {
		return _root;
	}

	public void setRoot(HierarchyCursor root) {
		_root = root;
	}

	public <T extends ITraversable, O extends IVertexOrderer> void doFiltered(IFilterVertexVisitor<T> visitor,
			O orderer) {
		VertexTraverser<T, O> traverser = new VertexTraverser<>(visitor, this, orderer);
		traverser.traverse();
	}

	public <T extends ITraversable, O extends IEdgeOrderer> void doFiltered(IFilterEdgeVisitor<T> visitor, O orderer) {
		EdgeTraverser<T, O> traverser = new EdgeTraverser<>(visitor, this, orderer);
		traverser.traverse();
	}

	public <T extends ITraversable> void doFiltered(IFilterVertexVisitor<T> visitor, CursorStatus status) {
		doFiltered(visitor, new CursorVertexDFSOrderer(status));
	}

	public <T extends ITraversable> void doFiltered(IFilterEdgeVisitor<T> visitor, CursorStatus status) {
		doFiltered(visitor, new CursorEdgeDFSOrderer(status));
	}

	public <T extends ITraversable> void doFilteredReverse(IFilterVertexVisitor<T> visitor, CursorStatus status) {
		doFiltered(visitor, new CursorVertexReverseDFSOrderer(status));
	}

	public Cursor getPrimaryCursor() {
		Wrapper<Cursor> cursorRef = new Wrapper<>();
		IFilterVertexVisitor<Cursor> visitor = new IFilterVertexVisitor<Cursor>() {
			@Override
			public boolean visit(Cursor cursor) {
				cursorRef.setValue(cursor);
				return false;
			}
		};
		doFiltered(visitor, new CursorVertexPrimaryOrderer(CursorStatus.ACTIVE));
		return cursorRef.getValue();
	}

	public interface DoTwinTransplantClosure {
		public void transplant(HierarchyCursor parent, TwinCursor child, int childIndex);
	}

	public void doTwinTransplant(DoTwinTransplantClosure closure) {

	}

	@Override
	public boolean traverse(IVertexOrderer orderer, IVertexVisitor visitor) {
		return orderer.traverse(this, visitor);
	}

	@Override
	public boolean traverse(IEdgeOrderer orderer, IEdgeVisitor visitor) {
		return orderer.traverseEdge(this, visitor);
	}

	@Override
	public ITraversable clone() {
		CursorCollection collection = new CursorCollection(_layout);
		collection._root = (HierarchyCursor) _root.clone();
		return collection;
	}

	public void addCursorAt(int charIndex) {
		TwinCursor cursor = new TwinCursor(Cursor.makeCursorFromCharIndex(charIndex, _layout));
		_root.addChild(cursor);
	}
}
