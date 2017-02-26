package org.fisked.language.cpp.rtags;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RTagsThread extends Thread {
	private static final RTagsThread _instance = new RTagsThread();
	private final BlockingQueue<RTagsOperation> _queue = new LinkedBlockingQueue<>();
	private volatile boolean _terminating = false;

	private class RTagsOperation {

	}

	public RTagsThread() {
		super("RTagsThread");
		start();
	}

	@Override
	public void run() {
		while (!_terminating) {
			try {
				RTagsOperation op = _queue.take();
			} catch (InterruptedException e) {
			}
		}
	}

	public static void terminate() {
		_instance._terminating = true;
		_instance.interrupt();
	}

}
