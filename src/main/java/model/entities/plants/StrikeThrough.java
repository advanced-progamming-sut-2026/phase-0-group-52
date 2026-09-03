package model.entities.plants;

import model.Game;
import model.Vec2;
import model.entities.zombies.Zombie;

public class StrikeThrough extends Plant {

    public static final double REACH_ALL = 9d;

    public StrikeThrough(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        actionTimer += model.Game.SECONDS_PER_TICK;
        double interval = getActionInterval();
        if (interval <= 0) interval = 1;
        while (actionTimer >= interval) {
            actionTimer -= interval;
            pierce(game, maxTargets(), maxRange(), getAttackdamage());
        }
    }

    public double reach() {
        double range = maxRange();
        return range >= Double.MAX_VALUE / 2d ? REACH_ALL : range;
    }

    protected int maxTargets() { return Integer.MAX_VALUE; }

    protected double maxRange() { return Double.MAX_VALUE; }

    protected void pierce(Game game, int targets, double range, double dmg) {
        markStruck();
        int hitCount = 0;
        for (Zombie z : PlantCombat.zombiesInRow(game, getRow())) {
            if (z.getPosition().x < getCol()) continue;
            if (z.getPosition().x - getCol() > range) continue;
            z.takeDamage(dmg);
            if (++hitCount >= targets) break;
        }
        PlantCombat.removeDeadZombies(game);
    }
}
