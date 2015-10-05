package org.fisked.buffer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.fisked.renderingengine.service.models.AttributedString;
import org.fisked.renderingengine.service.models.Color;
import org.fisked.renderingengine.service.models.Face;
import org.fisked.renderingengine.service.models.Point;
import org.fisked.theme.ITheme;
import org.fisked.theme.ThemeManager;

public class ModeLineController {
	
	private BufferWindow _window;
	
	public ModeLineController(BufferWindow window) {
		_window = window;
	}
	
	private AttributedString drawSlimRightArrow(Face face) {
		return new AttributedString("\ue0b1", face);
	}
	
	private AttributedString drawFatRightArrow(Color leftColor, Color rightColor) {
		return new AttributedString("\ue0b0", new Face(rightColor, leftColor, false));
	}
	
	private AttributedString drawSlimLeftArrow(Face face) {
		return new AttributedString("\ue0b3", face);
	}
	
	private AttributedString drawFatLeftArrow(Color leftColor, Color rightColor) {
		return new AttributedString("\ue0b2", new Face(leftColor, rightColor, false));
	}
	
	private AttributedString drawLineNumberChar(Face face) {
		return new AttributedString("\ue0a1", face);
	}
	
	private AttributedString drawBranch(Face face) {
		return new AttributedString("\ue0a0", face);
	}
	
	private AttributedString drawText(String text, Face face) {
		return new AttributedString(text, face);
	}
	
	private List<AttributedString> getLeftModelineText() {
		ITheme theme = ThemeManager.getThemeManager().getCurrentTheme();
		Face modeFace = _window.getCurrentMode().getModelineFace();
		Color modelineBackgroundColor = theme.getModelineBackgroundColorLight();
		Color modelineForegroundColor = theme.getModelineForegroundColor();
		Color modeBackgroundColor = modeFace.getBackgroundColor();
		Face modelineFace = new Face(modelineBackgroundColor, modelineForegroundColor);
		
		List<AttributedString> result = new ArrayList<>();
		result.add(drawText(" " + _window.getCurrentMode().getModeName().toUpperCase() + "  ", modeFace));
		result.add(drawFatRightArrow(modeBackgroundColor, modelineBackgroundColor));
		result.add(drawText(" " + _window.getBuffer().getFileName() + " ", modelineFace));
		result.add(drawSlimRightArrow(modelineFace));
		
		return result;
	}
	
	SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd"); 
	
	private String getDateString() {
		return dt.format(new Date());
	}
	
	private List<AttributedString> getRightModelineText() {
		ITheme theme = ThemeManager.getThemeManager().getCurrentTheme();
		Face modeFace = _window.getCurrentMode().getModelineFace();
		Color modelineBackgroundColor = theme.getModelineBackgroundColorLight();
		Color modelineForegroundColor = theme.getModelineForegroundColor();
		Color modeBackgroundColor = modeFace.getBackgroundColor();
		Face modelineFace = new Face(modelineBackgroundColor, modelineForegroundColor);
		
		Point point = _window.getBuffer().getCursor().getAbsolutePoint();
		
		List<AttributedString> result = new ArrayList<>();
		result.add(drawFatLeftArrow(modelineBackgroundColor, modeBackgroundColor));
		result.add(drawText(" ", modeFace));
		result.add(drawLineNumberChar(modeFace));
		result.add(drawText(" " + point.getY() + ":" + point.getX() + " ", modeFace));
		result.add(drawSlimLeftArrow(modeFace));
		result.add(drawText(" " + getDateString() + "  ", modeFace));
		
		return result;
	}
	
	private AttributedString getSpace(int spaces) {
		ITheme theme = ThemeManager.getThemeManager().getCurrentTheme();
		Color modelineBackgroundColor = theme.getModelineBackgroundColorLight();
		Color modelineForegroundColor = theme.getModelineForegroundColor();
		Face modelineFace = new Face(modelineBackgroundColor, modelineForegroundColor);
		
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < spaces; i++) {
			str.append(" ");
		}
		return new AttributedString(str.toString(), modelineFace);
	}

	// TODO: Allow themes to extend this
	
	public List<AttributedString> getModeLineText() {
		List<AttributedString> left = getLeftModelineText();
		List<AttributedString> right = getRightModelineText();
		
		int spaces = _window.getRootView().getClippingRect().getSize().getWidth();

		for (AttributedString str : left) {
			spaces -= str.getCharSequence().length();
		}
		
		for (AttributedString str : right) {
			spaces -= str.getCharSequence().length();
		}
		
		List<AttributedString> result = new ArrayList<>();

		result.addAll(left);
		result.add(getSpace(spaces));
		result.addAll(right);
		
		return result;
	}

}
