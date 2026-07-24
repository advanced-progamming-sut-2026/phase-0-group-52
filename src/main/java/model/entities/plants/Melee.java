package model.entities.plants;

import model.Game;
import model.Vec2;
import model.entities.zombies.Zombie;

public class Melee extends Plant {

    public Melee(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        if (getType() == Plants.CHOMPER) {
            chomp(game);
            return;
        }
        actionTimer += model.Game.SECONDS_PER_TICK;
        double interval = getActionInterval();
        if (interval <= 0) interval = 1;
        while (actionTimer >= interval) {
            actionTimer -= interval;
            strike(game);
        }
    }

    private void chomp(Game game) {
        if (actionTimer > 0) {
            actionTimer -= model.Game.SECONDS_PER_TICK;
            return;
        }
        Zombie front = null;
        for (Zombie z : game.getZombies()) {
            if (z.isDead() || z.getRow() != getRow()) continue;
            double dx = z.getPosition().x - getCol();
            if (dx >= 0 && dx <= 1.5 && (front == null || z.getPosition().x < front.getPosition().x))
                front = z;
        }
        if (front != null) {
            front.takeDamage(getAttackdamage());
            PlantCombat.removeDeadZombies(game);
            actionTimer = getActionInterval();
        }
    }

    private void strike(Game game) {
        if (getType() == Plants.WASABI_WHIP && game.getField() != null)
            PlantCombat.meltFrozenInRow(game, getRow(), 0);
        if (getType() == Plants.KIWIBEAST && getAttackdamage() < getType().getDamage() * 3)
            setAttackdamage(getAttackdamage() + getType().getDamage() * 0.5);
        boolean aoe = getType().getTags().contains(PlantTag.AOE);
        boolean hit = false;
        if (aoe) {
            for (Zombie z : PlantCombat.zombiesInArea(game, getCol(), getRow(), 1)) {
                z.takeDamage(getAttackdamage());
                hit = true;
            }
        } else {
            for (Zombie z : game.getZombies()) {
                if (z.isDead() || z.getRow() != getRow()) continue;
                if (Math.abs(z.getCol() - getCol()) <= 1) {
                    z.takeDamage(getAttackdamage());
                    hit = true;
                }
            }
        }
        if (hit) PlantCombat.removeDeadZombies(game);
    }
}
