package parser;

import gameoflife.GameOfLifeModel;
import utli.Array2DWrapper;

import java.util.ArrayList;
import java.util.List;

public class GameOfLifeConfigParser {
    public GameOfLifeConfigParser(List<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
    }

    public GameOfLifeModel.State parse() {
        skipNewLines();

        while (!atEnd()) {
            if (bad) {
                return null;
            }

            skipNewLines();

            Token.Type first = peek().getType();
            switch (first) {
                case Token.Type.ROWS -> parseAssignment(Token.Type.ROWS);
                case Token.Type.COLS -> parseAssignment(Token.Type.COLS);
                case Token.Type.CHUNKS -> parseAssignment(Token.Type.CHUNKS);
                default -> error("ROWS, COLS or CHUNKS assignment expected but got " + first, peek());
            }
        }

        System.out.println();

        if (!bad) {
            System.out.println("ROWS : " + ROWS + ", COLS : " + COLS);
            System.out.println("CHUNKS :");
            for (Chunk chunk : CHUNKS) {
                System.out.println("ROW : " + chunk.ROW + ", COL : " + chunk.COL);
                System.out.println("CHUNK :");
                for (int i = 0; i < chunk.CHUNK.getRows(); ++i) {
                    for (int j = 0; j < chunk.CHUNK.getCols(); ++j) {
                        System.out.print(chunk.CHUNK.get(i, j) + " ");
                    }
                    System.out.println();
                }
            }
        }

        if (bad) {
            return null;
        }

        GameOfLifeModel.State state;
        if (CHUNKS.isEmpty()) {
            if (ROWS == null || COLS == null) {
                System.err.println("ERROR: You have to define ROWS and COLS if CHUNKS is empty");
                return null;
            }

            state = new GameOfLifeModel.State(new Byte[ROWS][COLS]);
        } else {
            if (ROWS == null) {
                ROWS = getROWSFromCHUNKS();
            }

            if (COLS  == null) {
                COLS = getCOLSFromCHUNKS();
            }

            Array2DWrapper<Byte> buffer = new Array2DWrapper<>(new Byte[ROWS][COLS]);
            buffer.setAll((byte)0);
            for (Chunk chunk : CHUNKS) {
                for (int i = 0; i < chunk.CHUNK.getRows(); i++) {
                    for (int j = 0; j < chunk.CHUNK.getCols(); j++) {
                        int cell_i = chunk.ROW + i;
                        int cell_j = chunk.COL + j;

                        if (!buffer.inBounds(cell_i, cell_j)) {
                            continue;
                        }

                        buffer.set(cell_i, cell_j, chunk.CHUNK.get(i, j));
                    }
                }
            }

            state = new GameOfLifeModel.State(buffer);
        }

        return state;
    }

    private int getROWSFromCHUNKS() {
        int maxRows = 0;
        for (Chunk chunk : CHUNKS) {
            int rows = chunk.ROW + chunk.CHUNK.getRows();
            if (rows > maxRows) {
                maxRows = rows;
            }
        }
        return Math.max(maxRows, 0);
    }

    private int getCOLSFromCHUNKS() {
        int maxCols = 0;
        for (Chunk chunk : CHUNKS) {
            int cols = chunk.COL + chunk.CHUNK.getCols();
            if (cols > maxCols) {
                maxCols = cols;
            }
        }
        return Math.max(maxCols, 0);
    }

