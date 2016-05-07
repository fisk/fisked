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
