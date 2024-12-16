package gameoflife;

import parser.GameOfLifeConfigLexer;
import parser.GameOfLifeConfigParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;

public class GameOfLife {
    public static void main(String[] args) throws IOException {
        var tokens = new GameOfLifeConfigLexer(Paths.get("config-example.txt"), StandardCharsets.UTF_8).tokenize();
        var state = new GameOfLifeConfigParser(tokens).parse();

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
