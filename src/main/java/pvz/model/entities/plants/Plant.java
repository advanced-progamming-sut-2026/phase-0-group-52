package model.entities.plants;


import model.Game;
import model.Vec2;

public abstract class Plant implements PlantInterface {
    private Plants type;
    private double hp;
    private int price;
    private Vec2 position;
    private double attackdamage;

    /** شمارنده داخلی تیک‌ها برای عمل بعدی (شلیک/تولید خورشید/...). */
    protected double actionTimer = 0;
    /** آیا این گیاه boost شده (اثر غذای گیاه موقع کاشت). */
    protected boolean boosted = false;

    public Plant(Plants type, double hp, int price, Vec2 position, double attackdamage) {
        this.type = type;
        this.hp = hp;
        this.price = price;
        this.position = position;
        this.attackdamage = attackdamage;
    }

    // ---------- قلاب‌های polymorphic که موتور (GameEngine) صدا می‌زند ----------

    /** هر تیک یک‌بار صدا زده می‌شود. هر زیرکلاس رفتار خودش را اینجا پیاده می‌کند. */
    public void onTick(Game game) {}

    /** لحظه‌ی کاشته‌شدن روی زمین. */
    public void onPlanted(Game game) {}

    /** اثر «غذای گیاه» روی این گیاه. */
    public void onPlantFood(Game game) {}

    public void boost() {
        this.boosted = true;
    }

    // ---------- چرخه‌ی حیات ----------

    public void takeDamage(double dmg) {
        this.hp -= dmg;
    }

    public boolean isDead() {
        return hp <= 0;
    }

    /** ستون خانه (x). مختصات گیاه همیشه عدد صحیح است. */
    public int getCol() { return (int) position.x; }
    /** ردیف خانه (y). */
    public int getRow() { return (int) position.y; }

    // ---------- getter / setter ----------

    public Plants getType() { return type; }
    public void setType(Plants type) { this.type = type; }

    public double getHp() { return hp; }
    public void setHp(double hp) { this.hp = hp; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public Vec2 getPosition() { return position; }
    public void setPosition(Vec2 position) { this.position = position; }

    public double getAttackdamage() { return attackdamage; }
    public void setAttackdamage(double attackdamage) { this.attackdamage = attackdamage; }

    public boolean isBoosted() { return boosted; }
}
