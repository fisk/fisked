package org.fisked.renderingengine;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.fisked.renderingengine.service.IClipboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultClipboardService implements IClipboardService {
	private final Clipboard _clipboard = new Clipboard("Clipboard");
	private final static Logger LOG = LoggerFactory.getLogger(DefaultClipboardService.class);

	@Override
	public String getClipboard() {
		String result = "";
		Transferable contents = _clipboard.getContents(null);
		boolean hasTransferableText = contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
		if (hasTransferableText) {
			try {
				LOG.debug("Get built in clipboard");
				result = (String) contents.getTransferData(DataFlavor.stringFlavor);
				LOG.debug("Got built in clipboard: " + result);
				if (result == null)
					result = "";
			} catch (UnsupportedFlavorException | IOException e) {
			}
		}
		return result;
	}

	@Override
	public void setClipboard(String text) {
		LOG.debug("Set built in clipboard: " + text);
		StringSelection selection = new StringSelection(text);
		_clipboard.setContents(selection, selection);
		LOG.debug("Set built in clipboard");
	}

}
