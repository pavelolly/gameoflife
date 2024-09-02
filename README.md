# Conway's Game of Life

Game of Life implementation in Java with java.swing

## UI

To control the game you can use panel with buttons at the bottom of the window.
You can start and stop the simulation or run it step by step. You can also clear the field and reset it to its initial state.
All of these actions have keys attached to them:
- ```Space``` - start/stop
- ```Enter``` - step
- ```R``` - reset
- ```C``` - clear

## Config

There is config file with inititla state of the game `config-example.txt`

The syntax is the following:
- define ROWS and COLS which are dimensions of the grid
- define CHUNKS as list of objects in curly braces
- every object must contain ROW, COL and CHUNK which are position and layout of some part of the grid
- CHUNK must be defined as sequence of dots (```'.'```) and stars (```'*'```) surrounded with quotes (```'"'```)
- dots are dead cells and stars are alive ones
- the sequence must have rectangular form where rows are separated with newlines

config-example.txt:
```
ROWS = 100
COLS = 200

CHUNKS = [
{
ROW = 0
COL = 0
CHUNK = "..................................
         ..................................
         .............*....................
         .............**........*****......
         ...........*.*.*..................
         .................................."
}
{
ROW = 0
COL = 0
CHUNK = "***
         ***
         ***"
}
{
ROW = 45
COL = 45
CHUNK = "................
         ****************
         ................"
}
]
```


