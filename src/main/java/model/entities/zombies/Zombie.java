package model.entities.zombies;

import model.ChapterType;
import model.Game;
import model.Vec2;

public abstract class Zombie {
    private double hp;
    private double speed;
    private double damage;
    private int line;
    private Vec2 position;
    private ArmorType armorType;
    private double armorHp;
    private ChapterType chapter;
    private ZombieType type;
    private Zombies origin;
    private ZombieState state;
    private ZombieAbility ability;

    protected double eatTimer = 0;

    private boolean slowed = false;

    private boolean hypnotized = false;

    private int frozenTicks = 0;
    private double preFreezeSpeed = 0;

    public boolean isFrozenSolid() { return frozenTicks > 0; }

    public void freezeFor(int ticks) {
        if (frozenTicks <= 0) preFreezeSpeed = speed;
        frozenTicks = Math.max(frozenTicks, ticks);
        speed = 0;
        state = ZombieState.DISABLED;
    }

    public void advanceFreeze() {
        if (frozenTicks > 0) {
            frozenTicks--;
            if (frozenTicks == 0) {
                speed = preFreezeSpeed;
                state = ZombieState.WALKING;
            }
        }
    }

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

    public abstract void onTick(Game game);

    public void onDeath(Game game) {}

    public void move(Game game) {
        position.x -= speed * model.Game.SECONDS_PER_TICK;
    }

    public void takeDamage(double dmg) {
        if (armorHp > 0) {
            double absorbed = Math.min(armorHp, dmg);
            armorHp -= absorbed;
            dmg -= absorbed;
        }
        if (dmg > 0) hp -= dmg;
    }

    public void takeDirectDamage(double dmg) {
        hp -= dmg;
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public int getCol() { return (int) Math.floor(position.x); }

    public int getRow() { return line; }

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

    public String getDisplayName() {
        if (armorType != null && armorType != ArmorType.DEFAULT) {
            switch (armorType) {
                case CONEHEAD:   return "Conehead Zombie";
                case BUCKETHEAD: return "Buckethead Zombie";
                case BRICKHEAD:  return "Brickhead Zombie";
                case KNIGHT:     return "Knight Zombie";
                case ICEBLOCK:   return "Iceblock Zombie";
                default:         break;
            }
        }
        String s = getClass().getSimpleName();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (i > 0 && Character.isUpperCase(c) && !Character.isUpperCase(s.charAt(i - 1))) b.append(' ');
            b.append(c);
        }
        return b.toString();
    }

    public ChapterType getChapter() { return chapter; }
    public void setChapter(ChapterType chapter) { this.chapter = chapter; }

    public Zombies getOrigin() { return origin; }
    public void setOrigin(Zombies origin) { this.origin = origin; }

    public String getAlias() {
        ZombieRecord record = ZombieData.of(origin);
        return record == null ? null : record.getAlias();
    }

    public ZombieType getType() { return type; }
    public void setType(ZombieType type) { this.type = type; }

    public ZombieState getState() { return state; }
    public void setState(ZombieState state) { this.state = state; }

    public ZombieAbility getAbility() { return ability; }
    public void setAbility(ZombieAbility ability) { this.ability = ability; }

    public boolean isSlowed() { return slowed; }
    public void setSlowed(boolean slowed) { this.slowed = slowed; }

    public boolean isHypnotized() { return hypnotized; }
    public void setHypnotized(boolean hypnotized) { this.hypnotized = hypnotized; }
}
