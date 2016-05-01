package org.fisked.buffer.registers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fisked.behavior.BehaviorConnectionFactory;
import org.fisked.behavior.IBehaviorConnection;
import org.fisked.renderingengine.service.IClipboardService;
import org.fisked.util.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterManager {
	private final static BehaviorConnectionFactory BEHAVIORS = new BehaviorConnectionFactory(RegisterManager.class);
	private final static Logger LOG = LoggerFactory.getLogger(RegisterManager.class);

	public static RegisterManager getInstance() {
		return Singleton.getInstance(RegisterManager.class);
	}

	private final Map<String, String> _map = new ConcurrentHashMap<>();

	public String getRegister(char reg) {
		if (reg == SYSTEM_REGISTER) {
			return getClipboard();
		} else {
			return _map.get(Character.toString(reg));
		}
	}

	public void setRegister(char reg, String str) {
		if (reg == SYSTEM_REGISTER) {
			setClipboard(str);
		} else {
			_map.put(Character.toString(reg), str);
		}
	}

	public void setClipboard(String text) {
		try (IBehaviorConnection<IClipboardService> clipboardBC = BEHAVIORS
				.getBehaviorConnection(IClipboardService.class).get()) {
			LOG.debug("Setting clipboard: " + text);
			clipboardBC.getBehavior().setClipboard(text);
			LOG.debug("Clipboard success.");
		} catch (Exception e) {
			LOG.error("Exception in clipboard: ", e);
			_map.put(Character.toString(SYSTEM_REGISTER), text);
		}
	}

	public String getClipboard() {
		try (IBehaviorConnection<IClipboardService> clipboardBC = BEHAVIORS
				.getBehaviorConnection(IClipboardService.class).get()) {
			LOG.debug("Getting clipboard");
			String result = clipboardBC.getBehavior().getClipboard();
			LOG.debug("Clipboard success: " + result);
			return result;
		} catch (Exception e) {
			LOG.error("Exception in clipboard: ", e);
			return _map.get(Character.toString(SYSTEM_REGISTER));
		}
	}

	public static final char SYSTEM_REGISTER = '*';
	public static final char UNNAMED_REGISTER = '\0';
}
