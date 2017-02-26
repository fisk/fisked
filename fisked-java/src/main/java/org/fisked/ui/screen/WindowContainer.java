package org.fisked.ui.screen;

import java.util.ArrayList;
import java.util.List;

import org.fisked.ui.window.Window;

public class WindowContainer {
	private WindowContainer _parent;
	private final List<WindowContainer> _children = new ArrayList<>();
	private Window _window;

	public WindowContainer() {
	}

	public Window getWindow() {
		return _window;
	}

	public List<WindowContainer> getChildren() {
		return _children;
	}

	public WindowContainer find(Window window) {
		if (_window == window) {
			return this;
		}
		for (WindowContainer child : _children) {
			WindowContainer childQuery = child.find(window);
			if (childQuery != null) {
				return childQuery;
			}
		}
		return null;
	}

	public void removeFromParent() {
		if (_parent != null) {
			_parent._children.remove(this);
			_parent = null;
		}
	}

	public void addChild(WindowContainer child) {
		child._parent = this;
		_children.add(child);
	}

	public void setWindow(Window window) {
		_window = window;
	}

	public void clearWindow() {
		_window = null;
	}

	public WindowContainer getParent() {
		return _parent;
	}
}
