package org.fisked;

import org.fisked.buffer.drawing.Window;

public interface IApplication {
	void exit(int status);

	void pushPrimaryWindow(Window window);

	Window getPrimaryWindow();

	Window popPrimaryWindow();
}
