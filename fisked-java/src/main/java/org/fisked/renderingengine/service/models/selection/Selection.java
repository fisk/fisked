package org.fisked.renderingengine.service.models.selection;

import org.fisked.renderingengine.service.models.Range;

public class Selection {
	private final Range _range;
	private final SelectionMode _mode;

	public Selection(int start, int length, SelectionMode mode) {
		this(new Range(start, length), mode);
	}

	public Selection(Range range, SelectionMode mode) {
		_range = range;
		_mode = mode;
	}

	public Range getRange() {
		return _range;
	}

	public SelectionMode getMode() {
		return _mode;
	}
}
