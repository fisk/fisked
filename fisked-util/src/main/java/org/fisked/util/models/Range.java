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
package org.fisked.util.models;

public class Range implements Comparable<Range> {
	private final int _start;
	private final int _length;

	public Range(int start, int length) {
		_start = start;
		_length = length;
	}

	public int getStart() {
		return _start;
	}

	public int getLength() {
		return _length;
	}

	public int getEnd() {
		return _start + _length;
	}

	public Range intersection(Range range) {
		int start = Math.max(getStart(), range.getStart());
		int end = Math.min(getEnd(), range.getEnd());
		if (end <= start) {
			return null;
		}
		return new Range(start, end - start);
	}

	@Override
	public int compareTo(Range o) {
		int result = Integer.compare(_start, o._start);
		if (result == 0) {
			return Integer.compare(_length, o._length);
		}
		return result;
	}

	@Override
	public String toString() {
		return "{ " + _start + ", " + (_start + _length) + " }";
	}

	public int getStartSorted() {
		int start = getStart();
		int end = getEnd();
		if (start > end) {
			return end;
		}
		return start;
	}
}
