package parser;

import gameoflife.GameOfLife;
import gameoflife.GameOfLifeModel;
import utli.Array2DWrapper;

import java.util.ArrayList;
import java.util.List;

public class GameOfLifeConfigParser {
    List<Token> tokens;
    int posiiton;

    boolean bad = true;

    Integer ROWS;
    Integer COLS;
    ArrayList<Chunk> CHUNKS = new ArrayList<>();

    private class Chunk {
        Array2DWrapper<Byte> chunk;
        int row;
        int col;

        public Chunk(Array2DWrapper<Byte> chunk, int row, int col) {
            this.chunk = chunk;
            this.row = row;
            this.col = col;
        }
    }

    public GameOfLifeConfigParser(List<Token> tokens) {
        this.tokens = tokens;
        this.posiiton = 0;
    }

    public GameOfLifeModel.State parse() {
        if (this.tokens == null) {
            return null;
        }

        while (!this.atEnd()) {
            if (this.bad) {
                return null;
            }

            this.skipnewlines();

            Token token = advance();

            if (!token.getType().isWord()) {
                error("Word expected but got "+token.getType(), token);
                continue;
            }

            Token.Type type = token.getType();

            switch (type) {
                case ROWS:
                case COLS:
                    this.parseGlobalAssignNumber(token);
                case CHUNKS:
                    this.parseAssignChunks();
                default:
                    error(token.getType()+" can only be assigned inside a chunk", token);
            }
        }

        return this.makeGameOfLifeState();
    }

    private GameOfLifeModel.State makeGameOfLifeState() {
        if (this.ROWS == null) {
            error("ROWS was never assigned");
        }
        if (this.COLS == null) {
            error("COLS was never assigned");
        }
        if (this.CHUNKS.isEmpty()) {
            error("CHUNKS were never added");
        }

        if (this.bad) {
            return null;
        }

        GameOfLifeModel.State state = new GameOfLifeModel.State(new Byte[this.ROWS][this.COLS]);
        for (Chunk chunk : this.CHUNKS) {
            for (int i = 0; i < chunk.chunk.getRows(); i++) {
                for (int j = 0; j < chunk.chunk.getCols(); j++) {
                    int ci = chunk.row + i;
                    int cj = chunk.col + j;

                    if (state.field.inBounds(ci, cj)) {
                        state.field.set(ci, cj, chunk.chunk.get(i, j));
                    }
                }
            }
        }

        return state;
    }

    private void parseGlobalAssignNumber(Token wordToken) {
        if (wordToken.getType() == Token.Type.ROWS && this.ROWS != null) {
            error("Attempt to reassign ROWS", wordToken);
            return;
        }
        if (wordToken.getType() == Token.Type.COLS && this.COLS != null) {
            error("Attempt to reassign COLS", wordToken);
            return;
        }

        this.skipnewlines();

        Token token = advance();

        if (token.getType() != Token.Type.EQUALS) {
            error("Expected '=' after "+wordToken.getType()+" but got "+token.getType(), token);
            return;
        }

        this.skipnewlines();

        token = advance();

        if (token.getType() != Token.Type.NUMBER) {
            error("Expected number to assign to "+wordToken.getType()+" but got "+token.getType(), token);
            return;
        }

        Integer value = Integer.parseInt(token.getLexeme());

        this.skipnewlines();

        token = advance();

        if (token.getType() != Token.Type.SEMICOLON) {
            error("Expected ';' after assignment but got "+token.getType(), token);
            return;
        }

        switch (wordToken.getType()) {
            case ROWS:
                this.ROWS = value;
                break;
            case COLS:
                this.COLS = value;
                break;
            default:
                throw new AssertionError("Unreachable: wordToken is neither ROWS nor COLS");
        }
    }

    private void parseAssignChunks() {
        this.skipnewlines();
        Token token = advance();

        if (token.getType() != Token.Type.EQUALS) {
            error("Expected '=' after CHUNKS but got "+token.getType(), token);
            return;
        }

        this.skipnewlines();

        token = advance();

        if (token.getType() != Token.Type.LSQUIRLY) {
            error("Expected '{' as the start of CHUNKS but got "+token.getType(), token);
            return;
        }

        this.skipnewlines();

        while (this.parseChunk()) {
            if (this.bad) {
                return;
            }
        }

        this.skipnewlines();

        token = advance();

        if (token.getType() != Token.Type.RSQUIRLY) {
            error("Expected '}' as the end of CHUNKS but got "+token.getType(), token);
            return;
        }
    }

