package gameoflife;

import parser.GameOfLifeConfigLexer;
import parser.GameOfLifeConfigParser;
import parser.Token;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class GameOfLife {
    public static void main(String[] args) throws IOException {
        GameOfLifeConfigLexer lexer = new GameOfLifeConfigLexer(Paths.get("config-example.txt"), StandardCharsets.UTF_8);
        var tokens = lexer.tokenize();

        for (Token token : tokens) {
            System.out.println(token);
        }

        GameOfLifeConfigParser parse = new GameOfLifeConfigParser(tokens);
        GameOfLifeModel.State state = parse.parse();

        for (int i = 0; i < state.field.getRows(); ++i) {
            for (int j = 0; j < state.field.getCols(); ++j) {
                System.out.print(state.field.get(i, j)+" ");
            }
            System.out.println();
        }

//        Byte[][] buffer = {{0, 0, 0, 0, 0},
//                           {0, 0, 1, 0, 0},
//                           {0, 0, 0, 1, 0},
//                           {0, 1, 1, 1, 0},
//                           {0, 0, 0, 0, 0}};
//
//        new GameOfLifeFrame(new GameOfLifeModel(buffer));

    }
}
