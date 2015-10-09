package org.fisked.language;

import java.util.List;

import org.fisked.buffer.Buffer;

public interface ISourceCompiler {
	public interface IDiagnostic {
		int getLineNumber();
		String getMessage();
	}
	public interface ICompilationResult {
		void callback(List<IDiagnostic> messages);
	}
	void compile(Buffer buffer, ICompilationResult callback);
}
