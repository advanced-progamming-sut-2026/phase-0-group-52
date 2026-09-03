package model.entities.plants;

public final class Muzzle {

    public static final String MAIN = "main";

    private final String name;
    private final int rowOffset;
    private final int direction;
    private final double frame;

    public Muzzle(String name, int rowOffset, int direction) {
        this(name, rowOffset, direction, 0d);
    }

    public Muzzle(String name, int rowOffset, int direction, double frame) {
        this.name = name;
        this.rowOffset = rowOffset;
        this.direction = direction;
        this.frame = frame;
    }

    public static Muzzle forward() {
        return new Muzzle(MAIN, 0, 1);
    }

    public String getName() {
        return name;
    }

    public int getRowOffset() {
        return rowOffset;
    }

    public int getDirection() {
        return direction;
    }

    public double getFrame() {
        return frame;
    }

    public double frameIn(Plants plant, String state) {
        return MuzzleTiming.frameOf(plant, name, state, frame);
    }
}
