package model.entities;

public class BowlingNut {

    public static final double SPEED = 0.42d;
    public static final double DRIFT = 0.34d;
    public static final double HIT_RANGE = 0.5d;
    public static final double BLAST_RANGE = 1.2d;
    public static final int MAX_BOUNCES = 6;

    private final boolean explosive;
    private final boolean heavy;

    private double column;
    private double lane;
    private int row;
    private int drift;
    private int bounces;
    private boolean spent;

    public BowlingNut(int row, double column, boolean explosive, boolean heavy) {
        this.row = row;
        this.lane = row;
        this.column = column;
        this.explosive = explosive;
        this.heavy = heavy;
    }

    public boolean isHeavy() {
        return heavy;
    }

    public boolean isExplosive() {
        return explosive;
    }

    public double getColumn() {
        return column;
    }

    public double getLane() {
        return lane;
    }

    public int getRow() {
        return row;
    }

    public int getDrift() {
        return drift;
    }

    public int getBounces() {
        return bounces;
    }

    public boolean isSpent() {
        return spent;
    }

    public void spend() {
        spent = true;
    }

    public void bounce(int rows) {
        if (heavy) {
            return;
        }
        bounces++;
        if (bounces >= MAX_BOUNCES) {
            spent = true;
            return;
        }
        if (drift == 0) {
            drift = row <= 0 ? 1 : row >= rows - 1 ? -1 : (bounces % 2 == 0 ? 1 : -1);
            return;
        }
        drift = -drift;
    }

    public void advance(int rows, int columns) {
        column += SPEED;
        if (drift != 0) {
            lane += DRIFT * drift;
            if (lane <= 0d) {
                lane = 0d;
                drift = 1;
            } else if (lane >= rows - 1) {
                lane = rows - 1;
                drift = -1;
            }
            row = (int) Math.round(lane);
        }
        if (column > columns + 1) {
            spent = true;
        }
    }
}
