public class GameOfLife {
    public static void main(String[] args) {
        Byte[][] buffer = {{0, 0, 0, 0, 0},
                           {0, 0, 1, 0, 0},
                           {0, 0, 0, 1, 0},
                           {0, 1, 1, 1, 0},
                           {0, 0, 0, 0, 0}};

        new GameOfLifeFrame(new GameOfLifeModel(buffer));
    }
}
