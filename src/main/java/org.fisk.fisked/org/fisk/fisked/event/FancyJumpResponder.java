package org.fisk.fisked.event;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.fisk.fisked.mode.NormalMode;
import org.fisk.fisked.text.AttributedString;
import org.fisk.fisked.text.BufferContext;
import org.fisk.fisked.text.TextLayout.Glyph;
import org.fisk.fisked.utils.LogFactory;
import org.slf4j.Logger;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyType;

public class FancyJumpResponder implements EventResponder {
    private BufferContext _bufferContext;
    private EventResponder _currentResponder = getInitialResponder();
    private Runnable _postfix = null;
    private List<WordResponder> _installedResponders = new ArrayList<>();
    
    private static final Logger _log = LogFactory.createLog();
    
    private char _prefix;
    
    public FancyJumpResponder(BufferContext bufferContext, char prefix) {
        _bufferContext = bufferContext;
        _prefix = prefix;
    }

    private class WordResponder implements EventResponder {
        private int _position;
        private TextEventResponder _responder;
        private ListEventResponder _parent;
        private String _matchString;
        private String _matchStringRaw;
        private int _matches = 0;
        
        public WordResponder(int position, int number, int max, char character, ListEventResponder parent) {
            _parent = parent;
            _position = position;
            var str = new StringBuilder();
            max = (int)Math.floor(Math.log((double)max) / Math.log(52.0));
            int i;
            for (i = 0;; ++i) {
                int c = number % 52;
                str.append(" ");
                if (c < 26) {
                    str.append(Character.toString('a' + c));
                } else {
                    str.append(Character.toString('A' + c - 26));
                }
                if (number < 52) {
                    break;
                } else {
                    number /= 52;
                }
            }
            for (; i < max; ++i) {
                str.append(" a");
            }
            str.reverse();
            _matchStringRaw = str.toString().replace(" ", "");
            str.insert(0, Character.toString(_prefix) + " " + Character.toString(character) + " ");
            _matchString = str.toString();
            _log.info("Word match: " + str);
            _responder = new TextEventResponder(str.toString(), () -> {
                _log.info("Respond 3");
                _bufferContext.getBuffer().getCursor().setPosition(_position);
                _bufferContext.getBufferView().adaptViewToCursor();
                _currentResponder = getInitialResponder();
                _installedResponders.clear();
            });
        }
        
        @Override
        public Response processEvent(KeyStrokeEvent event) {
            var result = _responder.processEvent(event);
            switch (result) {
            case YES:
                _log.info("Matched word at " + _position);
                break;
            case NO:
                _log.info("Failed match word at " + _position + ", matchString: " + _matchString + ", eventChar: " + event.getKeyStroke().getCharacter());
                _postfix = () -> {
                    _parent.removeEventResponder(this);
                    _installedResponders.remove(this);
                };
                break;
            case MAYBE:
                _matches++;
                _log.info("Failed match word at " + _position + ", matchString: " + _matchString + ", eventChar: " + event.getKeyStroke().getCharacter());
                _log.info("Maybe match word at " + _position + ", current char: " + _matchStringRaw.substring(_matches, _matches + 1));
                break;
            }
            return result;
        }
        
        @Override
        public void respond() {
            _log.info("Respond 2");
            _responder.respond();
        }

        public AttributedString decorate(Glyph glyph, AttributedString character) {
            if (glyph.getPosition() != _position) {
                return character;
            }
            return AttributedString.create(_matchStringRaw.substring(_matches, _matches + 1), 
                    TextColor.ANSI.RED, TextColor.ANSI.DEFAULT);
        }
    }
    
    
    private EventResponder getFirstCharResponder() {
        return new EventResponder() {
            @Override
            public Response processEvent(KeyStrokeEvent event) {
                var key = event.getKeyStroke();
                if (key.getKeyType() == KeyType.Character) {
                    var responders = new ListEventResponder();
                    var range = _bufferContext.getTextLayout().getGlyphRange();
                    if (range.getLength() == 0) {
                        return Response.NO;
                    }
                    _log.info("Range: " + range + ", length: " + _bufferContext.getBuffer().toString().length());
                    var pattern = Pattern.compile("\\b" + key.getCharacter(), Pattern.MULTILINE);
                    var str = _bufferContext.getBuffer().getString().substring(range.getStart(), range.getEnd());
                    var matcher = pattern.matcher(str);
                    var matches = new ArrayList<Integer>();
                    while (matcher.find()) {
                        matches.add(matcher.start());
                    }
                    int number = 0;
                    var iter = matches.iterator();
                    while (iter.hasNext()) {
                        int match = iter.next();
                        _log.info("Adding word responder: " + (range.getStart() + match) + ", " + number);
                        var responder = new WordResponder(range.getStart() + match, number++, matches.size(), key.getCharacter(), responders);
                        responders.addEventResponder(responder);
                        _installedResponders.add(responder);
                    }
                    if (number > 0) {
                        _log.info("Setting word responders");
                        _currentResponder = responders;
                        return Response.MAYBE;
                    }
                }
                return Response.NO;
            }

            @Override
            public void respond() {
            }
        };
    }
    
    private EventResponder getInitialResponder() {
        return new EventResponder() {
            @Override
            public Response processEvent(KeyStrokeEvent event) {
                var key = event.getKeyStroke();
                if (key.getKeyType() == KeyType.Character && key.getCharacter() == _prefix) {
                    _currentResponder = getFirstCharResponder();
                    _log.info("Set first char responder");
                    return Response.MAYBE;
                } else {
                    return Response.NO;
                }
            }

            @Override
            public void respond() {
            }
        };
    }
    
    @Override
    public Response processEvent(KeyStrokeEvent event) {
        var result = _currentResponder.processEvent(event);
        if (result == Response.NO) {
            _currentResponder = getInitialResponder();
            _installedResponders.clear();
            _log.info("Restoring initial responder");
        }
        if (_postfix != null) {
            _postfix.run();
            _postfix = null;
        }
        return result;
    }

    @Override
    public void respond() {
        _log.info("Respond 1");
        _currentResponder.respond();
    }

    public AttributedString decorate(Glyph glyph, AttributedString character) {
        for (var responder: _installedResponders) {
            character = responder.decorate(glyph, character);
        }
        return character;
    }
}
