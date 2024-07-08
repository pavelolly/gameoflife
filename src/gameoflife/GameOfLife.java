package gameoflife;

import parser.GameOfLifeConfigLexer;
import parser.GameOfLifeConfigParser;
import parser.Token;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;

public class GameOfLife {
    public static void main(String[] args) throws IOException {
        var tokens = new GameOfLifeConfigLexer(Paths.get("config-example.txt"), StandardCharsets.UTF_8).tokenize();
        for (Token token : tokens) {
            System.out.println(token);
        }
        var state = new GameOfLifeConfigParser(tokens).parse();

        if (state != null) {
            System.out.println();

            for (int i = 0; i < state.field.getRows(); ++i) {
                for (int j = 0; j < state.field.getCols(); ++j) {
                    System.out.print((state.field.get(i, j) > 0 ? '*' : '.') + " ");
                }
                System.out.println();
            }
        }

        if (state == null) {
            System.err.println("Failed to parse config");
            Byte[][] buffer = new Byte[10][10];
            Arrays.setAll(buffer, idx -> {
                Arrays.setAll(buffer[idx], _ -> (byte) 0);
                return buffer[idx];
            });
            new GameOfLifeFrame(new GameOfLifeModel(buffer));
        } else {
            new GameOfLifeFrame(new GameOfLifeModel(state));
        }

    }
}
