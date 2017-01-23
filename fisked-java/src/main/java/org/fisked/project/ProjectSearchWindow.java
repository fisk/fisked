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

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.command.CommandView;
import org.fisked.renderingengine.service.IConsoleService.IRenderingContext;
import org.fisked.responder.Event;
import org.fisked.responder.InputResponderChain;
import org.fisked.responder.RecognitionState;
import org.fisked.ui.drawing.Screen;
import org.fisked.ui.drawing.View;
import org.fisked.ui.listview.ListView;
import org.fisked.ui.listview.ListView.ListViewDataSource;
import org.fisked.ui.listview.ListView.ListViewDelegate;
import org.fisked.ui.window.IWindowManager;
import org.fisked.ui.window.Window;
import org.fisked.util.models.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectSearchWindow extends Window {
	private final ProjectSearchController _controller;
	private final static Logger LOG = LoggerFactory.getLogger(ProjectSearchWindow.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(ProjectSearchWindow.class);

	private final CommandView _commandView;
	private final ListView<String> _listView;
	private final ProjectSearchCommandController _commandController;

	public ProjectSearchController getController() {
		return _controller;
	}

	private CommandView createCommandView() {
		Rectangle rect = new Rectangle(0, _rootView.getBounds().getSize().getHeight() - 1,
				_rootView.getBounds().getSize().getWidth(), 1);
		CommandView view = new CommandView(rect, _commandController);
		_commandController.setCommandView(_commandView);
		_rootView.addSubview(view);
		return view;
	}

	private ListView<String> createListView() {
		Rectangle listViewRect = new Rectangle(0, 0, _rootView.getBounds().getSize().getWidth(),
				_rootView.getBounds().getSize().getHeight() - 2);
		ListView<String> listView = new ListView<>(listViewRect);
		ListViewDataSource<String> dataSource = _controller.createListViewDataSource();
		listView.setDataSource(dataSource);
		ListViewDelegate<String> delegate = _controller.createListViewDelegate();
		listView.setDelegate(delegate);
		_rootView.addSubview(listView);
		return listView;
	}

	public ProjectSearchWindow(Project project, Screen screen) {
		super(screen.getBounds(), "Project Search");
		_controller = new ProjectSearchController(project, this);
		_commandController = new ProjectSearchCommandController(_controller);

		Rectangle rootViewRect = _windowRect;

		_rootView = new View(rootViewRect);

		InputResponderChain chain = new InputResponderChain();

		chain.addResponder((Event event) -> {
			if (event.isControlChar('q')) {
				return RecognitionState.Recognized;
			} else {
				return RecognitionState.NotRecognized;
			}
		}, () -> {
			try (IBehaviorConnection<IWindowManager> windowManagerBC = BEHAVIORS
					.getBehaviorConnection(IWindowManager.class).get()) {
				LOG.debug("Quit command being invoked.");
				windowManagerBC.getBehavior().popPrimaryScreen();
			} catch (Exception e) {
				LOG.error("Could not pop window: ", e);
			}
		});

		_listView = createListView();
		chain.addResponder(_listView.createResponder());

		_commandView = createCommandView();
		chain.addResponder(_commandController);

		_primaryResponder = chain;

		_controller.init();
	}

	@Override
	public void drawPoint(IRenderingContext context) {
		_listView.drawPoint(context);
	}

	public ListView<String> getListView() {
		return _listView;
	}

	public ProjectSearchCommandController getCommandController() {
		return _commandController;
	}
}
