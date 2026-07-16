package model;

import model.entities.Cell;
import model.entities.CellType;
import model.entities.Lawnmower;

public class GameField {
    public static final int ROWS = 5;
    public static final int COLS = 9;

    private final Cell[][] grid;
    private final Lawnmower[] lawnmowers;
    private final ChapterType chapter;

    public GameField(ChapterType chapter) {
        this(chapter, ROWS, COLS);
    }

    public GameField(ChapterType chapter, int rows, int cols) {
        this.chapter = chapter;
        this.grid = new Cell[rows][cols];
        this.lawnmowers = new Lawnmower[rows];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell(CellType.NORMAL, r, c);
            }
            lawnmowers[r] = new Lawnmower(r, true);
        }
    }

    public boolean inBounds(int col, int row) {
        return row >= 0 && row < grid.length && col >= 0 && col < grid[0].length;
    }

    public Cell getCell(int col, int row) {
        if (!inBounds(col, row)) return null;
        return grid[row][col];
    }

    public Lawnmower getLawnmower(int row) {
        if (row < 0 || row >= lawnmowers.length) return null;
        return lawnmowers[row];
    }

    public java.util.List<int[]> getLowGroundCells() {
        java.util.List<int[]> result = new java.util.ArrayList<int[]>();
        for (int r = 0; r < getRows(); r++) {
            for (int c = 0; c < getCols(); c++) {
                Cell cell = getCell(c, r);
                if (cell != null && cell.getType() == CellType.LOW_GROUND)
                    result.add(new int[]{c, r});
            }
        }
        return result;
    }

    public Cell[][] getGrid() { return grid; }
    public Lawnmower[] getLawnmowers() { return lawnmowers; }
    public ChapterType getChapter() { return chapter; }
    public int getRows() { return grid.length; }
    public int getCols() { return grid[0].length; }
}
