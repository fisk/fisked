package org.fisked.email;

public class UnsupportedProtocolException extends RuntimeException {
	public UnsupportedProtocolException(String string) {
		super(string);
	}

	private static final long serialVersionUID = 1L;
}
