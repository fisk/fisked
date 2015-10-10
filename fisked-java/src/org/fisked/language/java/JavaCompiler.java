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
