import java.util.ArrayList;
import java.util.List;

public class Board {
    private final static int DEFAULT_BOARD_SIZE = 4;
    private final static int FIRST_CELL_VALUE = 2;
    private final static int goal = 2048;
    private int[][] cells;
    private int maxValue;
    private boolean moved = false;
    private boolean merged = false;

    private final List<CellCreatedListener> cellCreatedListeners = new ArrayList<>();
    private final List<CellMovedListener> cellMovedListeners = new ArrayList<>();
    private final List<CellMergedFromListener> cellMergedFromListeners = new ArrayList<>();
    private final List<CellMergedToListener> cellMergedToListeners = new ArrayList<>();
    private final List<GameOverListener> gameOverListeners = new ArrayList<>();
    private final List<WinListener> winListeners = new ArrayList<>();

    static class Point {
        private final int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    interface CellCreatedListener {
        void cellCreated(int x, int y, int value);
    }

    interface CellMovedListener {
        void cellMoved(int fromX, int fromY, int toX, int toY);
    }

    interface CellMergedFromListener {
        void cellMergedFrom(int fromX, int fromY, int toX, int toY, int mergedValue);
    }

    interface CellMergedToListener {
        void cellMergedTo(int toX, int toY, int fromX, int fromY);
    }

    interface GameOverListener {
        void gameOver();
    }

    interface WinListener {
        void win();
    }


    public Board() {
        this(DEFAULT_BOARD_SIZE);
    }

    public Board(int size) {
        this(new int[size][size]);
        createRandomCells(2);
    }

    public Board(int[][] cells) {
        assert cells.length > 0 && cells.length == cells[0].length;

        maxValue = cells[0][0];
        for (int y = 0; y < cells.length; y++) {
            for (int x = 0; x < cells.length; x++) {
                if (cells[y][x] > maxValue) {
                    maxValue = cells[y][x];
                }
            }
        }

        this.cells = cells;
    }

    public int getSize() {
        return cells.length;
    }

    public int getValue(int x, int y) {
        return cells[y][x];
    }

