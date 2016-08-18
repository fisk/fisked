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
package org.fisked.util.datastructure;

import java.util.TreeMap;

import org.fisked.util.Wrapper;
import org.fisked.util.models.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Only supports disjoint ranges for now. Will improve if there is need...
 */
public class IntervalTree<T> {
	private final static Logger LOG = LoggerFactory.getLogger(IntervalTree.class);

	private static class Entry<T> {
		Range _range;
		T _value;

		Entry(Range range, T value) {
			_range = range;
			_value = value;
		}
	}

	private final TreeMap<Integer, Entry<T>> _tree = new TreeMap<>();

	public void add(Range range, T value) {
		LOG.debug("Add: " + range);
		_tree.put(range.getStart(), new Entry<T>(range, value));
	}

	public void deleteFirstIntersect(Range range) {
		Entry<T> entry = _tree.floorEntry(range.getStart()).getValue();
		if (entry != null) {
			if (entry._range.intersection(range).getLength() != 0) {
				_tree.remove(entry._range.getStart(), entry);
			}
		} else {
			entry = _tree.ceilingEntry(range.getStart()).getValue();
			if (entry != null) {
				if (entry._range.intersection(range).getLength() != 0) {
					_tree.remove(entry._range.getStart(), entry);
				}
			}
		}
	}

	public int getStart() {
		return _tree.firstKey();
	}

	public interface ForEach<T> {
		void doit(Range range, T value);
	}

	public void forEach(ForEach<T> closure) {
		_tree.forEach((Integer start, Entry<T> entry) -> {
			closure.doit(entry._range, entry._value);
		});
	}

	public void forEachReverse(ForEach<T> closure) {
		_tree.descendingMap().forEach((Integer start, Entry<T> entry) -> {
			closure.doit(entry._range, entry._value);
		});
	}

	public void forEachIntersect(Range range, ForEach<T> closure) {
		int next = range.getStart();
		boolean first = true;
		for (;;) {
			Entry<T> entry;
			if (first) {
				java.util.Map.Entry<Integer, Entry<T>> innerEntry = _tree.floorEntry(next);
				if (innerEntry == null) {
					innerEntry = _tree.ceilingEntry(next);
					if (innerEntry == null)
						return;
				}
				entry = innerEntry.getValue();
			} else {
				java.util.Map.Entry<Integer, Entry<T>> innerEntry = _tree.ceilingEntry(next);
				if (innerEntry == null)
					return;
				entry = innerEntry.getValue();
			}
			LOG.debug("Intersect try: " + next + range + ", " + entry._range);
			if (range.intersection(entry._range) != null) {
				LOG.debug("Intersected");
				closure.doit(entry._range, entry._value);
			}
			next = entry._range.getEnd();
			if (entry._range.getLength() == 0) {
				next++;
			}
			first = false;
		}
	}

	public boolean isIntersecting(Range range) {
		Wrapper<Boolean> result = new Wrapper<>(false);
		forEachIntersect(range, (Range intersectRange, T value) -> {
			result.setValue(true);
		});
		return result.getValue();
	}

	public int size() {
		return _tree.size();
	}

	public boolean isEmpty() {
		return size() == 0;
	}

}