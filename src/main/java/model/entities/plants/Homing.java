package model.entities.plants;

import model.Game;
import model.Vec2;
import model.entities.zombies.ArmorType;
import model.entities.zombies.Zombie;

public class Homing extends Plant {

    public Homing(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        actionTimer += model.Game.SECONDS_PER_TICK;
        double interval = getActionInterval();
        if (interval <= 0) interval = 1.5;
        while (actionTimer >= interval) {
            actionTimer -= interval;
            fire(game);
        }
    }

    private void fire(Game game) {
        if (getType() == Plants.MAGNET_SHROOM) {
            Zombie armored = nearestArmored(game);
            if (armored != null) {
                armored.setArmorHp(0);
                armored.setArmorType(ArmorType.DEFAULT);
                System.out.println(getType().getName() + " stripped the armor off a zombie in row "
                        + (armored.getRow() + 1) + ".");
            }
            return;
        }
        Zombie target = nearest(game);
        if (target == null) return;
        if (getType() == Plants.CAULIPOWER) {
            target.setHypnotized(true);
            return;
        }
        target.takeDamage(getAttackdamage());
        PlantCombat.removeDeadZombies(game);
    }

    private Zombie nearest(Game game) {
        Zombie best = null;
        double bestDist = Double.MAX_VALUE;
        for (Zombie z : game.getZombies()) {
            if (z.isDead() || z.isHypnotized()) continue;
            double d = dist(z);
            if (d < bestDist) {
                bestDist = d;
                best = z;
            }
        }
        return best;
    }

    private Zombie nearestArmored(Game game) {
        Zombie best = null;
        double bestDist = Double.MAX_VALUE;
        for (Zombie z : game.getZombies()) {
            if (z.isDead() || z.getArmorType() == null || z.getArmorType() == ArmorType.DEFAULT) continue;
            double d = dist(z);
            if (d < bestDist) {
                bestDist = d;
                best = z;
            }
        }
        return best;
    }

    private double dist(Zombie z) {
        double dx = z.getPosition().x - getCol();
        double dy = z.getRow() - getRow();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
