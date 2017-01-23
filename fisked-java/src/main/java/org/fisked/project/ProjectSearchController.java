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
package org.fisked.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.ui.buffer.BufferWindow;
import org.fisked.ui.drawing.Screen;
import org.fisked.ui.listview.ListView;
import org.fisked.ui.listview.ListView.ListViewDataSource;
import org.fisked.ui.listview.ListView.ListViewDelegate;
import org.fisked.ui.window.IWindowManager;
import org.fisked.ui.window.Window;
import org.fisked.util.FileUtil;
import org.fisked.util.models.AttributedString;
import org.fisked.util.models.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectSearchController {
	private final ProjectSearchWindow _window;
	private final Project _project;
	private final static Logger LOG = LoggerFactory.getLogger(ProjectSearchController.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(
			ProjectSearchController.class);

	private List<String> _paths = new ArrayList<>();

	public ProjectSearchController(Project project, ProjectSearchWindow window) {
		_window = window;
		_project = project;

	}

	public ListViewDataSource<String> createListViewDataSource() {
		ListViewDataSource<String> ds = new ListViewDataSource<String>() {

			@Override
			public int length() {
				return _paths.size();
			}

			@Override
			public String get(int index) {
				return _paths.get(index);
			}

		};
		return ds;
	}

	private ListView<String> getListView() {
		return _window.getListView();
	}

	private ProjectSearchCommandController getCommandController() {
		return _window.getCommandController();
	}

	public void init() {
		updateListView();
	}

	public ListViewDelegate<String> createListViewDelegate() {
		ListViewDelegate<String> delegate = new ListViewDelegate<String>() {

			@Override
			public int getElementLines() {
				return 1;
			}

			@Override
			public void selectedItem(int index) {
				LOG.debug("Selected item: " + index);

				try (IBehaviorConnection<IWindowManager> wmBC = BEHAVIORS.getBehaviorConnection(IWindowManager.class)
						.get()) {
					IWindowManager wm = wmBC.getBehavior();
					Screen screen = wm.popPrimaryScreen();
					Window primary = screen.getPrimaryWindow();
					screen.detatchWindow(primary);

					String path = _paths.get(index);

					BufferWindow window = new BufferWindow(primary.getRootView().getBounds(), path);

					File file = FileUtil.getFile(path);
					try {
						window.openFile(file);
					} catch (IOException e) {
						LOG.error("Could not open file", e);
					}

					getCommandController().clearSearchString();
					getListView().setNeedsDrawing();

					screen.attachWindow(window);
				} catch (Exception e) {
					LOG.error("Can't get window manager service: ", e);
				}
			}

			@Override
			public AttributedString toString(String element, boolean selected) {
				AttributedString str = new AttributedString(element);
				if (selected) {
					str.setBackgroundColor(Color.BLUE);
					str.setForegroundColor(Color.BLACK);
				}
				return str;
			}
		};
		return delegate;
	}

	public void updateListView() {
		_paths = _project.searchFilePath(getCommandController().getSearchString());
		getListView().setNeedsDrawing();
	}
}
