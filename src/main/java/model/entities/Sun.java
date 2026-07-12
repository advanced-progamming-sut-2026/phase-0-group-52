package model.entities;

import model.Game;
import model.Vec2;

/**
 * یک خورشید روی زمین یا در حال سقوط.
 * - خورشیدِ گیاه‌زا: روی گیاه می‌ماند تا برداشت شود (falling=false، fromSky=false).
 * - خورشیدِ آسمانی: ۵۰ تیک (۵ ثانیه) طول می‌کشد تا فرود بیاید (falling=true)، سپس روی زمین قابل برداشت است.
 */
public class Sun {
    public static final int FALL_TICKS = 5 * Game.TICKS_PER_SECOND;  // ۵ ثانیه × ۱۰ = ۵۰ تیک

    private int amount;
    private SunType type;          // فقط برای خورشید آسمانی معنا دارد
    private Vec2 position;          // (x=col, y=row)
    private boolean falling;
    private int fallTicksRemaining;
    private boolean fromSky;

    /** خورشیدِ تولیدشده توسط یک گیاه (مثلا Sunflower). */
    public Sun(int amount, Vec2 position) {
        this.amount = amount;
        this.position = position;
        this.type = null;
        this.falling = false;
        this.fromSky = false;
        this.fallTicksRemaining = 0;
    }

    /** خورشیدِ سقوط‌کننده از آسمان روی خانه‌ی (col,row). */
    public Sun(SunType type, Vec2 position) {
        this.type = type;
        this.amount = type.getAmount();
        this.position = position;
        this.falling = true;
        this.fromSky = true;
        this.fallTicksRemaining = FALL_TICKS;
    }

    /** یک تیک از سقوط می‌گذرد؛ وقتی به صفر برسد، روی زمین می‌نشیند. */
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
