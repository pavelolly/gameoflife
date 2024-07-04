package parser;

public class Token {
    public enum Type {
        INVALID,
        ROWS, COLS, CHUNKS, CHUNK, ROW, COL,
        NUMBER,
        EQUALS, LSQUIRLY, RSQUIRLY, SEMICOLON,
        DOT, STAR, NEWLINE, EOF;

        public boolean isWord() {
            return this == ROWS || this == COLS || this == CHUNKS || this == CHUNK ||
                    this == ROW || this == COL;
        }


        @Override
        public String toString() {
            switch (this) {
                case INVALID: return "INVALID";
                case ROWS: return "ROWS";
                case COLS: return "COLS";
                case CHUNKS: return "CHUNKS";
                case CHUNK: return "CHUNK";
                case ROW: return "ROW";
                case COL: return "COL";
                case NUMBER: return "NUMBER";
                case EQUALS: return "EQUALS";
                case LSQUIRLY: return "LSQUIRLY";
                case RSQUIRLY: return "RSQUIRLY";
                case SEMICOLON: return "SEMICOLON";
                case DOT: return "DOT";
                case STAR: return "STAR";
                case NEWLINE: return "NEWLINE";
                case EOF: return "EOF";
            }

            return null;
        }
    }

    private final Type type;
    private final String lexeme;
    private final int line;
    private final int column;

    public Token(Type type, String lexeme, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }

    public Type getType() {
        return this.type;
    }

    public String getLexeme() {
        return this.lexeme;
    }

    public int getLine() {
        return this.line;
    }

    public int getColumn() {
        return this.column;
    }

    public static Integer length(Token.Type type) {
        switch (type) {
            case EOF:
                return 0;
            case EQUALS: case LSQUIRLY: case RSQUIRLY: case SEMICOLON:
            case DOT: case STAR: case NEWLINE:
                return 1;
            case ROW: case COL:
                return 3;
            case ROWS: case COLS:
                return 4;
            case CHUNK:
                return 5;
            case CHUNKS:
                return 6;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return type.toString() + " (" + lexeme + ") line:" + line + " column:" + column;
    }

}
