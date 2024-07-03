import java.util.Arrays;

public final class Array2DWrapper<T> {
    private final Object[][] buffer;
    private final int rows;
    private final int cols;

    Array2DWrapper(T[][] buffer) {
        if (buffer == null) {
            this.buffer = null;
            this.rows = 0;
            this.cols = 0;
            return;
        }

        this.buffer = new Object[buffer.length][];
        for (int i = 0; i < buffer.length; ++i) {
            this.buffer[i] = Arrays.copyOf(buffer[i], buffer[i].length);
        }

        this.rows = buffer.length;
        this.cols = buffer.length > 0 ? buffer[0].length : 0;
    }

    public Array2DWrapper<T> copy() {
        return new Array2DWrapper<>((T[][])this.buffer);
    }

    public int getRows() {
        return this.rows;
    }

    public int getCols() {
        return this.cols;
    }

    public T get(int row, int col) {
        return (T) buffer[row][col];
    }

    public void set(int row, int col, T value) {
        buffer[row][col] = value;
    }

    public boolean inBounds(int row, int col) {
        return 0 <= row && row < this.rows && 0 <= col && col < this.cols;
    }
}