    public int getGoal() {
        return goal;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setNumber(int x, int y, int value) {
        cells[y][x] = value;
    }

    public void addCellCreatedListener(CellCreatedListener listener) {
        cellCreatedListeners.add(listener);
    }

    public void addCellMovedListener(CellMovedListener listener) {
        cellMovedListeners.add(listener);
    }

    public void addCellMergedFromListener(CellMergedFromListener listener) {
        cellMergedFromListeners.add(listener);
    }

    public void addCellMergedToListener(CellMergedToListener listener) {
        cellMergedToListeners.add(listener);
    }

    public void addGameOverListener(GameOverListener listener) {
        gameOverListeners.add(listener);
    }

    public void addWinListener(WinListener listener) {
        winListeners.add(listener);
    }

    public void removeCellCreatedListener(CellCreatedListener listener) {
        cellCreatedListeners.remove(listener);
    }

    public void removeCellMovedListener(CellMovedListener listener) {
        cellMovedListeners.remove(listener);
    }

    public void removeCellMergedFromListener(CellMergedFromListener listener) {
        cellMergedFromListeners.remove(listener);
    }

    public void removeCellMergedToListener(CellMergedToListener listener) {
        cellMergedToListeners.remove(listener);
    }

    public void removeGameOverListener(GameOverListener listener) {
        gameOverListeners.remove(listener);
    }

    public void removeWinListener(WinListener listener) {
        winListeners.remove(listener);
    }

    public void copyTo(Board board) {
        for(int y = 0; y < getSize(); y++) {
            for (int x = 0; x < getSize(); x++) {
                int value = getValue(x, y);
                board.setNumber(x, y, value);
            }
        }
    }

    public void moveUp() {
        moveUp(1);
    }

    public void moveUp(int cellsToGenerate) {
        for (int x = 0; x < getSize(); x++) {
            mergeCellsOnCol(x, -1);
            moveCellsOnCol(x, -1);
        }
        if (cellsToGenerate > 1) {
            createRandomCells(cellsToGenerate);
        } else if (cellsToGenerate == 1 && (moved || merged)) {
            createRandomCells(cellsToGenerate);
            moved = false;
            merged = false;
        }
    }

    public void moveDown() {
        moveDown(1);
    }

    public void moveDown(int cellsToGenerate) {
        for (int x = 0; x < getSize(); x++) {
            mergeCellsOnCol(x, 1);
            moveCellsOnCol(x, 1);
        }
        if (cellsToGenerate > 1) {
            createRandomCells(cellsToGenerate);
        } else if (cellsToGenerate == 1 && (moved || merged)) {
            createRandomCells(cellsToGenerate);
            moved = false;
            merged = false;
        }
    }


    public void moveLeft() {
        moveLeft(1);
    }

    public void moveLeft(int cellsToGenerate) {
        for (int y = 0; y < getSize(); y++) {
            mergeCellsOnRow(y, -1);
            moveCellsOnRow(y, -1);
        }
        if (cellsToGenerate > 1) {
            createRandomCells(cellsToGenerate);
        } else if (cellsToGenerate == 1 && (moved || merged)) {
            createRandomCells(cellsToGenerate);
            moved = false;
            merged = false;
        }
    }

    public void moveRight() {
        moveRight(1);
    }

    public void moveRight(int cellsToGenerate) {
        for (int y = 0; y < getSize(); y++) {
            mergeCellsOnRow(y, 1);
            moveCellsOnRow(y, 1);
        }
        if (cellsToGenerate > 1) {
            createRandomCells(cellsToGenerate);
        } else if (cellsToGenerate == 1 && (moved || merged)) {
            createRandomCells(cellsToGenerate);
            moved = false;
            merged = false;
        }
    }

    private void mergeCellsOnCol(int x, int dy) {
        int mergeTarget = -1;
        if (dy < 0) {
            for (int y = 0; y < getSize(); y++) {
                mergeTarget = performMergeOnRow(x, y, mergeTarget);
            }
        } else {
            for (int y = getSize() - 1; y >= 0; y--) {
                mergeTarget = performMergeOnRow(x, y, mergeTarget);
            }
        }
    }

    private void mergeCellsOnRow(int y, int dx) {
        int mergeTarget = -1;
        if (dx < 0) {
            for (int x = 0; x < getSize(); x++) {
                mergeTarget = performMergeOnCol(x, y, mergeTarget);
            }
        } else {
            for (int x = getSize() - 1; x >= 0; x--) {
                mergeTarget = performMergeOnCol(x, y, mergeTarget);
            }
        }
    }

    private int performMergeOnCol(int x, int y, int mergeTarget) {
        int num = getValue(x, y);
        if (num != 0) {
            if (mergeTarget == -1 || getValue(mergeTarget, y) != num) {
                mergeTarget = x;
            } else {
                int mergedValue = getValue(mergeTarget, y) + num;
                setNumber(mergeTarget, y, mergedValue);
                setNumber(x, y, 0);
                if (mergedValue > maxValue) {
                    maxValue = mergedValue;
                    if (maxValue == getGoal()) {
                        for (var listener : winListeners) {
                            listener.win();
                        }
                    }
                }
                merged = true;
                for (var listener : cellMergedFromListeners) {
                    listener.cellMergedFrom(x, y, mergeTarget, y, mergedValue);
                }
                for (var listener : cellMergedToListeners) {
                    listener.cellMergedTo(mergeTarget, y, x, y);
                }
                mergeTarget = -1;
            }
        }
        return mergeTarget;
    }

    private int performMergeOnRow(int x, int y, int mergeTarget) {
        int num = getValue(x, y);
        if (num != 0) {
            if (mergeTarget == -1 || getValue(x, mergeTarget) != num) {
                mergeTarget = y;
            } else {
                int mergedValue = getValue(x, mergeTarget) + num;
                setNumber(x, mergeTarget, mergedValue);
                setNumber(x, y, 0);
                if (mergedValue > maxValue) {
                    maxValue = mergedValue;
                    if (maxValue == getGoal()) {
                        for (var listener : winListeners) {
                            listener.win();
                        }
                    }
                }
                merged = true;
                for (var listener : cellMergedFromListeners) {
                    listener.cellMergedFrom(x, y, x, mergeTarget, mergedValue);
                }

                for (var listener : cellMergedToListeners) {
                    listener.cellMergedTo(x, mergeTarget, x, y);
                }

                mergeTarget = -1;
            }
        }
        return mergeTarget;
    }

    private void moveCellsOnCol(int x, int dy) {
        if (dy < 0) {
            for (int y = 0; y < getSize(); y++) {
                moveCell(x, y, 0, dy);
            }
        } else {
            for (int y = getSize() - 1; y >= 0; y--) {
                moveCell(x, y, 0, dy);
            }
        }
    }

    private void moveCellsOnRow(int y, int dx) {
        if (dx < 0) {
            for (int x = 0; x < getSize(); x++) {
                moveCell(x, y, dx, 0);
            }
        } else {
            for (int x = getSize() - 1; x >= 0; x--) {
                moveCell(x, y, dx, 0);
            }
        }
    }

    private void moveCell(int x, int y, int dx, int dy) {
        if (cells[y][x] == 0) return;

        int fromX = x;
        int fromY = y;

        int nextX = x + dx;
        int nextY = y + dy;
        while (areCoordsInside(nextX, nextY) && getValue(nextX, nextY) == 0) {
            setNumber(nextX, nextY, getValue(x, y));
            setNumber(x, y, 0);
            x = nextX;
            y = nextY;
            nextX += dx;
            nextY += dy;
        }
        if (fromX != x || fromY != y) {
            for (var listener : cellMovedListeners) {
                listener.cellMoved(fromX, fromY, x, y);
            }
            moved = true;
        }
    }

    private boolean areCoordsInside(int x, int y) {
        return x >= 0 && x < getSize() && y >= 0 && y < getSize();
    }

    private boolean hasMoreMoves() {
        for (int y = 0; y < getSize(); y++) {
            for (int x = 0; x < getSize(); x++) {
                int num = getValue(x, y);
                if (num == 0 ||
                        areCoordsInside(x, y - 1) && getValue(x, y - 1) == num ||
                        areCoordsInside(x, y + 1) && getValue(x, y + 1) == num ||
                        areCoordsInside(x - 1, y) && getValue(x - 1, y) == num ||
                        areCoordsInside(x + 1, y) && getValue(x + 1, y) == num) {
                    return true;
                }
            }
        }
        return false;
    }

    private void createRandomCells(int count) {
        ArrayList<Point> unoccupiedCells = new ArrayList<>();
        for (int y = 0; y < getSize(); y++) {
            for (int x = 0; x < getSize(); x++) {
                if (getValue(x, y) == 0) {
                    unoccupiedCells.add(new Point(x, y));
                }
            }
        }

        for (int i = 0; i < count && unoccupiedCells.size() > 0; i++) {
            int randomIndex = (int) (Math.random() * unoccupiedCells.size());
            Point point = unoccupiedCells.get(randomIndex);
            setNumber(point.x, point.y, FIRST_CELL_VALUE);
            if (FIRST_CELL_VALUE > maxValue) {
                maxValue = FIRST_CELL_VALUE;
            }
            for (var listener : cellCreatedListeners) {
                listener.cellCreated(point.x, point.y, FIRST_CELL_VALUE);
            }
            unoccupiedCells.remove(randomIndex);
        }

        if (unoccupiedCells.size() == 0) {
            if (!hasMoreMoves()) {
                for (var listener : gameOverListeners) {
                    listener.gameOver();
                }
            }
        }

    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        String format = "%" + String.valueOf(getMaxValue()).length() + "s ";
        for (int y = 0; y < getSize(); y++) {
            for (int x = 0; x < getSize(); x++) {
                int value = getValue(x, y);
                if (value == 0) {
                    s.append(String.format(format, "-"));
                } else {
                    s.append(String.format(format, value));
                }
            }
            s.append('\n');
        }
        return s.toString();
    }

    public String toStringWithoutFormatting() {
        var s = new StringBuilder();
        for (int y = 0; y < getSize(); y++) {
            for (int x = 0; x < getSize(); x++) {
                s.append(getValue(x, y));
                if (x < getSize() - 1) {
                    s.append(' ');
                }
            }
            s.append('\n');
        }
        return s.toString();
    }
}
