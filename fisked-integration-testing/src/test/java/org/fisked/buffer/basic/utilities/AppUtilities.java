package org.fisked.buffer.basic.utilities;

import org.fisked.buffer.Buffer;
import org.fisked.buffer.BufferWindow;
import org.fisked.util.models.Rectangle;

public class AppUtilities {
	public Rectangle getBounds() {
		return new Rectangle(0, 0, 1024, 1024);
	}

	public BufferWindow getWindow() {
		Rectangle rect = getBounds();
		BufferWindow window = new BufferWindow(rect);
		Buffer buffer = new Buffer();
		window.setBuffer(buffer);
		return window;
	}

	public TestEventLoop getEventLoop(BufferWindow window) {
		return new TestEventLoop(new EventBuilder(), window);
	}
}
