package gameoflife;

import java.util.Arrays;

public class GameOfLifeModel {
    public static class State {
        public byte[][] field;
        public int rows;
        public int cols;

        public State(byte[][] _field, int _rows, int _cols) {
            field = _field;
            rows = _rows;
            cols = _cols;
        }

        public State copy() {
            return new State(field, rows, cols);
        }
    }


    public GameOfLifeModel(byte[][] buffer) {
        mField     = new Field(buffer);
        mBackField = new Field(buffer);

        mState = new State(mBackField.getBufferRef(), mField.getRows(), mField.getCols());
    }

    public State getState() {
        return mState.copy();
    }

    public void nextStep() {
        for (int i = 0; i < mField.getRows(); ++i) {
            for (int j = 0; j < mField.getCols(); ++j) {
                byte cell = mField.get(i, j);
                int neighbours = countNeighbours(i, j);
                
                if (cell > 0) {
                    if (neighbours <= 1 || neighbours >= 4) {
                        mBackField.set(i, j, (byte)0);
                    }
                } else {
                    if (neighbours == 3) {
                        mBackField.set(i, j, (byte)1);
                    }
                }
            }
        }

        mField = mBackField.copy();
    }

    private int countNeighbours(int i, int j) {
        if (!mField.inBounds(i, j)) {
            return 0;
        }

        int count = 0;
        for (int di = -1; di <= 1; ++di) {
            for (int dj = -1; dj <= 1; ++dj) {
                if (di == 0 && dj == 0) {
                    continue;
                }

                int nrows = mField.getRows();
                int ncols = mField.getCols();

                int row = ((i + di) % nrows + nrows) % nrows;
                int col = ((j + dj) % ncols + ncols) % ncols;

                byte cell = mField.get(row, col);

                if (cell > 0) {
                    ++count;
                }
            }
        }

        return count;
    }
    
    private class Field {
        public Field(byte[][] buffer) {
            mBuffer = new byte[buffer.length][];
            for (int i = 0; i < buffer.length; ++i) {
                mBuffer[i] = Arrays.copyOf(buffer[i], buffer[i].length);
            }

            mRows = buffer.length;
            mCols = buffer.length > 0 ? buffer[0].length : 0;
        }

        public Field copy() {
            return new Field(mBuffer);
        }

        public void set(int i, int j, byte value) {
            mBuffer[i][j] = value;
        }

        public byte get(int i, int j) {
            return mBuffer[i][j];
        }

        public byte[][] getBufferRef() {
            return mBuffer;
        }

        public int getRows() {
            return mRows;
        }

        public int getCols() {
            return mCols;
        }

        public boolean inBounds(int i, int j) {
            return 0 <= i && i < mRows && 0 <= j && j < mCols;
        }

        public byte[][] mBuffer;
        int mRows;
        int mCols;
    }

    private Field mField;
    private Field mBackField;
    private State mState;
}
