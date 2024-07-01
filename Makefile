
SRC := gameoflife
SOURCES = ${SRC}/GameOfLifeModel.java ${SRC}/GameOfLifeFrame.java ${SRC}/GameOfLife.java

.PHONY: all clean run


all:
	javac -d . ${SOURCES}

clean:
	${RM} -r gameoflife/*.class

run: all
	java gameoflife.GameOfLife