package view.gui;

public enum LawnLayer {

    TOMBSTONE(4),
    MOWER_PARKED(3),
    PLANT(2),
    ZOMBIE(1),
    SHOT(0),
    EFFECT(-1),
    SUN(-2);

    private static final float ROW_SPAN = 100f;
    private static final float IN_FRONT = -1e9f;

    private final int rank;

    LawnLayer(int rank) {
        this.rank = rank;
    }

    public float depth(float rowFeet) {
        return this == SUN ? IN_FRONT : rowFeet * ROW_SPAN + rank;
    }
}
