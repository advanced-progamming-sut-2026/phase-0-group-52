package model.entities;

import model.Vec2;

public class Sun {
    public static final int FALL_TICKS = 5;

    private int amount;
    private SunType type;
    private Vec2 position;
    private boolean falling;
    private int fallTicksRemaining;
    private boolean fromSky;

    public Sun(int amount, Vec2 position) {
        this.amount = amount;
        this.position = position;
        this.type = null;
        this.falling = false;
        this.fromSky = false;
        this.fallTicksRemaining = 0;
    }

    public Sun(SunType type, Vec2 position) {
        this.type = type;
        this.amount = type.getAmount();
        this.position = position;
        this.falling = true;
        this.fromSky = true;
        this.fallTicksRemaining = FALL_TICKS;
    }

    public void tickFall() {
        if (falling) {
            fallTicksRemaining--;
            if (fallTicksRemaining <= 0) {
                falling = false;
            }
        }
    }

    public int getCol() { return (int) position.x; }
    public int getRow() { return (int) position.y; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public SunType getType() { return type; }
    public Vec2 getPosition() { return position; }
    public void setPosition(Vec2 position) { this.position = position; }

    public boolean isFalling() { return falling; }
    public int getFallTicksRemaining() { return fallTicksRemaining; }
    public boolean isFromSky() { return fromSky; }
}
