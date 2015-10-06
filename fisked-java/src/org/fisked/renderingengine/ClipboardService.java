package org.fisked.renderingengine;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.fisked.renderingengine.service.IClipboardService;

public class ClipboardService implements IClipboardService {
	private Clipboard clipboard = new Clipboard("Clipboard");
	
	@Override
	public String getClipboard() {
		String result = "";
		Transferable contents = clipboard.getContents(null);
		boolean hasTransferableText =
				(contents != null) &&
				contents.isDataFlavorSupported(DataFlavor.stringFlavor)
				;
		if (hasTransferableText) {
			try {
				result = (String)contents.getTransferData(DataFlavor.stringFlavor);
				if (result == null) result = "";
			}
			catch (UnsupportedFlavorException | IOException e){}
		}
		return result;
	}

	@Override
	public void setClipboard(String text) {
		StringSelection selection = new StringSelection(text);
		clipboard.setContents(selection, selection);
	}

}
