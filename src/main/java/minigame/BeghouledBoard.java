package minigame;

import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BeghouledBoard {

    public static final int ROWS = 5;
    public static final int COLUMNS = 9;
    public static final int MATCH = 3;
    public static final int SUN_PER_TILE = 10;
    public static final int TARGET_BASE = 500;
    public static final int TARGET_STEP = 350;
    public static final int MOVES_BASE = 20;
    public static final int MOVES_STEP = 4;

    public static final Plants[] PIECES = {
        Plants.SUNFLOWER, Plants.PEASHOOTER, Plants.WALL_NUT,
        Plants.SNOW_PEA, Plants.CHERRY_BOMB,
    };

    public static final class Cleared {
        private final int column;
        private final int row;
        private final Plants type;

        Cleared(int column, int row, Plants type) {
            this.column = column;
            this.row = row;
            this.type = type;
        }

        public int getColumn() {
            return column;
        }

        public int getRow() {
            return row;
        }

        public Plants getType() {
            return type;
        }
    }

    private final Plants[][] grid = new Plants[COLUMNS][ROWS];
    private final List<Cleared> lastCleared = new ArrayList<Cleared>();

    private final int target;

    private int sun;
    private int movesLeft;
    private int chain;

    public BeghouledBoard(int level) {
        int step = Math.max(0, level - 1);
        this.target = TARGET_BASE + TARGET_STEP * step;
        this.movesLeft = MOVES_BASE + MOVES_STEP * step;
        fill();
    }

    public int getTarget() {
        return target;
    }

    public int getSun() {
        return sun;
    }

    public int getMovesLeft() {
        return movesLeft;
    }

    public int getChain() {
        return chain;
    }

    public List<Cleared> getLastCleared() {
        return lastCleared;
    }

    public Plants at(int column, int row) {
        return inside(column, row) ? grid[column][row] : null;
    }

    public float progress() {
        return target <= 0 ? 0f : Math.min(1f, sun / (float) target);
    }

    public boolean isWon() {
        return sun >= target;
    }

    public boolean isLost() {
        return !isWon() && movesLeft <= 0;
    }

    public static boolean areNeighbours(int ca, int ra, int cb, int rb) {
        return Math.abs(ca - cb) + Math.abs(ra - rb) == 1;
    }

    public boolean wouldMatch(int ca, int ra, int cb, int rb) {
        if (!inside(ca, ra) || !inside(cb, rb) || !areNeighbours(ca, ra, cb, rb)) {
            return false;
        }
        swapCells(ca, ra, cb, rb);
        boolean any = !findMatches().isEmpty();
        swapCells(ca, ra, cb, rb);
        return any;
    }

    public boolean swap(int ca, int ra, int cb, int rb) {
        if (!wouldMatch(ca, ra, cb, rb) || movesLeft <= 0) {
            return false;
        }
        swapCells(ca, ra, cb, rb);
        movesLeft--;
        chain = 0;
        lastCleared.clear();
        resolve();
        return true;
    }

    public boolean hasAnyMove() {
        for (int c = 0; c < COLUMNS; c++) {
            for (int r = 0; r < ROWS; r++) {
                if (wouldMatch(c, r, c + 1, r) || wouldMatch(c, r, c, r + 1)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void shuffle() {
        do {
            fill();
        } while (!hasAnyMove());
    }

    private void resolve() {
        Set<Integer> matched = findMatches();
        while (!matched.isEmpty()) {
            chain++;
            for (Integer key : matched) {
                int column = key.intValue() / ROWS;
                int row = key.intValue() % ROWS;
                lastCleared.add(new Cleared(column, row, grid[column][row]));
                grid[column][row] = null;
            }
            sun += matched.size() * SUN_PER_TILE * chain;
            collapse();
            matched = findMatches();
        }
        if (!hasAnyMove()) {
            shuffle();
        }
    }

    private Set<Integer> findMatches() {
        Set<Integer> hit = new HashSet<Integer>();
        for (int r = 0; r < ROWS; r++) {
            scan(hit, 0, r, 1, 0, COLUMNS);
        }
        for (int c = 0; c < COLUMNS; c++) {
            scan(hit, c, 0, 0, 1, ROWS);
        }
        return hit;
    }

    private void scan(Set<Integer> hit, int startColumn, int startRow,
            int stepColumn, int stepRow, int length) {
        int run = 1;
        for (int i = 1; i <= length; i++) {
            int column = startColumn + stepColumn * i;
            int row = startRow + stepRow * i;
            Plants here = i < length ? grid[column][row] : null;
            Plants before = grid[column - stepColumn][row - stepRow];
            if (here != null && here == before) {
                run++;
                continue;
            }
            if (run >= MATCH && before != null) {
                for (int back = 0; back < run; back++) {
                    hit.add(Integer.valueOf(
                            (column - stepColumn * (back + 1)) * ROWS
                                    + (row - stepRow * (back + 1))));
                }
            }
            run = 1;
        }
    }

    private void collapse() {
        for (int c = 0; c < COLUMNS; c++) {
            int write = ROWS - 1;
            for (int r = ROWS - 1; r >= 0; r--) {
                if (grid[c][r] != null) {
                    grid[c][write--] = grid[c][r];
                }
            }
            while (write >= 0) {
                grid[c][write--] = random();
            }
        }
    }

    private void fill() {
        do {
            for (int c = 0; c < COLUMNS; c++) {
                for (int r = 0; r < ROWS; r++) {
                    grid[c][r] = random();
                }
            }
        } while (!findMatches().isEmpty());
    }

    private static Plants random() {
        return PIECES[PlantCombat.RANDOM.nextInt(PIECES.length)];
    }

    private void swapCells(int ca, int ra, int cb, int rb) {
        Plants held = grid[ca][ra];
        grid[ca][ra] = grid[cb][rb];
        grid[cb][rb] = held;
    }

    private static boolean inside(int column, int row) {
        return column >= 0 && column < COLUMNS && row >= 0 && row < ROWS;
    }
}
