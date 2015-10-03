# fisked
This is going to be the best new text editor in history.
Completely written in Java.

Build with $ mvn package
Run with $ ./fisked [FILE]

Current features:
* Input mode (enter with i, exit with escape)
  - write text

* Normal mode
  - enter input mode with i
  - write command with :
  - navigate with hjkl vim-style
  - scroll with CTRL-E and CTRL-Y

* Commands (enter by writing colon)
  - :e <file> opens file
  - :w saves current file
  - :r runs command and inserts text into current buffer

This readme file was edited with this text editor because it is awesome.
DAYUMN!