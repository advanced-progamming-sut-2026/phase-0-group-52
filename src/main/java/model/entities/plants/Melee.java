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
        if (getType() == Plants.CHOMPER) { chomp(game); return; }
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
            markStruck();
            front.takeDamage(getAttackdamage());
            PlantCombat.removeDeadZombies(game);
            actionTimer = getActionInterval();
        }
    }

    @Override
    public void onPlantFood(Game game) {
        if (getType() == Plants.CHOMPER) {
            java.util.List<Zombie> ahead = new java.util.ArrayList<Zombie>();
            for (Zombie z : game.getZombies())
                if (!z.isDead() && z.getRow() == getRow() && z.getPosition().x >= getCol())
                    ahead.add(z);
            ahead.sort(new java.util.Comparator<Zombie>() {
                public int compare(Zombie a, Zombie b) {
                    return Double.compare(a.getPosition().x, b.getPosition().x);
                }
            });
            int eaten = 0;
            for (Zombie z : ahead) {
                z.setArmorHp(0);
                z.setHp(0);
                if (++eaten >= 3) break;
            }
            PlantCombat.removeDeadZombies(game);
            actionTimer = 0;
            System.out.println("Chomper devoured " + eaten + " zombie(s) whole!");
            return;
        }
        int hits = 0;
        for (Zombie z : PlantCombat.zombiesInArea(game, getCol(), getRow(), 1)) {
            z.takeDamage(getAttackdamage() * 10);
            hits++;
        }
        PlantCombat.removeDeadZombies(game);
        System.out.println(getType().getName() + " unleashed a 3x3 flurry, hitting " + hits + " zombie(s)!");
    }

    private void strike(Game game) {
        if (getType() == Plants.WASABI_WHIP && game.getField() != null)
            PlantCombat.meltFrozenInRow(game, getRow(), 0);
        if (getType() == Plants.KIWIBEAST && getAttackdamage() < getType().getDamage() * 3)
            setAttackdamage(getAttackdamage() + getType().getDamage() * 0.5);
        boolean aoe = getType().getTags().contains(PlantTag.AOE);
        boolean hit = false;
        markStruck();
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
