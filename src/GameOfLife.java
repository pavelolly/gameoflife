public class GameOfLife {
    public static void main(String[] args) {
        byte[][] buffer = {{0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 1, 1, 1, 0}, {0, 0, 0, 0, 0}};
        GameOfLifeModel game = new GameOfLifeModel(buffer);
    
        new GameOfLifeFrame(game);
    }
}
