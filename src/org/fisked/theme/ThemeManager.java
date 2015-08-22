package org.fisked.theme;

public class ThemeManager {
	private static ThemeManager _sharedInstance;
	
	public static ThemeManager getThemeManager() {
		if (_sharedInstance != null) return _sharedInstance;
		synchronized (ThemeManager.class) {
			if (_sharedInstance == null) _sharedInstance = new ThemeManager();
		}
		return _sharedInstance;
	}
	
	private ITheme _currentTheme = new FiskedDefaultTheme();
	
	public ITheme getCurrentTheme() {
		return _currentTheme;
	}
}
