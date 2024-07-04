package parser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GameOfLifeConfigLexer {
    private String source;
    private int position;
    private int line;
    private int column;

    List<Token> tokens = new ArrayList<>();
    boolean bad = false;

    public GameOfLifeConfigLexer(String source) {
        this.source = source;
        this.position = 0;
        this.line = 1;
        this.column = 0;
    }

    public GameOfLifeConfigLexer(Path filepath, Charset charset) throws IOException {
        this(new String(Files.readAllBytes(filepath), charset));
    }

    public List<Token> tokenize() {
        while (!atEnd()) {
            if (bad) {
                return null;
            }

            this.skipspaces();

            char ch = peek();

            switch (ch) {
                case '=':
                    this.addToken(Token.Type.EQUALS);
                    break;
                case '{':
                    this.addToken(Token.Type.LSQUIRLY);
                    break;
                case '}':
                    this.addToken(Token.Type.RSQUIRLY);
                    break;
                case ';':
                    this.addToken(Token.Type.SEMICOLON);
                    break;
                case '.':
                    this.addToken(Token.Type.DOT);
                    break;
                case '*':
                    this.addToken(Token.Type.STAR);
                    break;
                case '\n':
                    this.addToken(Token.Type.NEWLINE);
                    break;
                case '\0':
                    this.addToken(Token.Type.EOF);
                    break;
                default:
                    String lexeme = null;
                    if (Character.isLetter(ch)) {
                        this.readWord();
                    } else if (Character.isDigit(ch)) {
                        this.readNumber();
                    } else {
                        error("Unexpected symbol: '+ch+'", this.line, this.column);
                    }
                    continue;
            }

            advance();
        }

        return this.tokens;
    }

    private void error(String message, int line, int column) {
        System.out.printf("ERROR %d:%d: %s\n", line, column, message);
        this.bad = true;
    }

    private boolean atEnd() {
        return this.position >= this.source.length();
    }

    private char advance() {
        if (atEnd()) {
            return '\0';
        }

        char ch = this.source.charAt(this.position);

        if (ch == '\n') {
            ++this.line;
            this.column = 0;
        }
        ++this.position;

        return ch;
    }

    private char peek() {
        if (atEnd()) {
            return '\0';
        }

        return this.source.charAt(this.position);
    }

    private void skipspaces() {
        while (Character.isWhitespace(peek()) && peek() != '\n') {
            advance();
            ++this.column;
        }
    }

    private void addToken(Token.Type type) {
        this.tokens.add(new Token(type, null, this.line, this.column));
        this.column += Token.length(type);
    }

    private void addToken(Token.Type type, String lexeme) {
        this.tokens.add(new Token(type, lexeme, this.line, this.column));
        this.column += lexeme.length();
    }

    private void readWord() {
        int start = this.position;
        while (Character.isLetter(peek())) {
            advance();
        }

        String word = this.source.substring(start, this.position).toUpperCase();
        if ("ROWS".equals(word)) {
            this.addToken(Token.Type.ROWS);
        } else if ("COLS".equals(word)) {
            this.addToken(Token.Type.COLS);
        } else if ("CHUNKS".equals(word)) {
            this.addToken(Token.Type.CHUNKS);
        } else if ("CHUNK".equals(word)) {
            this.addToken(Token.Type.CHUNK);
        } else if ("ROW".equals(word)) {
            this.addToken(Token.Type.ROW);
        } else if ("COL".equals(word)) {
            this.addToken(Token.Type.COL);
        } else {
            error("Unexpected word: '"+word+"'", this.line, this.column);
        }

    }

    private void readNumber() {
        int start = this.position;
        while (Character.isDigit(peek())) {
            advance();
        }
        String number = this.source.substring(start, this.position);
        this.addToken(Token.Type.NUMBER, number);
    }
}
