# fisked
This is going to be the best new text editor in history.
Completely written in Java.

Build with $ mvn package
Run with $ ./fisked [FILE]

Current features:
* Input mode (enter with i, exit with ESC)
  - write text

* Normal mode
  - enter input mode with i
  - enter visual mode with v
  - write command with :
  - navigate with hjkl vim-style
  - scroll with CTRL-E and CTRL-Y
  - navigate to the beginning of the line with 0 and end of line with $

* Visual mode
  - select text with basic navigations
  - delete selected text with d
  - switch selection endpoints with o
  - exit with ESC

* Commands (enter by writing colon)
  - :e <file> opens file
  - :w saves current file
  - :r runs command and inserts text into current buffer
  - exit with ESC

This readme file was edited with this text editor because it is awesome.
DAYUMN!