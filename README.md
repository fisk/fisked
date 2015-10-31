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
  - move to next word with w, the end of the word with e and previous word with b. These commands are motions
  - Use d <motion> to delete something and c <motion> to delete something and then enter insert mode. These commands are operators
  - enter input mode at point + 1 with a
  - enter input mode at end of line with A
  - enter input mode on new line under current line with o
  - enter input mode on new line on previous line with O
  - paste after with p and before cursor with P
  - go to beginning of file with G and end of file with gg
  - navigate to the beginning of the line with 0 and end of line with $
  - navigate to the next character X with fX, same backwards with F instead of f
  - repeat any navigation related command by putting a number prefix first, e.g. 200j to go down 200 lines or 10fK to find the 10th instance of 'K'
  - repeaters also work with operators, so 20 d w would delete 20 words for example

* Visual mode
  - select text with basic navigations
  - delete selected text with d
  - switch selection endpoints with o
  - copy selection with y
  - exit with ESC

* Commands (enter by writing colon)
  - :e <file> opens file
  - :q exits fisked
  - :w saves current file
  - :r runs command and inserts text into current buffer
  - :ruby evaluates selected text as ruby and inserts it into the document
  - :python evaluates selected text as python and inserts it into the document
  - cancel writing command with ESC

This readme file was edited with this text editor because it is awesome.
DAYUMN!