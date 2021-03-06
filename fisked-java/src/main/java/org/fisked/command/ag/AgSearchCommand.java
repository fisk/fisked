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
package org.fisked.command.ag;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.command.api.ICommandHandler;
import org.fisked.project.Project;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.ui.buffer.BufferWindow;
import org.fisked.ui.listview.ListView;
import org.fisked.ui.listview.ListView.ListViewDataSource;
import org.fisked.ui.listview.ListView.ListViewDelegate;
import org.fisked.ui.screen.Screen;
import org.fisked.ui.window.IWindowManager;
import org.fisked.ui.window.PopUpWindow;
import org.fisked.ui.window.Window;
import org.fisked.util.models.AttributedString;
import org.fisked.util.models.Point;
import org.fisked.util.models.Rectangle;
import org.fisked.util.shell.ShellCommandExecution;
import org.fisked.util.shell.ShellCommandExecution.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgSearchCommand implements ICommandHandler {
	private final static Logger LOG = LoggerFactory.getLogger(AgSearchCommand.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(AgSearchCommand.class);

	private class AgSearchResultLine {
		private final Path _path;
		private final String _text;
		private final int _line;
		private final int _index;

		public AgSearchResultLine(Path path, String text, int line, int index) {
			_path = path;
			_text = text;
			_line = line;
			_index = index;
		}

		@Override
		public String toString() {
			return "{ path: " + _path + ", line: " + _line + ", _index: " + _index + ", text: " + _text + "}";
		}

		public Path getPath() {
			return _path;
		}

		public String getText() {
			return _text;
		}

		public int getLine() {
			return _line;
		}

		public int getIndex() {
			return _index;
		}
	}

	private List<AgSearchResultLine> parse(String result) {
		List<AgSearchResultLine> listResult = new ArrayList<>();
		try (Scanner scanner = new Scanner(result)) {
			for (;;) {
				if (!scanner.hasNextLine()) {
					break;
				}
				String line = scanner.nextLine();
				LOG.debug("Line: " + line);
				try (Scanner lineScanner = new Scanner(line)) {
					lineScanner.useDelimiter(":");
					String pathString = lineScanner.next();
					int lineNumber = lineScanner.nextInt();
					int indexNumber = lineScanner.nextInt();
					String text = lineScanner.next();

					AgSearchResultLine entry = new AgSearchResultLine(Paths.get(pathString), text, lineNumber,
							indexNumber);
					LOG.debug("Entry: " + entry);
					listResult.add(entry);
				}
			}
			return listResult;
		}
	}

	private static final String POPUP_WINDOW_KEY = "org.fisked.command.ag.PopupWindow";

	private static class AgSearchResultWindow extends PopUpWindow {
		protected List<AgSearchResultLine> _list;
		protected ListView<AgSearchResultLine> _listView;
		protected ListViewDelegate<AgSearchResultLine> _delegate;
		protected ListViewDataSource<AgSearchResultLine> _dataSource;

		@Override
		public void drawPoint(IRenderingContext context) {
			_listView.drawPoint(context);
		}

		public AgSearchResultWindow() {
			super("Ag Search Results");
			setId(POPUP_WINDOW_KEY);
		}

		public void setup(List<AgSearchResultLine> list, Screen screen, BufferWindow invoker) {
			_list = list;
			if (_listView != null) {
				_listView.removeFromParent();
			}

			_listView = new ListView<>(new Rectangle(new Point(0, 0), _windowRect.getSize()));
			_delegate = new ListViewDelegate<AgSearchResultLine>() {
				@Override
				public int getElementLines() {
					return 1;
				}

				@Override
				public void selectedItem(int index) {
					LOG.debug("Selected element " + index);
					AgSearchResultLine result = _list.get(index);
					Path path = result._path;
					try (IBehaviorConnection<IWindowManager> wmBC = BEHAVIORS
							.getBehaviorConnection(IWindowManager.class).get()) {
						Screen screen = wmBC.getBehavior().getPrimaryScreen();
						screen.detatchWindow(AgSearchResultWindow.this);
						BufferWindow window = _invoker;
						window.openFile(path.toFile());
					} catch (Exception e) {
						LOG.error("Could not setup popup window: ", e);
					}
				}

				@Override
				public AttributedString toString(AgSearchResultLine element, boolean selected) {
					String text = element.getText();
					String path = element.getPath().toString();
					AttributedString attrString = new AttributedString(
							path + ":" + element._line + ":" + element._index + ": " + text);
					return attrString;
				}
			};
			_dataSource = new ListViewDataSource<AgSearchResultLine>() {
				@Override
				public int length() {
					return _list.size();
				}

				@Override
				public AgSearchResultLine get(int index) {
					return _list.get(index);
				}
			};
			_listView.setDelegate(_delegate);
			_listView.setDataSource(_dataSource);
			setupPopupView(invoker, _listView, _listView.createResponder());
		}
	}

	private void setupAgResultView(BufferWindow invoker, List<AgSearchResultLine> list) {
		try (IBehaviorConnection<IWindowManager> wmBC = BEHAVIORS.getBehaviorConnection(IWindowManager.class).get()) {
			Screen screen = wmBC.getBehavior().getPrimaryScreen();
			Window cachedWindow = wmBC.getBehavior().getWindow(POPUP_WINDOW_KEY);
			if (cachedWindow != null) {
				((AgSearchResultWindow) cachedWindow).setup(list, screen, invoker);
			} else {
				AgSearchResultWindow window = new AgSearchResultWindow();
				window = (AgSearchResultWindow) wmBC.getBehavior().registerWindow(window);
				window.setup(list, screen, invoker);
			}
		} catch (Exception e) {
			LOG.error("Could not setup popup window: ", e);
		}
	}

	@Override
	public void run(BufferWindow window, String[] argv) {
		StringBuilder sb = new StringBuilder();
		LOG.debug(argv.toString());
		LOG.debug("Length: " + argv.length);
		for (int i = 1; i < argv.length; i++) {
			String arg = argv[i];
			if (i != 1) {
				sb.append(" ");
			}
			sb.append(arg);
		}
		LOG.debug("command: ag " + sb.toString());
		Project project = Project.getProject(Paths.get(window.getBuffer().getFile().getAbsolutePath()));
		File rootFile = project.getRootDirectoryFile();
		String[] args = { "ag", "--vimgrep", sb.toString(), rootFile.getAbsolutePath() };
		ShellCommandExecution execution = new ShellCommandExecution(args);
		execution.redirectInput();
		ExecutionResult result = execution.executeSync();
		if (result.getStatus() != 0) {
			window.getCommandController().setCommandFeedback("Could execute the ag command.");
		} else {
			List<AgSearchResultLine> resultList = parse(result.getResult());
			setupAgResultView(window, resultList);
			window.getCommandController().setCommandFeedback("");
		}
		window.refresh();
	}

}
