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
package org.fisked.command.provided;

import java.util.concurrent.Future;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Validate;
import org.fisked.IApplication;
import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.command.ag.AgSearchCommand;
import org.fisked.command.api.CommandHandlerReference;
import org.fisked.command.api.ICommandManager;
import org.fisked.shell.ShellCommandHandler;
import org.fisked.ui.buffer.BufferWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, publicFactory = false)
@Instantiate
public class ApplicationCommands {
	private final static Logger LOG = LoggerFactory.getLogger(ApplicationCommands.class);
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(ApplicationCommands.class);

	private CommandHandlerReference _quitCommand;
	private CommandHandlerReference _openCommand;
	private CommandHandlerReference _saveCommand;
	private CommandHandlerReference _shellCommand;
	private CommandHandlerReference _agSearchCommand;

	@Validate
	public void start() {
		try (IBehaviorConnection<ICommandManager> commandBC = BEHAVIORS.getBehaviorConnection(ICommandManager.class)
				.get()) {
			LOG.debug("Registering application commands.");
			ICommandManager cm = commandBC.getBehavior();

			_quitCommand = cm.registerHandler("q", (BufferWindow window, String[] argv) -> {
				try (IBehaviorConnection<IApplication> applicationBC = BEHAVIORS
						.getBehaviorConnection(IApplication.class).get()) {
					applicationBC.getBehavior().exit(0);
				} catch (Exception e) {
					LOG.error("Couldn't shut down gracefully.", e);
					System.exit(-1);
				}
			});
			_openCommand = cm.registerHandler("e", new OpenFileCommand());
			_saveCommand = cm.registerHandler("w", (BufferWindow window, String[] argv) -> {
				try {
					window.getBuffer().save();
					window.getCommandController().setCommandFeedback("Saved file.");
				} catch (Exception e) {
					window.getCommandController().setCommandFeedback("Couldn't save.");
				}
			});
			_shellCommand = cm.registerHandler("r", new ShellCommandHandler());
			_agSearchCommand = cm.registerHandler("ag", new AgSearchCommand());
		} catch (Exception e) {
			LOG.error("Couldn't start application commands: ", e);
		}
	}

	@Invalidate
	public void stop() {
		Future<IBehaviorConnection<ICommandManager>> commandBCF = BEHAVIORS
				.getBehaviorConnection(ICommandManager.class);
		if (!commandBCF.isDone()) {
			LOG.debug("Could not unregister as registering ");
			return;
		}
		try (IBehaviorConnection<ICommandManager> commandBC = commandBCF.get()) {
			LOG.debug("Unregistering application commands.");
			ICommandManager cm = commandBC.getBehavior();

			cm.removeHandler(_quitCommand);
			cm.removeHandler(_openCommand);
			cm.removeHandler(_saveCommand);
			cm.removeHandler(_shellCommand);
		} catch (Exception e) {
			LOG.error("Couldn't stop application commands: ", e);
		}
	}

}
