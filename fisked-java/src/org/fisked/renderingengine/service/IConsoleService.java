package org.fisked.renderingengine.service;

import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Rectangle;

public interface IConsoleService {
	void activate();
	void deactivate();
	int getChar();
	void flush();
	int getScreenWidth();
	int getScreenHeight();
	public interface IRenderingContext extends AutoCloseable {
		void moveTo(int x, int y);
		void printString(String string);
		void printString(AttributedString string);
		void clearScreen();
		void clearScreen(Color color);
		void clearRect(Rectangle rect, Color color);
		void close();
	}
	IRenderingContext getRenderingContext();
	ICursorService getCursorService();
}
