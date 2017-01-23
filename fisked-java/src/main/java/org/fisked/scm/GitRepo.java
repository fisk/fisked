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
package org.fisked.scm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jgit.internal.storage.file.FileRepository;

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
		if (path.getNameCount() == 0) return null;
		Path parent = path.getParent();
		return findGitRoot(parent);
	}
	
	public static GitRepo getRepoForPath(File file) {
		file = file.getAbsoluteFile();
		if (!file.exists()) return null;
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
