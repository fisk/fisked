package org.fisked.mode.responder;

import java.io.File;
import java.util.List;

import org.apache.lucene.document.Document;
import org.fisked.buffer.BufferWindow;
import org.fisked.project.Project;
import org.fisked.responder.Event;
import org.fisked.responder.EventRecognition;
import org.fisked.responder.IInputResponder;
import org.fisked.responder.RecognitionState;

public class ProjectResponder implements IInputResponder {
	private final BufferWindow _window;

	public ProjectResponder(BufferWindow window) {
		_window = window;
	}

	@Override
	public RecognitionState recognizesInput(Event nextEvent) {
		return EventRecognition.matchesExact(nextEvent, ",p");
	}

	@Override
	public void onRecognize() {
		File file = _window.getBuffer().getFile();
		Project project = Project.getProject(file);
		if (project == null) {
			return;
		}
		StringBuilder builder = new StringBuilder();
		List<Document> documents = project.searchFilePath("g1");

		builder.append("Herp derp:\n");
		for (Document document : documents) {
			builder.append(document.getField("path").stringValue());
			builder.append("\n");
		}
		_window.getBuffer().appendStringAtPointLogged(builder.toString());
	}

}
