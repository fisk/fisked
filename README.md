# fisked
This is going to be the best new text editor in history.
Completely written in Java.

Build with $ mvn package
Run with $ ./fisked

Current features:
* Input mode (enter with i, exit with escape)
  - write text
  
* Normal mode
  - enter input mode with i
  - write command with :
 
* Commands (enter by writing colon)
  - :e <file> opens file
  - :w saves current file
  - :r runs command and inserts text into current buffer
  
