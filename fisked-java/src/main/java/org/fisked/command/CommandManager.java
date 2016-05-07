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
package org.fisked.command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.fisked.buffer.BufferWindow;
import org.fisked.command.api.CommandHandlerReference;
import org.fisked.command.api.ICommandHandler;
import org.fisked.command.api.ICommandManager;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
public class CommandManager implements ICommandManager {
	private final Map<String, CommandHandlerReference> _map = new ConcurrentHashMap<>();

	@Override
	public CommandHandlerReference registerHandler(String command, ICommandHandler handler) {
		CommandHandlerReference handlerWrapper = new CommandHandlerReference(command, handler);
		_map.put(command, handlerWrapper);
		return handlerWrapper;
	}

	@Override
	public boolean handle(BufferWindow window, String command, String[] argv) {
		ICommandHandler handler = _map.get(command);
		if (handler == null) {
			return false;
		}
		handler.run(window, argv);
		return true;
	}

	@Override
	public void removeHandler(CommandHandlerReference handler) {
		_map.remove(handler.getCommand(), handler);
	}
}
