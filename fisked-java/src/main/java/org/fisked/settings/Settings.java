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
package org.fisked.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.fisked.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Settings {
	public static Settings _instance;
	private final static Logger LOG = LoggerFactory.getLogger(Settings.class);

	private static final String _settingsFileName = "conf/fiskedrc";

	private int _numberOfDigitsForLineNumbers;
	private int _tabWhitespaces;
	private boolean _usingPowerlinePatchedFont;
	private TerminalType _terminalType;

	public enum TerminalType {
		Unknown, iTerm
	}

	private Settings() {
		File home = FileUtil.getFiskedHome();
		File f = new File(home, _settingsFileName);
		LOG.debug("Settings file: " + f.getAbsolutePath());
		if (!f.exists()) {
			save(f);
		}
		load(f);
	}

	private static class SettingsHolder {
		public static final Settings instance = new Settings();
	}

	public static Settings getInstance() {
		return SettingsHolder.instance;
	}

	public int getNumberOfDigitsForLineNumbers() {
		return _numberOfDigitsForLineNumbers;
	}

	public int getTabWhitespaces() {
		return _tabWhitespaces;
	}

	public boolean isPowerlinePatchedFontUsed() {
		return _usingPowerlinePatchedFont;
	}

	public TerminalType getTerminalType() {
		return _terminalType;
	}

	// Can later be public if we want to store any changes the user does during
	// runtime.
	private void save(File f) {
		StringBuilder sb = new StringBuilder();
		List<String> lines = new ArrayList<>();

		lines.add("# The number of whitespaces used for a tab.");
		lines.add("view.tabWhitespaces" + "=" + SettingsConverter.toString(_tabWhitespaces));
		lines.add("");

		lines.add("# The number of digits used for displaying line numbers. Set to 0 to hide the line numbers.");
		lines.add("view.linenumbers.numberofdigits" + "=" + SettingsConverter.toString(_numberOfDigitsForLineNumbers));
		lines.add("");

		lines.add("# Set to true if the Powerline patched font are used. https://github.com/powerline/fonts");
		lines.add("view.usingPowerlinePatchedFont" + "=" + SettingsConverter.toString(_usingPowerlinePatchedFont));
		lines.add("");

		// Must be a better way to handle the enums...
		TerminalType[] terminalTypeValues = TerminalType.values();
		String[] terminalTypes = new String[terminalTypeValues.length];
		for (int i = 0; i < terminalTypes.length; i++) {
			terminalTypes[i] = terminalTypeValues[i].name();
		}
		lines.add(
				"# Some functions are performing better with specific terminals. If you are using a supported terminal you might want to specify which one.");
		lines.add("# Supported terminals: " + String.join(", ", terminalTypes));
		lines.add("terminal.type" + "=" + SettingsConverter.toString(_terminalType));
		lines.add("");

		for (String line : lines) {
			sb.append(line);
			sb.append("\n");
		}

		try {
			FileUtils.write(f, lines.toString());
		} catch (IOException e) {
			LOG.error("Could not save config file.");
		}
	}

	private void load(File f) {
		Properties prop = new Properties();
		try {
			FileInputStream fis = new FileInputStream(f);
			prop.load(fis);
			fis.close();
		} catch (FileNotFoundException e) {
			LOG.error("Config file did not exists. No problem!");
		} catch (IOException e) {
			LOG.error("Could not read config file.");
		}

		_numberOfDigitsForLineNumbers = SettingsConverter.convert(prop.getProperty("view.linenumbers.numberofdigits"),
				4);
		_tabWhitespaces = SettingsConverter.convert(prop.getProperty("view.tabWhitespaces"), 4);
		_usingPowerlinePatchedFont = SettingsConverter.convert(prop.getProperty("view.usingPowerlinePatchedFont"),
				false);
		_terminalType = SettingsConverter.convert(prop.getProperty("terminal.type"), TerminalType.Unknown);
		if (_terminalType == TerminalType.Unknown) {
			// If the user doesn't know, try to detect automatically
			if (System.getenv().containsKey("ITERM_PROFILE")) {
				_terminalType = TerminalType.iTerm;
			}
		}

		LOG.debug("Digits for line numbers: " + _numberOfDigitsForLineNumbers);
		LOG.debug("Powerline patched font: " + _usingPowerlinePatchedFont);
		LOG.debug("Terminal type: " + terminalName(_terminalType));
	}

	private String terminalName(TerminalType terminalType) {
		switch (terminalType) {
		case Unknown:
			return "Unknown";
		case iTerm:
			return "iTerm";
		default:
			return "Unknown";
		}
	}
}