    private void parseAssignment(Token.Type type) {
        // consume word
        Token word = advance();

        Token equals = advance();
        if (equals.getType() != Token.Type.EQUALS) {
            error("Expected EQUALS after " + type + ", but got " + equals.getType(), equals);
            return;
        }

        switch (type) {
            case Token.Type.ROWS:
            case Token.Type.COLS:
                Token[] tokens = advance(2);

                if (tokens[0].getType() != Token.Type.NUMBER) {
                    error("Expected NUMBER after EQUALS, but got " + tokens[0].getType(), tokens[0]);
                    return;
                }

                if (tokens[1].getType() != Token.Type.NEWLINE && tokens[1].getType() != Token.Type.INVALID) {
                    error("Expected NEWLINE or END_OF_FILE after NUMBER, but got " + tokens[1].getType(), tokens[1]);
                    return;
                }

                if (getVar(type) != null) {
                    error("Attempt to reassign " + type, word);
                    return;
                }

                setVar(type, Integer.parseInt(tokens[0].getLexeme()));

                break;
            case Token.Type.CHUNKS:
                Token lsquare = advance();

                if (lsquare.getType() != Token.Type.LSQUARE) {
                    error("Expected LSQUARE after EQUALS, but got " + lsquare.getType(), lsquare);
                    return;
                }

                while (!bad && peek().getType() != Token.Type.RSQUARE && peek().getType() != Token.Type.INVALID) {
                    parseChunk();
                }

                if (bad) {
                    return;
                }

                if (peek().getType() != Token.Type.RSQUARE) {
                    error("Could not find RSQUARE", lsquare);
                    return;
                }

                Token[] chunksEnd = advance(2);

                // chunksEnd[0] is RSQUARE

                if (chunksEnd[1].getType() != Token.Type.NEWLINE && chunksEnd[1].getType() != Token.Type.INVALID) {
                    error("Expected NEWLINE or END_OF_FILE after RSQUARE, but got " + chunksEnd[1].getType(), chunksEnd[1]);
                    return;
                }

                break;
            default:
                throw new AssertionError("parseAssignment() called with incorrect type " + type);
        }
    }

    private void parseChunk() {
        skipNewLines();

        Token lsquirly = advance();

        if (lsquirly.getType() != Token.Type.LSQUIRLY) {
            error("Chunk block should start with LSQUIRLY, but got " + lsquirly, lsquirly);
            return;
        }

        Token newLine = advance();
        if (newLine.getType() != Token.Type.NEWLINE) {
            error("Expected NEWLINE afer LSQUIRLY, but got " + newLine.getType(), newLine);
            return;
        }

        skipNewLines();

        Integer ROW = null;
        Integer COL = null;
        List<Chunk> chunks = new ArrayList<>();
        while (!bad && peek().getType() != Token.Type.RSQUIRLY && peek().getType() != Token.Type.INVALID) {
            Token word = advance();

            Token equals = advance();
            if (equals.getType() != Token.Type.EQUALS) {
                error("Expected EQUALS after " + word.getType() + ", but got " + equals.getType(), equals);
                return;
            }

            switch (word.getType()) {
                case Token.Type.ROW:
                case Token.Type.COL:
                    Token[] tokens = advance(2);

                    if (tokens[0].getType() != Token.Type.NUMBER) {
                        error("Expected NUMBER after EQUALS, but got " + tokens[0].getType(), tokens[0]);
                        return;
                    }

                    if (tokens[1].getType() != Token.Type.NEWLINE) {
                        error("Expected NEWLINE after NUMBER, but got " + tokens[1].getType(), tokens[1]);
                        return;
                    }

                    if (word.getType() == Token.Type.ROW) {
                        if (ROW != null) {
                            error("Attempt to reassign " + word.getType(), word);
                            return;
                        }

                        ROW = Integer.parseInt(tokens[0].getLexeme());
                    } else if (word.getType() == Token.Type.COL) {
                        if (COL != null) {
                            error("Attempt to reassign " + word.getType(), word);
                            return;
                        }

                        COL = Integer.parseInt(tokens[0].getLexeme());
                    } else {
                        throw new AssertionError("Unreachable code: neither ROW nor COL in 'case ROW: case COL'");
                    }

                    break;
                case Token.Type.CHUNK:
                    List<List<Byte>> cells = parseChunkLiteral();

                    if (bad) {
                        return;
                    }

                    if (ROW == null || COL == null) {
                        error("Should define ROW and COL before defining CHUNK", word);
                        return;
                    }

                    chunks.add(new Chunk(ROW, COL, cells));
                    break;
                default:
            }

            skipNewLines();
        }

        if (bad) {
            return;
        }

        if (peek().getType() != Token.Type.RSQUIRLY) {
            error("Could not find RSQUIRLY", lsquirly);
            return;
        }

        Token[] chunkEnd = advance(2);

        // chunkEnd[0] is RSQURLY

        if (chunkEnd[1].getType() != Token.Type.NEWLINE) {
            error("Expected NEWLINE after RSQUIRLY, but got " + chunkEnd[1].getType(), chunkEnd[1]);
            return;
        }

        this.CHUNKS.addAll(chunks);
    }

