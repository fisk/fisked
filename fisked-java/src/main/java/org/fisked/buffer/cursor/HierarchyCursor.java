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

import java.util.ArrayList;
import java.util.List;

import org.fisked.buffer.cursor.traverse.IEdgeOrderer;
import org.fisked.buffer.cursor.traverse.IEdgeVisitor;
import org.fisked.buffer.cursor.traverse.ITraversable;
import org.fisked.buffer.cursor.traverse.IVertexOrderer;
import org.fisked.buffer.cursor.traverse.IVertexVisitor;

public class HierarchyCursor implements ITraversable {
	private final List<ITraversable> _children = new ArrayList<ITraversable>();
	private ITraversable _primary;

	public HierarchyCursor() {
	}

	public HierarchyCursor(ITraversable primary) {
		_primary = primary;
		addChild(primary);
	}

	public ITraversable getPrimary() {
		return _primary;
	}

	public void setPrimary(ITraversable traversable) {
		_primary = traversable;
	}

	public void addChild(ITraversable child) {
		_children.add(child);
	}

	public List<ITraversable> getChildren() {
		return _children;
	}

	public void replaceChild(int index, ITraversable traversable) {
		_children.remove(index);
		_children.add(index, traversable);
	}

	public void removeChild(int index) {
		_children.remove(index);
	}

	public void clear() {
		_children.clear();
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
		HierarchyCursor result = new HierarchyCursor();
		for (ITraversable child : _children) {
			ITraversable clone = child.clone();
			if (child == _primary) {
				result.setPrimary(clone);
			}
			result.addChild(clone);
		}
		return result;
	}
}
