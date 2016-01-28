package org.fisked.renderingengine.service.models;

import java.text.AttributedCharacterIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.fisked.buffer.BufferTextState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttributedString implements CharSequence {
	private java.text.AttributedString _string;
	private final String _unattributedString;

	private static abstract class Attribute extends java.text.AttributedCharacterIterator.Attribute {
		private static final long serialVersionUID = 1L;

		protected Attribute(String name) {
			super(name);
		}

		public abstract String getANSIAttribute(Object value);
	}

	private static ForegroundColorAttribute _foregroundColorAttribute = new ForegroundColorAttribute();
	private static BackgroundColorAttribute _backgroundColorAttribute = new BackgroundColorAttribute();
	private static BoldAttribute _boldAttribute = new BoldAttribute();

	private abstract static class ColorAttribute extends Attribute {
		private static final long serialVersionUID = 1L;

		public ColorAttribute(String name) {
			super(name);
		}

		public ANSIAttribute ansiAttribute(int base, Color color) {
			return new ANSIAttribute() {
				@Override
				public String toString() {
					return "" + (base + color.getRawColor());
				}
			};
		}
	}

	private static class ForegroundColorAttribute extends ColorAttribute {
		private static final long serialVersionUID = 1L;
		public static final String NAME = "FOREGROUND_COLOR";

		public ForegroundColorAttribute() {
			super(NAME);
		}

		public ANSIAttribute ansiAttribute(Color color) {
			return super.ansiAttribute(30, color);
		}

		@Override
		public String getANSIAttribute(Object value) {
			return ansiAttribute((Color) value).toString();
		}
	}

	private static class BackgroundColorAttribute extends ColorAttribute {
		private static final long serialVersionUID = 1L;
		public static final String NAME = "BACKGROUND_COLOR";

		public BackgroundColorAttribute() {
			super(NAME);
		}

		public ANSIAttribute ansiAttribute(Color color) {
			return super.ansiAttribute(40, color);
		}

		@Override
		public String getANSIAttribute(Object value) {
			return ansiAttribute((Color) value).toString();
		}
	}

	private static class BoldAttribute extends Attribute {
		private static final long serialVersionUID = 1L;
		public static final String NAME = "BOLD";

		public BoldAttribute() {
			super(NAME);
		}

		@Override
		public String getANSIAttribute(Object value) {
			return "1";
		}
	}

	public AttributedString(String string) {
		_string = new java.text.AttributedString(string);
		_unattributedString = string;
	}

	public AttributedString(String string, Face face) {
		this(string);
		setFace(face);
	}

	public void setForegroundColor(Color color) {
		if (_unattributedString.length() == 0)
			return;
		_string.addAttribute(_foregroundColorAttribute, color);
	}

	public void setForegroundColor(Color color, int startIndex, int endIndex) {
		if (_unattributedString.length() == 0)
			return;
		if (endIndex == startIndex)
			return;
		_string.addAttribute(_foregroundColorAttribute, color, startIndex, endIndex);
	}

	public void setBackgroundColor(Color color) {
		if (_unattributedString.length() == 0)
			return;
		_string.addAttribute(_backgroundColorAttribute, color);
	}

	public void setBackgroundColor(Color color, int startIndex, int endIndex) {
		if (_unattributedString.length() == 0)
			return;
		if (endIndex == startIndex)
			return;
		_string.addAttribute(_backgroundColorAttribute, color, startIndex, endIndex);
	}

	public void setBold(int startIndex, int endIndex) {
		if (_unattributedString.length() == 0)
			return;
		if (endIndex == startIndex)
			return;
		_string.addAttribute(_boldAttribute, true, startIndex, endIndex);
	}

	public void setFace(Face face) {
		if (_unattributedString.length() == 0)
			return;
		if (face.getBackgroundColor() != null)
			setBackgroundColor(face.getBackgroundColor());
		if (face.getForegroundColor() != null)
			setForegroundColor(face.getForegroundColor());
		if (face.getBold())
			setBold();
	}

	public void setBold() {
		if (_unattributedString.length() == 0)
			return;
		_string.addAttribute(_boldAttribute, Boolean.TRUE);
	}

	private interface ANSIAttribute {
		@Override
		String toString();
	}

	private void addAttributedString(StringBuilder str, String string,
			Map<AttributedCharacterIterator.Attribute, Object> attributes) {
		if (!attributes.isEmpty()) {
			int i = 0;
			str.append("\u001b[");
			for (Entry<AttributedCharacterIterator.Attribute, Object> entry : attributes.entrySet()) {
				if (i++ != 0)
					str.append(";");
				Attribute attr = (Attribute) entry.getKey();
				Object value = entry.getValue();
				str.append(attr.getANSIAttribute(value));
			}
			str.append("m");
		}
		str.append(string);
		if (!attributes.isEmpty()) {
			str.append("\u001b[0m");
		}
	}

	@Override
	public String toString() {
		return _unattributedString;
	}

	public String toANSIString() {
		StringBuilder str = new StringBuilder();

		AttributedCharacterIterator iterator = _string.getIterator();

		while (iterator.getIndex() < _unattributedString.length()) {
			int startIndex = iterator.getIndex();
			int endIndex = iterator.getRunLimit();
			Map<AttributedCharacterIterator.Attribute, Object> attributes = iterator.getAttributes();
			String substring = _unattributedString.substring(startIndex, endIndex);
			addAttributedString(str, substring, attributes);

			iterator.setIndex(endIndex);
		}

		return str.toString();
	}

	public CharSequence getCharSequence() {
		return _unattributedString;
	}

	public void clearAttributes() {
		_string = new java.text.AttributedString(_unattributedString);
	}

	public AttributedString substring(int start, int end) {
		String str = _unattributedString.substring(start, end);
		AttributedString attrString = new AttributedString(str);
		AttributedCharacterIterator iterator = _string.getIterator(null, start, end);

		while (iterator.getIndex() < end) {
			int startIndex = iterator.getIndex();
			int endIndex = iterator.getRunLimit();
			Map<java.text.AttributedCharacterIterator.Attribute, Object> attributes = iterator.getAttributes();

			attrString._string.addAttributes(attributes, startIndex - start, endIndex - start);
			iterator.setIndex(endIndex);
		}

		return attrString;
	}

	public AttributedString copy() {
		return substring(0, length());
	}

	@Override
	public int length() {
		return _unattributedString.length();
	}

	@Override
	public char charAt(int index) {
		return _unattributedString.charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return substring(start, end);
	}

	final static Logger LOG = LoggerFactory.getLogger(BufferTextState.class);

	public AttributedString stringByInsertingString(String string, int index) {
		LOG.debug("string: " + _unattributedString + ", insert: " + string + " at " + index);

		StringBuilder builder = new StringBuilder(_unattributedString);
		builder.insert(index, string);
		String str = builder.toString();
		AttributedString attrString = new AttributedString(str);
		int start = 0;
		int end = length();
		AttributedCharacterIterator iterator = _string.getIterator(null, start, end);

		while (iterator.getIndex() < end) {
			int startIndex = iterator.getIndex();
			int endIndex = iterator.getRunLimit();
			int nextIndex = endIndex;

			if (startIndex > index) {
				startIndex += string.length();
			}

			if (endIndex > index) {
				endIndex += string.length();
			}

			Map<java.text.AttributedCharacterIterator.Attribute, Object> attributes = iterator.getAttributes();

			attrString._string.addAttributes(attributes, startIndex, endIndex);
			iterator.setIndex(nextIndex);
		}

		LOG.debug("result: " + attrString.toString());

		return attrString;
	}

	public AttributedString stringByDeletingString(Range range) {
		LOG.debug("string: " + _unattributedString + ", delete: " + range.getStart() + " - " + range.getEnd());

		if (range.getStart() < 0 || range.getEnd() > length())
			throw new RuntimeException("Wrong range to delete");

		StringBuilder builder = new StringBuilder(_unattributedString);
		builder.delete(range.getStart(), range.getEnd());
		String str = builder.toString();
		AttributedString attrString = new AttributedString(str);
		int start = 0;
		int end = length();
		AttributedCharacterIterator iterator = _string.getIterator(null, start, end);

		while (iterator.getIndex() < end) {
			int startIndex = iterator.getIndex();
			int endIndex = iterator.getRunLimit();
			int length = endIndex - startIndex;
			Range intersection = new Range(startIndex, length).intersection(range);
			int nextIndex = endIndex;

			if (!(startIndex >= range.getStart() && endIndex < range.getEnd())) {
				if (intersection != null) {
					startIndex -= range.getLength();
					length -= intersection.getLength();
				}

				if (length > 0) {
					if (startIndex >= range.getEnd()) {
						startIndex -= range.getLength();
					}

					endIndex = startIndex + length;

					if (startIndex < 0 || endIndex > length())
						throw new RuntimeException("Wrong range to delete");

					LOG.debug("Deleting: " + startIndex + " - " + endIndex);

					Map<java.text.AttributedCharacterIterator.Attribute, Object> attributes = iterator.getAttributes();
					attrString._string.addAttributes(attributes, startIndex, endIndex);
				}
			}

			iterator.setIndex(nextIndex);
		}

		LOG.debug("result: " + attrString.toString());

		return attrString;
	}
}
