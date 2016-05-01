package org.fisked.project;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.fisked.fileindex.FileIndexer;

public class Project {
	private static Map<File, Project> _projectMap = new HashMap<>();
	private final File _rootDirectory;
	private final File _projectFiles;
	private FileIndexer _index = null;

	private Project(File rootDirectory, File projectFiles) {
		_rootDirectory = rootDirectory;
		_projectFiles = projectFiles;
		try {
			_index = new FileIndexer(rootDirectory.getAbsolutePath());
			_index.indexFileOrDirectory(_rootDirectory.getAbsolutePath());
		} catch (Exception e) {
		}
	}

	private static Project getProjectMap(File file, File match) {
		Project project = _projectMap.get(match);
		if (project == null) {
			project = new Project(file, match);
			_projectMap.put(file, project);
		}
		return project;
	}

	public boolean hasProjectData() {
		return _projectFiles.exists();
	}

	public static Project getProject(File file) {
		final String postfix = ".fisked";
		if (file == null) {
			file = Paths.get(".").toAbsolutePath().normalize().toFile();
			File match = new File(file, postfix);
			return getProjectMap(file, match);
		}
		if (file.isFile()) {
			file = file.getParentFile();
		}
		File match = new File(file, postfix);
		if (match.exists()) {
			return getProjectMap(file, match);
		} else {
			return getProject(file.getParentFile());
		}
	}

	public List<Document> searchFilePath(String text) {
		try {
			return _index.searchPath(text);
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}
}
