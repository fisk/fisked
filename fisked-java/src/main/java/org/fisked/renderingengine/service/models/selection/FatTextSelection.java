package org.fisked.renderingengine.service.models.selection;

import java.util.List;

import org.fisked.renderingengine.service.models.Range;

public class FatTextSelection extends TextSelection {
	private final List<Range> _ranges;

	public FatTextSelection(SelectionMode mode, String text, List<Range> ranges) {
		super(mode, text);
		_ranges = ranges;
	}

	public List<Range> getRanges() {
		return _ranges;
	}

}
