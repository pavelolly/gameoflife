package gameoflife;

import java.io.IOException;

public class GameOfLife {
    public static void main(String[] args) throws IOException, InterruptedException {
        byte[][] buffer = {{0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 1, 1, 1, 0}, {0, 0, 0, 0, 0}};
        GameOfLifeModel game = new GameOfLifeModel(buffer);

        while (true) {
            GameOfLifeModel.State state = game.getState();

            for (int i = 0; i < state.rows; ++i) {
                for (int j = 0; j < state.cols; ++j) {
                    System.out.print((state.field[i][j] > 0 ? "*" : ".") + " ");
                }
                System.out.println();
            }

            Thread.sleep(1000);
            game.nextStep();

            System.out.print("\u001b["+(game.getState().rows)+"F");
        }
    }
}
