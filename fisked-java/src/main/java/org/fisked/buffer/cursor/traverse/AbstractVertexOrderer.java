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
package org.fisked.buffer.cursor.traverse;

import org.fisked.buffer.cursor.Cursor;
import org.fisked.buffer.cursor.CursorCollection;
import org.fisked.buffer.cursor.NullCursor;
import org.fisked.buffer.cursor.TwinCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractVertexOrderer implements IVertexOrderer {
	private final CursorStatus _cursorStatus;

	public AbstractVertexOrderer(CursorStatus status) {
		_cursorStatus = status;
	}

	protected boolean shouldVisit(ITraversable traversable) {
		switch (_cursorStatus) {
		case ALL:
			return true;
		case ACTIVE:
		case INACTIVE:
			return traversable.getCursorStatus() == _cursorStatus;
		}
		return false;
	}

	private final static Logger LOG = LoggerFactory.getLogger(AbstractVertexOrderer.class);

	@Override
	public boolean traverse(TwinCursor traversable, IVertexVisitor visitor) {
		if (!shouldVisit(traversable)) {
			LOG.debug("Cursor " + traversable + " blocked: " + traversable.getCursorStatus());
			return true;
		}
		if (!visitor.visit(traversable)) {
			return false;
		}
		return traversable.getPrimary().traverse(this, visitor);
	}

	@Override
	public boolean traverse(CursorCollection traversable, IVertexVisitor visitor) {
		if (!shouldVisit(traversable)) {
			return true;
		}
		if (!visitor.visit(traversable)) {
			return false;
		}
		return traversable.getRoot().traverse(this, visitor);
	}

	@Override
	public boolean traverse(Cursor traversable, IVertexVisitor visitor) {
		if (!shouldVisit(traversable)) {
			return true;
		}
		return visitor.visit(traversable);
	}

	@Override
	public boolean traverse(NullCursor traversable, IVertexVisitor visitor) {
		return true;
	}
}
