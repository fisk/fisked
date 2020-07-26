package org.fisk.fisked.event;

import java.util.regex.Pattern;

import org.fisk.fisked.text.BufferContext;
import org.fisk.fisked.utils.LogFactory;
import org.slf4j.Logger;

import com.googlecode.lanterna.input.KeyType;

public class FindResponder implements EventResponder {
    private EventResponder _currentResponder;

    private static final Logger _log = LogFactory.createLog();

    private final String _prefix;
    private final boolean _forward;
    private final BufferContext _context;

    public FindResponder(BufferContext context, String prefix, boolean forward) {
        _context = context;
        _prefix = prefix;
        _forward = forward;
        _currentResponder = getInitialResponder();
    }

    private void respond(int count, String character) {
        var pattern = Pattern.compile(character, Pattern.MULTILINE);
        for (int i = 0; i < count; ++i) {
            if (_forward) {
                _context.getBuffer().getCursor().goNext(pattern);
            } else {
                _context.getBuffer().getCursor().goPrevious(pattern);
            }
        }
    }

    private EventResponder getFirstCharResponder(int count) {
        return new EventResponder() {
            private char _character;

            @Override
            public Response processEvent(KeyStrokeEvent event) {
                if (event.getKeyStroke().getKeyType() != KeyType.Character) {
                    return Response.NO;
                } else {
                    _character = event.getKeyStroke().getCharacter();
                    return Response.YES;
                }
            }

            @Override
            public void respond() {
                FindResponder.this.respond(count, Character.toString(_character));
            }
        };
    }

    private EventResponder getInitialResponder() {
        return new EventResponder() {
            private MotionResponder _motion = new MotionResponder(FindResponder.this._prefix, (int count) -> {
                _currentResponder = getFirstCharResponder(count);
            });

            @Override
            public Response processEvent(KeyStrokeEvent event) {
                var response = _motion.processEvent(event);
                if (response == Response.YES) {
                    _log.info("Motion matched");
                    _motion.respond();
                    return Response.MAYBE;
                }
                return response;
            }

            @Override
            public void respond() {
                _motion.respond();
            }

        };
    }

    @Override
    public Response processEvent(KeyStrokeEvent event) {
        var result = _currentResponder.processEvent(event);
        if (result != Response.MAYBE) {
            _log.info("Restoring initial responder");
            _currentResponder = getInitialResponder();
        }
        return result;
    }

    @Override
    public void respond() {
        _log.info("Responding");
        _currentResponder.respond();
    }
}
