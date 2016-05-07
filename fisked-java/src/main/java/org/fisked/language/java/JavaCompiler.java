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
package org.fisked.language.java;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.collections4.IteratorUtils;
import org.fisked.buffer.Buffer;
import org.fisked.language.ISourceCompiler;

public class JavaCompiler implements ISourceCompiler {
	private final javax.tools.JavaCompiler _compiler = ToolProvider.getSystemJavaCompiler();
	private final StandardJavaFileManager _fileManager = _compiler.getStandardFileManager(null, Locale.getDefault(), null);
	private final String _classpath;
	private final String _outputdir;
	
	class JavaDiagnostic implements IDiagnostic {
		private int _lineNumber;
		private String _message;

		public JavaDiagnostic(int lineNumber, String message) {
			_lineNumber = lineNumber;
			_message = message;
		}
		
		@Override
		public int getLineNumber() {
			return _lineNumber;
		}

		@Override
		public String getMessage() {
			return _message;
		}
		
	}

	private List<IDiagnostic> compile(File file){
		final ArrayList<String> options = new ArrayList<String>();
		
		if (_classpath != null) {
			options.add("-classpath");
			options.add(System.getProperty("java.class.path") + _classpath);
		}
		
		if (_outputdir != null) {
			options.add("-d");
			options.add(_outputdir);
		}
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

		@SuppressWarnings("unchecked")
		Iterable<? extends JavaFileObject> javaFileObjectsFromFiles = _fileManager.getJavaFileObjectsFromFiles((Iterable<? extends File>) IteratorUtils.singletonIterator(file));
		List<JavaFileObject> files = new ArrayList<>();
		files.add(javaFileObjectsFromFiles.iterator().next());
		final javax.tools.JavaCompiler.CompilationTask task = _compiler.getTask(null, _fileManager, diagnostics, options, null, files);
		boolean result = task.call();
		List<IDiagnostic> message = new ArrayList<>();
		
		if (!result) {
			for (@SuppressWarnings("rawtypes") Diagnostic diagnostic : diagnostics.getDiagnostics()) {
				message.add(new JavaDiagnostic((int)diagnostic.getLineNumber(), "Error on line %d in %s" + diagnostic.getLineNumber() + diagnostic.getMessage(null)));
			}
		}
		
		try {
			_fileManager.close();
		}
		catch (  IOException e) {
			e.printStackTrace();
		}
		return message;
	}

	public JavaCompiler(String classpath, String outputdir) {
		_classpath = classpath;
		_outputdir = outputdir;
	}

	@Override
	public void compile(Buffer buffer, ICompilationResult callback) {
		List<IDiagnostic> messages = compile(buffer.getFile());
		callback.callback(messages);
	}
}
