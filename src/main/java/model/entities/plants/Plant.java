package model.entities.plants;

import model.Game;
import model.Vec2;

public abstract class Plant implements PlantInterface {
    private Plants type;
    private double hp;
    private int price;
    private Vec2 position;
    private double attackdamage;
    private double actionInterval;

    protected double actionTimer = 0;

    protected boolean boosted = false;

    private boolean frozen = false;

    private int freezeLevel = 0;

    private double iceHp = 0;

    public Plant(Plants type, double hp, int price, Vec2 position, double attackdamage) {
        this.type = type;
        this.hp = hp;
        this.price = price;
        this.position = position;
        this.attackdamage = attackdamage;
        this.actionInterval = type.getActionInterval();
    }

    public double getActionInterval() { return actionInterval; }
    public void setActionInterval(double actionInterval) { this.actionInterval = actionInterval; }

    public void onTick(Game game) {}

    public void onPlanted(Game game) {}

    public static final double ACT_SHOW = 1.4d;
    public static final double FED_SHOW = 2.2d;
    public static final double BITE_SHOW = 0.45d;

    private double actingFor;
    private double fedFor;
    private double bittenFor;

    public double attackClipSeconds() {
        PlantRecord record = PlantData.record(getType());
        if (record == null || record.getAnimations() == null) {
            return ACT_SHOW;
        }
        java.util.Map<String, Double> clips = record.getAnimations().getClips();
        for (String name : new String[] {"attack", "special_stage1", "special", "bite"}) {
            Double seconds = clips.get(name);
            if (seconds != null && seconds.doubleValue() > 0d) {
                return seconds.doubleValue();
            }
        }
        return ACT_SHOW;
    }

    public void markStruck() {
        markActedFor(attackClipSeconds());
    }

    public void markActedFor(double seconds) {
        actingFor = Math.max(actingFor, seconds);
    }

    public void markActed() {
        actingFor = ACT_SHOW;
    }

    public void markFedFor(double seconds) {
        fedFor = seconds;
    }

    public void markFed() {
        fedFor = FED_SHOW;
    }

    public void markBitten() {
        bittenFor = BITE_SHOW;
    }

    public boolean isActing() {
        return actingFor > 0d;
    }

    public double fedRemaining() {
        return fedFor;
    }

    public boolean isFed() {
        return fedFor > 0d;
    }

    public boolean isBitten() {
        return bittenFor > 0d;
    }

    public boolean isFading() {
        return false;
    }

    public int growthStage() {
        return 1;
    }

    public void ageStatesTick() {
        double step = model.Game.SECONDS_PER_TICK;
        actingFor = Math.max(0d, actingFor - step);
        fedFor = Math.max(0d, fedFor - step);
        bittenFor = Math.max(0d, bittenFor - step);
    }

    public void onPlantFood(Game game) {
        markFed();
    }

    public void onDeath(Game game) {}

    public void boost() {
        this.boosted = true;
    }

    public void takeDamage(double dmg) {
        this.hp -= dmg;
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public int getCol() { return (int) position.x; }

    public int getRow() { return (int) position.y; }

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

    public boolean isFrozen() { return frozen; }

    public boolean isFrozenSolid() { return freezeLevel >= FREEZE_STAGES; }

    public static final int FREEZE_STAGES = 3;
    public void setFrozen(boolean frozen) { this.frozen = frozen; }

    public int getFreezeLevel() { return freezeLevel; }
    public double getIceHp() { return iceHp; }

    public boolean resistsCold() {
        return getType().getTags().contains(PlantTag.FIRE);
    }

    public void thawCompletely() {
        freezeLevel = 0;
        frozen = false;
        iceHp = 0;
    }

    public void addFreezeLevel() {
        if (resistsCold()) {
            return;
        }
        if (freezeLevel >= 3) return;
        freezeLevel++;
        if (freezeLevel >= 3) {
            frozen = true;
            iceHp = 600;
        }
    }

    public void damageIce(double dmg) {
        if (!frozen || iceHp <= 0) return;
        iceHp -= dmg;
        if (iceHp <= 0) thaw();
    }

    public void thaw() {
        iceHp = 0;
        freezeLevel = 0;
        frozen = false;
    }
}