    private boolean parseChunk() {
        this.skipnewlines();

        Token token = peek();

        if (token.getType() == Token.Type.RSQUIRLY) {
            return false;
        }

        token = advance();

        if (token.getType() != Token.Type.LSQUIRLY) {
            error("Expected '{' as the start of a chunk but got "+token.getType(), token);
            return false;
        }

        this.skipnewlines();

        Integer row = null;
        Integer col = null;
        Array2DWrapper<Byte> chunk = null;
        while (this.peek().getType() != Token.Type.RSQUIRLY) {
            if (this.bad) {
                return false;
            }

            token = advance();

            if (token.getType() == Token.Type.NEWLINE) {
                continue;
            }

            switch (token.getType()) {
                case ROW:
                    if (row != null) {
                        error("Attempt to reassign ROWS", token);
                        break;
                    }

                    row = this.parseChunkAssignNumber(token);
                case COL:
                    if (col != null) {
                        error("Attempt to reassign ROWS", token);
                        break;
                    }

                    col = this.parseChunkAssignNumber(token);
                case CHUNK:
                    chunk = this.parseAssignChunk();
                default:
                    error("Unexpected token: "+token.getType()+", tou can only assign ROW, COL and CHUNK inside chunk block", token);
                    return false;
            }
        }

        if (row == null) {
            error("ROW wasn't assigned in a chunk", token);
        }
        if (col == null) {
            error("COL wasn't assigned in a chunk", token);
        }
        if (chunk == null) {
            error("CHUNK wasn't assigned in a chunk", token);
        }

        advance();

        if (this.bad) {
            return false;
        }

        CHUNKS.add(new Chunk(chunk, row, col));

        return true;
    }

    private Integer parseChunkAssignNumber(Token wordToken) {
        this.skipnewlines();

        Token token = advance();

        if (token.getType() != Token.Type.EQUALS) {
            error("Expected '=' after "+wordToken.getType()+" but got "+token.getType(), token);
            return null;
        }

        this.skipnewlines();

        token = advance();

        if (token.getType() != Token.Type.NUMBER) {
            error("Expected number to assign to "+wordToken.getType()+" but got "+token.getType(), token);
            return null;
        }

        Integer value = Integer.parseInt(token.getLexeme());

        this.skipnewlines();

        token = advance();

        if (token.getType() != Token.Type.SEMICOLON) {
            error("Expected ';' after assignment but got "+token.getType(), token);
            return null;
        }

        return value;
    }

    private Array2DWrapper<Byte> parseAssignChunk() {
        Integer rows = 0, cols = null;
        ArrayList<Byte> chunk = new ArrayList<>();

        this.skipnewlines();

        Token token = advance();

        if (token.getType() != Token.Type.EQUALS) {
            error("Expected '=' after CHUNK but got "+token.getType(), token);
            return null;
        }

        this.skipnewlines();

        while (this.peek().getType() != Token.Type.SEMICOLON) {
            if (this.bad) {
                return null;
            }

            int curCols = 0;

            token = advance();

            switch (token.getType()) {
                case DOT:
                    chunk.add((byte)0);
                    ++curCols;
                case STAR:
                    chunk.add((byte)1);
                    ++curCols;
                case NEWLINE:
                    if (cols == null) {
                        cols = curCols;
                    } else {
                        if (curCols != cols) {
                            error("Assign chunk which is not a rectangle", token);
                            return null;
                        }
                    }

                    ++rows;
                default:
                    error("Unexpected token when parsing chunk: "+token.getType(), token);
                    return null;
            }
        }

        if (rows == null || cols == null || chunk == null) {
            throw new AssertionError("rows, cols or chunk are null");
        }

        Byte[][] buffer = new Byte[rows][cols];
        for (int i = 0; i < rows; i++) {
            buffer[i] = (Byte[])chunk.subList(i * buffer[i].length, (i + 1) * buffer[i].length).toArray();
        }

        return new Array2DWrapper<Byte>(buffer);
    }


    private void error(String message, Token token) {
        System.out.printf("ERROR %d:%d: %s\n", token.getLine(), token.getColumn(), message);
        this.bad = true;
    }

    private void error(String message) {
        System.out.println("GLOBAL ERROR: "+message);
        this.bad = true;
    }

    private boolean atEnd() {
        return this.posiiton >= this.tokens.size();
    }

    private Token advance() {
        if (atEnd()) {
            return new Token(Token.Type.INVALID, null, 0, 0);
        }

        return this.tokens.get(this.posiiton++);
    }

    private Token peek() {
        if (atEnd()) {
            return new Token(Token.Type.INVALID, null, 0, 0);
        }

        return this.tokens.get(this.posiiton);
    }

    private void skipnewlines() {
        while (this.peek().getType() == Token.Type.NEWLINE) {
            advance();
        }
    }

}
