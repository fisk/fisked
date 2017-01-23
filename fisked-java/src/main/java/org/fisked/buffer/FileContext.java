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
package org.fisked.buffer;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.fisked.language.java.JavaSourceDecorator;
import org.fisked.language.java.SourceDecoratorQueue;
import org.fisked.text.IBufferDecorator;
import org.fisked.util.models.AttributedString;

public class FileContext {
	private final File _file;
	private IBufferDecorator _decorator;

	private boolean hasExtension(String extension) {
		return FilenameUtils.getExtension(_file.getName()).equals("java");
	}

	public FileContext(File file) {
		_file = file;
		if (hasExtension("java")) {
			_decorator = new SourceDecoratorQueue(new JavaSourceDecorator());
		} else {
			_decorator = (state, decorator) -> decorator.call(new AttributedString(state.toString()));
		}
	}

	public File getFile() {
		return _file;
	}

	public IBufferDecorator getSourceDecorator() {
		return _decorator;
	}

}
