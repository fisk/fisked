package org.fisked.buffer;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.fisked.language.java.JavaSourceDecorator;
import org.fisked.language.java.SourceDecoratorQueue;
import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.text.IBufferDecorator;

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
