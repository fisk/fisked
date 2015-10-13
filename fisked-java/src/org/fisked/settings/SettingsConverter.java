package org.fisked.settings;

import org.fisked.settings.Settings.TerminalType;

public class SettingsConverter {

	// Integer
	public static Integer convert(String value, Integer defaultValue) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static String toString(Integer obj) {
		return Integer.toString(obj);
	}

	// boolean
	public static boolean convert(String value, boolean defaultValue) {
		try {
			return Boolean.parseBoolean(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static String toString(boolean obj) {
		return Boolean.toString(obj);
	}

	// terminal type
	public static TerminalType convert(String value, TerminalType defaultValue) {
		if (value == null) { return defaultValue; }

		if (value.compareToIgnoreCase("unknown") != 0) {
			return TerminalType.Unknown;
		} else if (value.compareToIgnoreCase("iterm") != 0) {
			return TerminalType.iTerm;
		}
		return defaultValue;
	}

	public static String toString(TerminalType obj) {
		return obj.toString().toLowerCase();
	}

}
