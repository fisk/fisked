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
package org.fisked.project;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.fisked.fileindex.FileIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Project {
	// TODO: make external module
	private static Map<File, Project> _projectMap = new HashMap<>();
	private final File _rootDirectory;
	private final File _indexDirectory;
	private FileIndexer _index = null;
	private final static Logger LOG = LoggerFactory.getLogger(Project.class);
	private boolean _indexingDone = false;

	private IgnoreFiles _ignoreFiles = null;

	public File getRootDirectoryFile() {
		return _rootDirectory.getAbsoluteFile();
	}

	public Path getRootDirectoryPath() {
		return getRootDirectoryFile().toPath();
	}

	public File getProjectDirectoryFile() {
		return getProjectDirectoryPath().toFile();
	}

	public Path getProjectDirectoryPath() {
		return getRootDirectoryPath().resolve(".fisked");
	}

	private Project(File rootDirectory) {
		_rootDirectory = rootDirectory;
		_indexDirectory = new File(new File(rootDirectory, ".fisked"), "index");
		try {
			LOG.debug("Indexing file: " + rootDirectory);
			LOG.debug("Indexing path: " + rootDirectory.getAbsolutePath().toString());
			FileUtils.deleteDirectory(_indexDirectory);
			_ignoreFiles = new IgnoreFiles(this);
			_index = new FileIndexer(_indexDirectory.getAbsolutePath(), _ignoreFiles);
		} catch (Exception e) {
			LOG.error("Error initializing project: ", e);
		}
	}

	private static Project getProjectMap(File file, File match) {
		LOG.debug("Project match: " + match.toString());
		Project project = _projectMap.get(match);
		if (project == null) {
			LOG.debug("Create new project for match: " + match.toString());
			project = new Project(file);
			_projectMap.put(match, project);
		}
		return project;
	}

	public static Project getProject(Path path) {
		LOG.debug("Get project path: " + path);
		final String postfix = ".fisked";
		File file;
		if (path == null) {
			path = Paths.get(".").toAbsolutePath().normalize();
			file = path.toFile();
			File match = new File(file, postfix);
			return getProjectMap(file, match);
		}
		file = path.toFile();
		if (file.isFile()) {
			file = file.getParentFile();
		}
		File match = new File(file, postfix);
		if (match.exists()) {
			LOG.debug("Found match file: " + file + ", with postfix: " + match);
			return getProjectMap(file, match);
		} else {
			return getProject(path.getParent());
		}
	}

	public static Project createProject(Path path) {
		Path projectPath = path.resolve(".fisked");
		File projectFile = projectPath.toAbsolutePath().toFile();
		LOG.debug("Project path: " + projectPath.toString());
		projectFile.mkdirs();

		return getProject(projectPath);
	}

	public List<String> searchFilePath(String text) {
		try {
			if (!_indexingDone) {
				_indexingDone = true;
				_index.indexFileOrDirectory(_rootDirectory.getAbsolutePath());
			}
			List<String> paths = new ArrayList<>();
			for (Document document : _index.searchPath(text)) {
				paths.add(document.get("path"));
			}
			return paths;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