    private List<List<Byte>> parseChunkLiteral() {
        skipNewLines();

        Token openQuote = advance();
        if (openQuote.getType() != Token.Type.QUOTE) {
            error("Chunk literal shouls start with QUOTE, but got " + openQuote.getType(), openQuote);
            return null;
        }

        Integer cols = null;
        List<List<Byte>> cells = new ArrayList<>();
        cells.add(new ArrayList<>());

        int curCols = 0;
        while (peek().getType() != Token.Type.QUOTE && peek().getType() != Token.Type.INVALID) {
            Token cell = advance();

            switch (cell.getType()) {
                case Token.Type.NEWLINE -> {
                    if (cols != null) {
                        if (cols != curCols) {
                            error("Chunk literal is not a rectangle", cell);
                            return null;
                        }
                    } else {
                        cols = curCols;
                    }
                    curCols = 0;
                    cells.add(new ArrayList<>());
                }
                case Token.Type.DOT -> {
                    cells.getLast().add((byte) 0);
                    curCols++;
                }
                case Token.Type.STAR -> {
                    cells.getLast().add((byte) 1);
                    curCols++;
                }
                default -> {
                    error("Unexpected token in chunk literal: " + cell.getType(), cell);
                    return null;
                }
            }
        }

        Token closeQuote = advance();

        if (closeQuote.getType() != Token.Type.QUOTE) {
            error("Unterminated chunk literal", openQuote);
            return null;
        }

        if (cells.isEmpty() || cells.getFirst().isEmpty()) {
            error("Bad chunk literal", closeQuote);
        }

        return cells;
    }

    private void error(String message, Token badToken) {
        System.err.printf("ERROR:%d:%d: %s\n", badToken.getLine(), badToken.getColumn(), message);
        System.err.println();

        bad = true;
    }

    private boolean atEnd() {
        return position >= tokens.size();
    }

    private Token advance() {
        return atEnd() ? Token.INVALID : tokens.get(position++);
    }

    private Token advanceExceptNewLine() {
        skipNewLines();
        return advance();
    }

    private Token[] advance(int n) {
        if (n <= 0) {
            return new Token[0];
        }

        Token[] peeked = peek(n);
        position += n;
        return peeked;
    }

    private Token peek() {
        return atEnd() ? Token.INVALID : tokens.get(position);
    }

    private Token[] peek(int n) {
        if (n <= 0) return new Token[0];

        Token[] peeked = new Token[n];
        for (int i = 0; i < n; i++) {
            int idx = position + i;

            if (idx < tokens.size()) {
                peeked[i] = tokens.get(idx);
            } else {
                peeked[i] = Token.INVALID;
            }
        }

        return peeked;
    }

    private void skipNewLines() {
        while (peek().getType() == Token.Type.NEWLINE) {
            position++;
        }
    }

    private Integer getVar(Token.Type type) {
        return switch (type) {
            case Token.Type.ROWS -> ROWS;
            case Token.Type.COLS -> COLS;
            default -> throw new AssertionError("getVar() called with incorrect type " + type);
        };
    }

    private void setVar(Token.Type type, Integer value) {
        switch (type) {
            case Token.Type.ROWS -> ROWS = value;
            case Token.Type.COLS -> COLS = value;
            default -> throw new AssertionError("setVar() called with incorrect type " + type);
        }
    }


    private List<Token> tokens;
    private int position;

    private boolean bad = false;

    Integer ROWS = null;
    Integer COLS = null;
    List<Chunk> CHUNKS = new ArrayList<>();

    private static class Chunk {
        public int ROW;
        public int COL;
        public Array2DWrapper<Byte> CHUNK;

        public Chunk(int row, int col, List<List<Byte>> cells) {
            this.ROW = row;
            this.COL = col;

            Byte[][] cellsArray = new Byte[cells.size()][];
            for (int i = 0; i < cells.size(); i++) {
                cellsArray[i] = cells.get(i).toArray(new Byte[cells.get(i).size()]);
            }

            this.CHUNK = new Array2DWrapper<>(cellsArray);
        }

        public Chunk(int row, int col, Array2DWrapper<Byte> CHUNKS) {
            this.ROW = row;
            this.COL = col;
            this.CHUNK = CHUNKS;
        }
    }

}

