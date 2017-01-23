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
package org.fisked.buffer.controller;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.buffer.Buffer;
import org.fisked.buffer.Buffer.UndoScope;
import org.fisked.command.CommandController;
import org.fisked.command.api.ICommandManager;
import org.fisked.ui.buffer.BufferWindow;
import org.fisked.util.datastructure.IntervalTree;
import org.fisked.util.models.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferCommandController extends CommandController {
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(
			BufferCommandController.class);
	private final static Logger LOG = LoggerFactory.getLogger(BufferCommandController.class);

	private final BufferWindow _window;

	public BufferCommandController(BufferWindow bufferWindow) {
		super();
		_window = bufferWindow;
	}

	Pattern searchReplacePattern = Pattern.compile("s/(.*)/(.*)/g?");

	@Override
	protected void handleCommand(String command) {
		Matcher searchPattern = searchReplacePattern.matcher(command);
		if (searchPattern.matches()) {
			handleSearchReplace(searchPattern);
			return;
		}
		String[] argv = command.split("\\s+");
		if (argv.length >= 1) {
			try (IBehaviorConnection<ICommandManager> commandBC = BEHAVIORS.getBehaviorConnection(ICommandManager.class)
					.get()) {
				commandBC.getBehavior().handle(_window, argv[0], argv);
			} catch (Exception e) {
				LOG.error("Command failed: ", e);
			}
		}
	}

	private void handleSearchReplace(Matcher searchPattern) {
		String searchString = searchPattern.group(1);
		String replaceString = searchPattern.group(2);

		List<FatTextSelection> ranges = _window.getBufferController().getFatTextSelections();

		Buffer buffer = _window.getBuffer();
		try (UndoScope us = buffer.createUndoScope()) {
			IntervalTree<String> intervalTree = new IntervalTree<>();

			String bufferString = _window.getBuffer().toString();
			ranges.forEach((FatTextSelection selection) -> {
				for (Range range : selection.getRanges()) {
					intervalTree.add(range, bufferString.substring(range.getStart(), range.getEnd()));
				}
				selection.getCursor().clearOtherSorted();
			});

			int first = intervalTree.getStart();
			Pattern pattern = Pattern.compile(searchString);

			intervalTree.forEachReverse((Range range, String text) -> {
				String subString = bufferString.substring(range.getStart(), range.getEnd());
				int adjust = range.getStart();
				Matcher matcher = pattern.matcher(subString);

				while (matcher.find()) {
					int start = matcher.start(0) + adjust;
					int end = matcher.end(0) + adjust;
					buffer.removeCharsInRangeLogged(new Range(start, end - start));
					buffer.insertStringLogged(start, replaceString);
					adjust += replaceString.length() - (end - start);
				}
			});
			_window.getBufferController().collapseCursors(first);
		}

	}

}
