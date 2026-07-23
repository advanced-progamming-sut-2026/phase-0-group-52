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

    public void onPlantFood(Game game) {}

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
    public void setFrozen(boolean frozen) { this.frozen = frozen; }

    public int getFreezeLevel() { return freezeLevel; }
    public double getIceHp() { return iceHp; }

    public void addFreezeLevel() {
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
