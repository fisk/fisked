package org.fisked.renderingengine.service.models;

import java.util.ArrayList;
import java.util.List;

public class AttributedString {
	private static abstract class Attribute {}
	private static class ColorAttribute extends Attribute {
		private Color _color;
		public ColorAttribute(Color color) {
			_color = color;
		}
		public String toString(int base) {
			return "" + (base + _color.getRawColor());
		}
	}
	private static class ForegroundColorAttribute extends ColorAttribute {
		public ForegroundColorAttribute(Color color) {
			super(color);
		}
		public String toString() {
			return super.toString(30);
		}
	}
	private static class BackgroundColorAttribute extends ColorAttribute {
		public BackgroundColorAttribute(Color color) {
			super(color);
		}
		public String toString() {
			return super.toString(40);
		}
	}
	private static class BoldAttribute extends Attribute {
		public String toString() {
			return "1";
		}
	}
	private CharSequence _string;
	private List<Attribute> _attributes = new ArrayList<>();
	
	public AttributedString(CharSequence string) {
		_string = string;
	}
	
	public void setForegroundColor(Color color) {
		_attributes.add(new ForegroundColorAttribute(color));
	}
	
	public void setBackgroundColor(Color color) {
		_attributes.add(new BackgroundColorAttribute(color));
	}
	
	public void setBold() {
		_attributes.add(new BoldAttribute());
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		if (_attributes.size() != 0) {
			str.append("\u001B[");
			int i = 0;
			for (Attribute attr : _attributes) {
				if (i++ != 0) {
					str.append(";");
				}
				str.append(attr.toString());
			}
			str.append("m");
		}
		str.append(_string);
		if (_attributes.size() != 0) {
			str.append("\u001b[0m");
		}
		return str.toString();
	}
}
