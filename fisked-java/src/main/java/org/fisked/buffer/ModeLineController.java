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
package org.fisked.buffer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.fisked.scm.ISCMRepository;
import org.fisked.scm.SCMRepositoryResolver;
import org.fisked.settings.Settings;
import org.fisked.theme.ITheme;
import org.fisked.theme.ThemeManager;
import org.fisked.util.concurrency.Dispatcher;
import org.fisked.util.models.AttributedString;
import org.fisked.util.models.Color;
import org.fisked.util.models.Face;
import org.fisked.util.models.Point;

public class ModeLineController {

	private final BufferWindow _window;
	Timer _timer = new Timer();

	public ModeLineController(BufferWindow window) {
		_window = window;
		_timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Dispatcher.getInstance().runMain(() -> {
					_window.setNeedsFullRedraw();
				});
			}
		}, 0, 5000);

	}

	private AttributedString drawSlimRightArrow(Face face) {
		String str;
		if (Settings.getInstance().isPowerlinePatchedFontUsed()) {
			str = "\ue0b1";
		} else {
			str = "|";
		}
		return new AttributedString(str, face);
	}

	private AttributedString drawFatRightArrow(Color leftColor, Color rightColor) {
		if (Settings.getInstance().isPowerlinePatchedFontUsed()) {
			return new AttributedString("\ue0b0", new Face(rightColor, leftColor, false));
		} else {
			return new AttributedString(" ", new Face(leftColor, rightColor, false));
		}
	}

	private AttributedString drawSlimLeftArrow(Face face) {
		String str;
		if (Settings.getInstance().isPowerlinePatchedFontUsed()) {
			str = "\ue0b3";
		} else {
			str = "|";
		}
		return new AttributedString(str, face);
	}

	private AttributedString drawFatLeftArrow(Color leftColor, Color rightColor) {
		if (Settings.getInstance().isPowerlinePatchedFontUsed()) {
			return new AttributedString("\ue0b2", new Face(leftColor, rightColor, false));
		} else {
			return new AttributedString(" ", new Face(rightColor, leftColor, false));
		}
	}

	private AttributedString drawLineNumberChar(Face face) {
		String str;
		if (Settings.getInstance().isPowerlinePatchedFontUsed()) {
			str = "\ue0a1";
		} else {
			str = "LN";
		}
		return new AttributedString(str, face);
	}

	private AttributedString drawBranch(Face face) {
		String str;
		if (Settings.getInstance().isPowerlinePatchedFontUsed()) {
			str = "\ue0a0";
		} else {
			str = "√";
		}
		return new AttributedString(str, face);
	}

	private AttributedString drawText(String text, Face face) {
		return new AttributedString(text, face);
	}

	private RepoInfo _repoInfo = null;

	private class RepoInfo {
		String _scm;
		String _branch;
		boolean _valid;
	}

	private RepoInfo getRepoInfo() {
		if (_repoInfo == null) {
			_repoInfo = new RepoInfo();
			File file = _window.getBuffer().getFile();
			ISCMRepository repo = SCMRepositoryResolver.getInstance().getRepositoryForFile(file);
			if (repo == null) {
				_repoInfo._valid = false;
			} else {
				_repoInfo._valid = true;
				_repoInfo._branch = repo.getBranchName();
				_repoInfo._scm = repo.getSCMName();
			}
		}
		return _repoInfo;
	}

	private List<AttributedString> getLeftModelineText() {
		ITheme theme = ThemeManager.getThemeManager().getCurrentTheme();
		Face modeFace = _window.getCurrentMode().getModelineFace();
		Color modelineBackgroundColor = theme.getModelineBackgroundColorLight();
		Color modelineForegroundColor = theme.getModelineForegroundColor();
		Color modeBackgroundColor = modeFace.getBackgroundColor();
		Face modelineFace = new Face(modelineBackgroundColor, modelineForegroundColor);

		List<AttributedString> result = new ArrayList<>();
		result.add(drawText(" " + _window.getCurrentMode().getModeName().toUpperCase() + "  ", modeFace));
		result.add(drawFatRightArrow(modeBackgroundColor, modelineBackgroundColor));
		RepoInfo ri = getRepoInfo();
		if (ri._valid) {
			result.add(drawText(" " + ri._scm + " ", modelineFace));
			result.add(drawSlimRightArrow(modelineFace));
			result.add(drawText(" ", modelineFace));
			result.add(drawBranch(modelineFace));
			result.add(drawText(" " + ri._branch + " ", modelineFace));
			result.add(drawSlimRightArrow(modelineFace));
		}
		result.add(drawText(" " + _window.getBuffer().getFileName() + " ", modelineFace));
		result.add(drawSlimRightArrow(modelineFace));

		return result;
	}

	SimpleDateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat _timeFormat = new SimpleDateFormat("HH:mm");

	private String getDateString() {
		return _dateFormat.format(new Date());
	}

	private String getTimeString() {
		return _timeFormat.format(new Date());
	}

	private List<AttributedString> getRightModelineText() {
		ITheme theme = ThemeManager.getThemeManager().getCurrentTheme();
		Face modeFace = _window.getCurrentMode().getModelineFace();
		Color modelineBackgroundColor = theme.getModelineBackgroundColorLight();
		Color modeBackgroundColor = modeFace.getBackgroundColor();

		Point point = _window.getBufferController().getPrimaryAbsolutePoint();

		List<AttributedString> result = new ArrayList<>();
		result.add(drawFatLeftArrow(modelineBackgroundColor, modeBackgroundColor));
		result.add(drawText(" ", modeFace));
		result.add(drawLineNumberChar(modeFace));
		result.add(drawText(" " + point.getY() + ":" + point.getX() + " ", modeFace));
		result.add(drawSlimLeftArrow(modeFace));
		result.add(drawText(" " + getDateString() + " ", modeFace));
		result.add(drawSlimLeftArrow(modeFace));
		result.add(drawText(" " + getTimeString() + "  ", modeFace));

		return result;
	}

	private AttributedString getSpace(int spaces) {
		ITheme theme = ThemeManager.getThemeManager().getCurrentTheme();
		Color modelineBackgroundColor = theme.getModelineBackgroundColorLight();
		Color modelineForegroundColor = theme.getModelineForegroundColor();
		Face modelineFace = new Face(modelineBackgroundColor, modelineForegroundColor);

		StringBuilder str = new StringBuilder();
		for (int i = 0; i < spaces; i++) {
			str.append(" ");
		}
		return new AttributedString(str.toString(), modelineFace);
	}

	// TODO: Allow themes to extend this stuff

	public List<AttributedString> getModeLineText() {
		List<AttributedString> left = getLeftModelineText();
		List<AttributedString> right = getRightModelineText();

		int spaces = _window.getRootView().getClippingRect().getSize().getWidth();

		for (AttributedString str : left) {
			spaces -= str.getCharSequence().length();
		}

		for (AttributedString str : right) {
			spaces -= str.getCharSequence().length();
		}

		List<AttributedString> result = new ArrayList<>();

		result.addAll(left);
		result.add(getSpace(spaces));
		result.addAll(right);

		return result;
	}

}
