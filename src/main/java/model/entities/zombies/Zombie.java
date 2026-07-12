package model.entities.zombies;

import model.ChapterType;
import model.Game;
import model.Vec2;

public abstract class Zombie {
    private double hp;
    private double speed;
    private double damage;          // معادل EatDPS
    private int line;               // ردیف (y)
    private Vec2 position;          // x = ستون (اعشاری چون حرکت پیوسته است)
    private ArmorType armorType;
    private double armorHp;         // جان زره؛ آسیب اول به زره می‌خورد
    private ChapterType chapter;
    private ZombieType type;
    private ZombieState state;
    private ZombieAbility ability;

    /** شمارنده داخلی برای زمان‌بندی خوردن گیاه. */
    protected double eatTimer = 0;

    // وضعیت‌ها (کندی/توقف)
    private double slowTimer = 0;      // تیک‌های باقی‌مانده‌ی کندی
    private double slowFactor = 1.0;   // ضریب سرعت در حین کندی
    private double stunTimer = 0;      // تیک‌های توقف کامل

    public Zombie(double hp, double speed, double damage, int line, Vec2 position, ArmorType armorType,
                  ChapterType chapter, ZombieType type, ZombieState state, ZombieAbility ability) {
        this.hp = hp;
        this.speed = speed;
        this.damage = damage;
        this.line = line;
        this.position = position;
        this.armorType = armorType;
        this.chapter = chapter;
        this.type = type;
        this.state = state;
        this.ability = ability;
    }

    // ---------- قلاب‌های polymorphic که موتور (GameEngine) صدا می‌زند ----------

    /** هر تیک یک‌بار. هر زیرکلاس رفتار خود را اینجا پیاده می‌کند. */
    public abstract void onTick(Game game);

    /** حرکت یک‌خانه‌به‌چپ؛ با در نظر گرفتنِ توقف و کندی. */
    public void move(Game game) {
        if (stunTimer > 0) { stunTimer -= 1; return; }     // توقف کامل
        double sp = speed;
        if (slowTimer > 0) { sp *= slowFactor; slowTimer -= 1; }
        position.x -= sp;
    }

    // ---------- وضعیت‌ها ----------

    public void applySlow(double ticks, double factor) {
        this.slowTimer = Math.max(this.slowTimer, ticks);
        this.slowFactor = factor;
    }

    public void applyStun(double ticks) {
        this.stunTimer = Math.max(this.stunTimer, ticks);
    }

    public boolean isStunned() { return stunTimer > 0; }

    // ---------- آسیب و چرخه‌ی حیات ----------

    /** آسیب‌زدن: اول به زره، سپس به جان. */
    public void takeDamage(double dmg) {
        if (armorHp > 0) {
            double absorbed = Math.min(armorHp, dmg);
            armorHp -= absorbed;
            dmg -= absorbed;
        }
        if (dmg > 0) hp -= dmg;
    }

    /** آسیب مستقیم به جان (تیر سمی/بلع؛ زره را نادیده می‌گیرد). */
    public void takeDirectDamage(double dmg) {
        hp -= dmg;
    }

    public boolean isDead() {
        return hp <= 0;
    }

    /** ستون فعلی (x). */
    public int getCol() { return (int) Math.floor(position.x); }
    /** ردیف (y). */
    public int getRow() { return line; }

    // ---------- getter / setter ----------

    public double getHp() { return hp; }
    public void setHp(double hp) { this.hp = hp; }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }

    public double getDamage() { return damage; }
    public void setDamage(double damage) { this.damage = damage; }

    public int getLine() { return line; }
    public void setLine(int line) { this.line = line; }

    public Vec2 getPosition() { return position; }
    public void setPosition(Vec2 position) { this.position = position; }

    public double getArmorHp() { return armorHp; }
    public void setArmorHp(double armorHp) { this.armorHp = armorHp; }

    public ArmorType getArmorType() { return armorType; }
    public void setArmorType(ArmorType armorType) { this.armorType = armorType; }

    public ChapterType getChapter() { return chapter; }
    public void setChapter(ChapterType chapter) { this.chapter = chapter; }

    public ZombieType getType() { return type; }
    public void setType(ZombieType type) { this.type = type; }

    public ZombieState getState() { return state; }
    public void setState(ZombieState state) { this.state = state; }

    public ZombieAbility getAbility() { return ability; }
    public void setAbility(ZombieAbility ability) { this.ability = ability; }
}
