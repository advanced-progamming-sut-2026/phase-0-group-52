package model;

import model.entities.Cell;
import model.entities.CellType;
import model.entities.Lawnmower;

import java.util.ArrayList;
import java.util.List;

/**
 * زمین بازی: شبکه ۵×۹ (مگر تنظیمات مرحله طور دیگری بگوید) + یک ماشین چمن‌زنی در ابتدای هر ردیف.
 * مختصات: (x = col, y = row). ردیف‌ها از بالا (۰) به پایین (۴).
 */
public class GameField {
    public static final int ROWS = 5;
    public static final int COLS = 9;

    private final Cell[][] grid;            // [row][col]
    private final Lawnmower[] lawnmowers;   // یکی برای هر ردیف
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

        // ---- چیدن زمین‌های مخصوص فصل‌ها ----
        applyChapterTerrain();
    }

    /**
     * اعمال زمین‌های مخصوص هر فصل.
     * برای ساحل: آب + ساحل‌های پست (نقاط ظهور زامبی از زیر).
     */
    private void applyChapterTerrain() {
        if (chapter == ChapterType.BIG_WAVE_BEACH) {
            int rows = getRows();
            int cols = getCols();

            // آب: ۳ ستون سمت راست
            for (int r = 0; r < rows; r++) {
                for (int c = cols - 3; c < cols; c++) {
                    grid[r][c].setType(CellType.WATER);
                }
            }

            // ساحل پست: نقاط ظهور زامبی از زیر (پیش‌فرض؛ قابل جایگزینی با setCellType توسط مرحله)
            grid[1][cols - 3].setType(CellType.LOW_GROUND);
            grid[3][cols - 3].setType(CellType.LOW_GROUND);
        }
    }

    /** آیا (x=col, y=row) داخل محدوده زمین است؟ */
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

    public Cell[][] getGrid() { return grid; }
    public Lawnmower[] getLawnmowers() { return lawnmowers; }
    public ChapterType getChapter() { return chapter; }
    public int getRows() { return grid.length; }
    public int getCols() { return grid[0].length; }

    /** تغییرِ نوعِ یک خانه (برای چیدنِ سنگ‌قبر توسط مرحله). */
    public void setCellType(int col, int row, CellType type) {
        Cell c = getCell(col, row);
        if (c != null) c.setType(type);
    }

    /** اولین خانه‌ی مانع (سنگ‌قبر) بین دو ستون در یک ردیف — برای گیاهانِ شلیک‌کننده. */
    public Cell firstObstacle(int row, int fromCol, int toCol) {
        int step = fromCol <= toCol ? 1 : -1;
        for (int c = fromCol; c != toCol + step; c += step) {
            Cell cell = getCell(c, row);
            if (cell != null && cell.isObstacle()) return cell;
        }
        return null;
    }

    /**
     * خانه‌های ساحلِ پست (نقاطِ ظهورِ زامبی از زیر) به‌صورت {col, row}.
     */
    public List<int[]> getLowGroundCells() {
        List<int[]> list = new ArrayList<>();
        for (int r = 0; r < getRows(); r++) {
            for (int c = 0; c < getCols(); c++) {
                if (grid[r][c].getType() == CellType.LOW_GROUND) {
                    list.add(new int[]{c, r});
                }
            }
        }
        return list;
    }
}
