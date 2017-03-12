package org.fisked.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IgnoreFiles {
	private final static Logger LOG = LoggerFactory.getLogger(IgnoreFiles.class);

	private final Set<String> _ignoredPaths = new HashSet<>();
	private final List<Pattern> _ignoredRegexp = new ArrayList<>();

	private void populateIgnoreFile(String ignoreFile) {
		try (Scanner scanner = new Scanner(ignoreFile)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.length() == 0) {
					continue;
				}
				if (!line.contains("*")) {
					_ignoredPaths.add(line);
					LOG.debug("Ignoring " + line);
				} else {
					try (Scanner wildcardScanner = new Scanner(line)) {
						StringBuilder regexp = new StringBuilder();
						wildcardScanner.useDelimiter("\\*");
						if (line.charAt(0) == '*') {
							regexp.append(".*");
						}
						while (wildcardScanner.hasNext()) {
							String str = wildcardScanner.next();
							for (char character : str.toCharArray()) {
								String charString = Integer.toString(character, 16);
								StringBuilder builder = new StringBuilder();
								builder.append("\\u");
								for (int i = charString.length(); i < 4; i++) {
									builder.append("0");
								}
								builder.append(charString);
								regexp.append(builder.toString());
							}
							if (wildcardScanner.hasNext("\\*")) {
								regexp.append(".*");
							}
						}
						String regexpBuilt = regexp.toString();
						LOG.debug("Built regexp string: " + regexpBuilt);
						Pattern p = Pattern.compile(regexpBuilt);
						_ignoredRegexp.add(p);
						LOG.debug("Ignoring " + line);
					}
				}
			}
		}
	}

	public IgnoreFiles(Project project) {
		Path projectPath = project.getProjectDirectoryPath();
		Path ignorePath = projectPath.resolve("ignore");
		if (ignorePath.toFile().exists()) {
			LOG.debug("Reading ignore file: " + ignorePath.toString());
			try {
				String ignoreFile = IOUtils.toString(ignorePath.toUri());
				populateIgnoreFile(ignoreFile);
			} catch (IOException e) {
				LOG.error("Error reading ignore file: ", e);
			}
		} else {
			LOG.debug("No ignore file found");
		}
	}

	public boolean isIgnored(Path path) {
		path = path.toAbsolutePath();
		String baseName = path.getFileName().toString();
		if (_ignoredPaths.contains(baseName)) {
			return true;
		}
		for (Pattern pattern : _ignoredRegexp) {
			if (pattern.matcher(baseName).matches()) {
				return true;
			}
		}
		return false;
	}

	public boolean isIgnored(File file) {
		Path path = file.toPath();
		return isIgnored(path);
	}
}
