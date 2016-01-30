package org.fisked.launcher.service;

public interface ILauncherService {
	String[] getMainArgs();

	void stop(int code);

	void printBundles();
}
