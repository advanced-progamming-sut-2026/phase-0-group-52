package view.gui;

public enum LawnLayer {

    TOMBSTONE(4),
    MOWER_PARKED(3),
    GROUND(2),
    SUN(0);

    public static final float SPAN = 10000f;

    private final int rank;

    LawnLayer(int rank) {
        this.rank = rank;
    }

    public float depth(float rowFeet) {
        return rank * SPAN + rowFeet;
    }
}
