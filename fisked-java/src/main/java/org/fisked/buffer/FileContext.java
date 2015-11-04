package org.fisked.buffer;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.fisked.language.java.JavaSourceDecorator;
import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.text.ITextDecorator;

public class FileContext {
	private final File _file;
	private ITextDecorator _decorator;

	private boolean hasExtension(String extension) {
		return FilenameUtils.getExtension(_file.getName()).equals("java");
	}

	public FileContext(File file) {
		_file = file;
		if (hasExtension("java")) {
			_decorator = new JavaSourceDecorator();
		} else {
			_decorator = new ITextDecorator() {
				@Override
				public void setNeedsRedraw() {
				}

				@Override
				public AttributedString decorate(AttributedString string) {
					return string;
				}
			};
		}
	}

	public File getFile() {
		return _file;
	}

	public ITextDecorator getSourceDecorator() {
		return _decorator;
	}

}
