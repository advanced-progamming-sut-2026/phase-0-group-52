package view.gui;

public final class LawnGeometry {

    public static final float NATIVE_HEIGHT = 768f;
    public static final float SCALE = Theme.WORLD_HEIGHT / NATIVE_HEIGHT;

    public static final int ROWS = 5;
    public static final int COLUMNS = 9;

    private static final float DEFAULT_X = 475f;
    private static final float DEFAULT_Y = 62f;
    private static final float DEFAULT_WIDTH = 735f;
    private static final float DEFAULT_HEIGHT = 486f;

    private static final float MUSTER_X = 1500f;
    private static final float MUSTER_STEP = 108f;

    private static float areaX = DEFAULT_X;
    private static float areaY = DEFAULT_Y;
    private static float areaWidth = DEFAULT_WIDTH;
    private static float areaHeight = DEFAULT_HEIGHT;

    private LawnGeometry() {}

    public static float scaled(float nativePixels) {
        return nativePixels * SCALE;
    }

    public static void setPlayArea(float x, float y, float width, float height) {
        if (width <= 1f || height <= 1f) {
            return;
        }
        areaX = x;
        areaY = y;
        areaWidth = width;
        areaHeight = height;
    }

    public static float areaX() {
        return areaX;
    }

    public static float areaY() {
        return areaY;
    }

    public static float areaWidth() {
        return areaWidth;
    }

    public static float areaHeight() {
        return areaHeight;
    }

    public static float defaultX() {
        return DEFAULT_X;
    }

    public static float defaultY() {
        return DEFAULT_Y;
    }

    public static float defaultWidth() {
        return DEFAULT_WIDTH;
    }

    public static float defaultHeight() {
        return DEFAULT_HEIGHT;
    }

    public static float cellWidth() {
        return areaWidth / COLUMNS;
    }

    public static float cellHeight() {
        return areaHeight / ROWS;
    }

    public static float columnLeft(int column) {
        return areaX + column * cellWidth();
    }

    public static float columnX(int column) {
        return columnLeft(column) + cellWidth() / 2f;
    }

    public static float rowFeet(int row) {
        return areaY + (ROWS - 1 - row) * cellHeight();
    }

    public static float rowMiddle(int row) {
        return rowFeet(row) + cellHeight() / 2f;
    }

    public static int columnAt(float x) {
        int column = (int) Math.floor((x - areaX) / cellWidth());
        return column < 0 || column >= COLUMNS ? -1 : column;
    }

    public static int rowAt(float y) {
        int fromBottom = (int) Math.floor((y - areaY) / cellHeight());
        if (fromBottom < 0 || fromBottom >= ROWS) {
            return -1;
        }
        return ROWS - 1 - fromBottom;
    }

    public static int rowOf(int index) {
        return Math.floorMod(index * 2 + 1, ROWS);
    }

    public static float musterX(int index) {
        return scaled(MUSTER_X + index * MUSTER_STEP);
    }
}
