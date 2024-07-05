package gameoflife;

import utli.Array2DWrapper;

public class GameOfLifeModel {
    public GameOfLifeModel(Byte[][] buffer) {
        this.field     = new Array2DWrapper<>(buffer);
        this.backField = new Array2DWrapper<>(buffer);
    }

    public GameOfLifeModel(State state) {
        this.field     = state.field.copy();
        this.backField = state.field.copy();
    }

    public State getState() {
        return new State(field);
    }

    public void clearField() {
        for (int i = 0; i < this.field.getRows(); ++i) {
            for (int j = 0; j < this.field.getCols(); ++j) {
                this.field.set(i, j, (byte) 0);
            }
        }
    }

    public void toggleCell(int row, int col) {
        byte value = (byte)(this.field.get(row, col) > 0 ? 0 : 1);
        this.field.set(row, col, value);
    }

    public void setState(State state) {
        this.field = state.field.copy();
    }

    public void nextStep() {
        for (int i = 0; i < this.field.getRows(); ++i) {
            for (int j = 0; j < this.field.getCols(); ++j) {
                byte cell = this.field.get(i, j);
                int neighbours = this.countNeighbours(i, j);
                
                if (cell > 0) {
                    if (neighbours <= 1 || neighbours >= 4) {
                        this.backField.set(i, j, (byte)0);
                    } else {
                        this.backField.set(i, j, (byte)1);
                    }
                } else {
                    if (neighbours == 3) {
                        this.backField.set(i, j, (byte)1);
                    } else {
                        this.backField.set(i, j, (byte)0);
                    }
                }
            }
        }

        this.field = this.backField.copy();
    }

    private int countNeighbours(int row, int col) {
        if (!field.inBounds(row, col)) {
            return 0;
        }

        int count = 0;
        for (int drow = -1; drow <= 1; ++drow) {
            for (int dcol = -1; dcol <= 1; ++dcol) {
                if (drow == 0 && dcol == 0) {
                    continue;
                }

                int nrows = field.getRows();
                int ncols = field.getCols();

                int rrow = ((row + drow) % nrows + nrows) % nrows;
                int rcol = ((col + dcol) % ncols + ncols) % ncols;

                byte cell = field.get(rrow, rcol);

                if (cell > 0) {
                    ++count;
                }
            }
        }

        return count;
    }

    private Array2DWrapper<Byte> field;
    private Array2DWrapper<Byte> backField;

    public static class State {
        public Array2DWrapper<Byte> field;

        public State(Byte[][] field) {
            this.field = new Array2DWrapper<>(field);
        }

        public State(Array2DWrapper<Byte> field) {
            this.field = field;
        }

        public State copy() {
            return new State(field.copy());
        }

    }
}
