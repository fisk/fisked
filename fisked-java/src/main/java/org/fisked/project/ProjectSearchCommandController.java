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
import org.fisked.command.CommandController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectSearchCommandController extends CommandController {
	private final static Logger LOG = LoggerFactory.getLogger(ProjectSearchCommandController.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(
			ProjectSearchCommandController.class);

	private String _searchString = "";
	private final ProjectSearchController _controller;

	public ProjectSearchCommandController(ProjectSearchController controller) {
		super();
		_controller = controller;
	}

	@Override
	protected void updateCommand(String command) {
		LOG.debug("Controller update command: " + command);
		_searchString = command;
		_controller.updateListView();
	}

	@Override
	protected void handleCommand(String command) {
		LOG.debug("Controller finish command: " + command);
		_searchString = command;
		_controller.updateListView();
	}

	public String getSearchString() {
		return _searchString;
	}

	public void clearSearchString() {
		_searchString = "";
		clearCommand();
	}

}
