package org.fisked.scm;

import java.io.File;

public class SCMRepositoryResolver {
	public static final SCMRepositoryResolver _instance = new SCMRepositoryResolver();
	
	public static SCMRepositoryResolver getInstance() {
		return _instance;
	}
	
	public ISCMRepository getRepositoryForFile(File file) {
		if (file == null) return null;
		GitRepo gitRepo = GitRepo.getRepoForPath(file);
		if (gitRepo != null) {
			return gitRepo;
		}
		return null;
	}
}
