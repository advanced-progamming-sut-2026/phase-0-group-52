package model.entities.zombies;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.plants.Plant;

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

    /** هر تیک یک‌بار. هر زیرکلاس رفتار خود را اینجا پیاده می‌کند (حرکت/حمله/قابلیت ویژه). */
    public abstract void onTick(Game game);

    /** حرکت به چپ؛ با در نظر گرفتنِ توقف و کندی. speed بر حسب ثانیه است → تقسیم بر تیک‌درثانیه. */
    public void move(Game game) {
        if (stunTimer > 0) { stunTimer -= 1; return; }     // توقف کامل
        double sp = speed;
        if (slowTimer > 0) { sp *= slowFactor; slowTimer -= 1; }
        position.x -= sp / Game.TICKS_PER_SECOND;
    }

    public void applySlow(double ticks, double factor) {
        this.slowTimer = Math.max(this.slowTimer, ticks);
        this.slowFactor = factor;
    }

    public void applyStun(double ticks) {
        this.stunTimer = Math.max(this.stunTimer, ticks);
    }

    public boolean isStunned() { return stunTimer > 0; }

    /** نزدیک‌ترین گیاهِ همان ردیف که زامبی به آن رسیده (فاصله < یک خانه). مشترک بین همه‌ی زیرکلاس‌ها. */
    protected Plant frontPlant(Game game) {
        Plant closest = null;
        double zx = position.x;              // مختصات دقیقِ (اعشاری) زامبی
        double best = Double.MAX_VALUE;
        for (Plant p : game.getPlants()) {
            if (p.getRow() != getRow()) continue;
            double dist = zx - p.getCol();     // فقط گیاهانِ جلو (چپ) یا هم‌محل
            if (dist >= 0 && dist < best) {
                best = dist;
                closest = p;
            }
        }
        return (best < 1.0) ? closest : null;  // فقط وقتی واقعاً رسیده باشد
    }

    /** رفتارِ پایه: اگر گیاهی جلو است بخور (eatDPS در تیک)، وگرنه حرکت کن. مشترک بین زیرکلاس‌ها. */
    protected void walkOrEat(Game game) {
        Plant target = frontPlant(game);
        if (target != null) {
            setState(ZombieState.ATTACKING);
            target.takeDamage(damage / Game.TICKS_PER_SECOND);
        } else {
            setState(ZombieState.WALKING);
            move(game);
        }
    }

    /** جانِ زره بر اساس نوعِ آن (برای مقداردهیِ اولیه توسط ZombieFactory). */
    public static double armorHpFor(ArmorType t) {
        switch (t) {
            case CONEHEAD:
                return 370;    // Cone
            case BUCKETHEAD:
                return 1100;   // Bucket
            case BRICKHEAD:
                return 2200;   // Brick
            case KNIGHT:
                return 3200;   // Shoulder(1600) + Crown(1600)
            default:
                return 0;
        }
    }

    /** آسیب‌زدن: اول به زره، سپس به جان. */
    public void takeDamage(double dmg) {
        if (armorHp > 0) {
            double absorbed = Math.min(armorHp, dmg);
            armorHp -= absorbed;
            dmg -= absorbed;
        }
        if (dmg > 0) hp -= dmg;
    }

    /** آسیب مستقیم به جان (مثل تیر سمی که زره را نادیده می‌گیرد). */
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

    /** آسیبِ پرتاب‌کننده‌ها (Lobber). پیش‌فرض مثل takeDamage؛ Snorkel حتی زیر آب می‌پذیردش. */
    public void takeDamageFromLobber(double dmg) {
        takeDamage(dmg);
    }



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
