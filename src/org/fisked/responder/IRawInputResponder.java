package org.fisked.responder;

import jcurses.system.InputChar;

public interface IRawInputResponder {
	boolean handleInput(InputChar input);
}
