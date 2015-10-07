package org.fisked.scm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.fisked.log.Log;

public class GitRepo implements ISCMRepository {
	private FileRepository _repository;
	
	public GitRepo(Path path) throws IOException {
		_repository = new FileRepository(path.toFile());
	}
	
	private static Path findGitRoot(Path path) {
		if (path == null) return null;
		Path maybeGit = path.resolve(".git");
		if (maybeGit.toFile().exists()) {
			return maybeGit;
		}
		Path parent = maybeGit.getParent();
		return findGitRoot(parent);
	}
	
	public static GitRepo getRepoForPath(File file) {
		file = file.getAbsoluteFile();
		File parent = file.getParentFile();
		Path path = parent.toPath().normalize();
		Path gitRoot = findGitRoot(path);
		
		if (gitRoot != null) {
			try {
				return new GitRepo(gitRoot);
			} catch (IOException e) {
				return null;
			}
		}
		return null;
	}
	
	public String getBranchName() {
		try {
			return _repository.getBranch();
		} catch (IOException e) {
			return "unknown";
		}
	}

	@Override
	public String getSCMName() {
		return "git";
	}
}
