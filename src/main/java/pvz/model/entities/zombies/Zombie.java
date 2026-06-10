package pvz.model.entities.zombies;

import pvz.model.Vec2;
import pvz.model.enums.*;

public abstract class Zombie {
    private double hp;
    private double speed;
    private double damage;
    private int line;
    private Vec2 position;
    private ArmorType armorType;
    private ChapterType chapter;
    private ZombieType type;
    private ZombieState state;
    private ZombieAbility ability;

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

    public double getHp() {
        return hp;
    }

    public void setHp(double hp) {
        this.hp = hp;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public Vec2 getPosition() {return position;}

    public void setX(Vec2 position) {
        this.position = position;
    }



    public ArmorType getArmorType() {
        return armorType;
    }

    public void setArmorType(ArmorType armorType) {
        this.armorType = armorType;
    }

    public ChapterType getChapter() {
        return chapter;
    }

    public void setChapter(ChapterType chapter) {
        this.chapter = chapter;
    }

    public ZombieType getType() {
        return type;
    }

    public void setType(ZombieType type) {
        this.type = type;
    }

    public ZombieState getState() {
        return state;
    }

    public void setState(ZombieState state) {
        this.state = state;
    }
}
