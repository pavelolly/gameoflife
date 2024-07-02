
.PHONY: all clean run


all:
	javac gameoflife/GameOfLife.java

clear:
	${RM} -r gameoflife/*.class

clean:
	${RM} -r gameoflife/*.class

run: all
	java gameoflife.GameOfLife