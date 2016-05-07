package org.fisked.renderingengine.service.models.selection;

public class TextSelection {
	private final SelectionMode _mode;
	private final String _text;

	public TextSelection(SelectionMode mode, String text) {
		_mode = mode;
		_text = text;
	}

	public SelectionMode getSelectionMode() {
		return _mode;
	}

	public String getText() {
		return _text;
	}

	public TextSelection withMode(SelectionMode mode) {
		return new TextSelection(mode, _text);
	}

}
