package org.fisked.renderingengine;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.fisked.renderingengine.service.IClipboardService;
import org.fisked.util.OSDetector;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
public class ClipboardService implements IClipboardService {
	private final IClipboardService _implementation = getImplementation();

	private IClipboardService getImplementation() {
		if (OSDetector.isMac()) {
			return new MacClipboardService();
		} else {
			return new DefaultClipboardService();
		}
	}

	@Override
	public String getClipboard() {
		return _implementation.getClipboard();
	}

	@Override
	public void setClipboard(String value) {
		_implementation.setClipboard(value);
	}

}
