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

import org.apache.felix.ipojo.annotations.Requires;
import org.fisked.IApplication;
import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.command.api.ICommandManager;
import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.RecognitionState;
import org.fisked.ui.buffer.BufferWindow;
import org.fisked.ui.screen.Screen;
import org.fisked.ui.window.IWindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectResponder implements IInputResponder {
	private final static Logger LOG = LoggerFactory.getLogger(ProjectCommands.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(ProjectCommands.class);
	private final String SEARCH_WINDOW_ID = "org.fisked.project.ProjectSearchWindow";

	private final BufferWindow _window;

	public ProjectResponder(BufferWindow window) {
		_window = window;
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		return EventRecognition.matchesExact(nextEvent, ",p");
	}

	@Requires
	private ICommandManager _commandManager;

	@Requires
	private IApplication _application;

	@Requires
	private IWindowManager _windowManager;

	@Override
	public void onRecognize() {
		File file = _window.getBuffer().getFile();
		Project project = Project.getProject(file.getAbsoluteFile().toPath());

		if (project == null) {
			return;
		}

		Screen screen = new Screen("Project Search Screen");

		try (IBehaviorConnection<IWindowManager> wmBC = BEHAVIORS.getBehaviorConnection(IWindowManager.class).get()) {
			IWindowManager wm = wmBC.getBehavior();
			ProjectSearchWindow projectWindow = (ProjectSearchWindow) wm.getWindow(SEARCH_WINDOW_ID);
			if (projectWindow == null) {

				projectWindow = new ProjectSearchWindow(project, screen);
				projectWindow.setId(SEARCH_WINDOW_ID);
				projectWindow.register();
			}

			LOG.debug("Activating project search.");
			screen.attachWindow(projectWindow);
			wmBC.getBehavior().pushPrimaryScreen(screen);
			projectWindow.getController().init();
		} catch (Exception e) {
			LOG.error("Can't get window manager service: ", e);
		}
	}

}
