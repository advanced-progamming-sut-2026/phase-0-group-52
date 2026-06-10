package pvz.model.entities;

import pvz.model.Vec2;

public class Projectile {
    private double damage;
    private Vec2 position;
    private Vec2 velocity;

    public Projectile(double damage, Vec2 position, Vec2 velocity) {
        this.damage = damage;
        this.position = position;
        this.velocity = velocity;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public Vec2 getPosition() {
        return position;
    }

    public void setPosition(Vec2 position) {
        this.position = position;
    }

    public Vec2 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vec2 velocity) {
        this.velocity = velocity;
    }
}
