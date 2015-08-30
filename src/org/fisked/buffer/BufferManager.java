package org.fisked.buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BufferManager {
	private static BufferManager _sharedInstance;
	public static BufferManager getSingleton() {
		if (_sharedInstance != null) return _sharedInstance;
		synchronized (BufferManager.class) {
			if (_sharedInstance == null) {
				_sharedInstance = new BufferManager();
			}
		}
		return _sharedInstance;
	}
	
	private List<Buffer> _buffers = new ArrayList<Buffer>();
	
	public void addBuffer(Buffer buffer) {
		_buffers.add(buffer);
	}
	
	public void removeBuffer(Buffer buffer) {
		_buffers.remove(buffer);
	}
	
	public Collection<Buffer> getContainers() {
		return _buffers;
	}

}
