package org.fisk.fisked.event;

import org.fisk.fisked.utils.LogFactory;
import org.slf4j.Logger;

import com.googlecode.lanterna.input.KeyType;

public class MotionResponder implements EventResponder {
    private static final Logger _log = LogFactory.createLog();
    
    private String _motion;
    private EventResponder _responder;
    
    private Responder _delegate;
    
    public static interface Responder {
        void respond(int count);
    }
    
    private EventResponder getMotionResponder(String prefixStr) {
        _log.info("Getting motion responder: " + prefixStr);
        var match = new StringBuilder();
        if (!prefixStr.equals("")) {
            for (int i = 0; i < prefixStr.length(); ++i) {
                if (i != 0) {
                    match.append(" ");
                }
                match.append(Character.toString(prefixStr.charAt(i)));
            }
            match.append(" ");
        }
        match.append(_motion);
        _log.info("Getting motion responder: " + _motion);
        _log.info("Getting motion responder: " + match);
        return new TextEventResponder(match.toString(), () -> {
            if (prefixStr.equals("")) {
                _delegate.respond(1);
            } else {
                _delegate.respond(Integer.parseInt(prefixStr));
            }
        });
    }
    
    private EventResponder getInitialResponder() {
        return new EventResponder() {
            private StringBuffer _prefix = new StringBuffer();

            @Override
            public Response processEvent(KeyStrokeEvent event) {
                if (event.getKeyStroke().getKeyType() != KeyType.Character) {
                    _responder = getMotionResponder(_prefix.toString());
                    return Response.MAYBE;
                }
                int diff = '9' - event.getKeyStroke().getCharacter();
                if (diff >= 10 || diff < 0) {
                    _responder = getMotionResponder(_prefix.toString());
                    return Response.MAYBE;
                }
                _prefix.append(Character.toString(event.getKeyStroke().getCharacter()));
                return Response.MAYBE;
            }

            @Override
            public void respond() {
            }
        };
    }

    public MotionResponder(String motion, Responder responder) {
        _motion = motion;
        _responder = getInitialResponder();
        _delegate = responder;
    }
    
    private EventResponder _matched;

    @Override
    public Response processEvent(KeyStrokeEvent event) {
        _matched = _responder;
        var response = _responder.processEvent(event);
        if (response != Response.MAYBE) {
            _responder = getInitialResponder();
        }
        return response;
    }

    @Override
    public void respond() {
        _matched.respond();
    }
}
