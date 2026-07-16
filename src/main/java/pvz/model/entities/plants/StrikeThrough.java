package pvz.model.entities.plants;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.zombies.Zombie;

public class StrikeThrough extends Plant {

    public StrikeThrough(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        actionTimer += 1;
        double interval = getType().getActionInterval();
        if (interval <= 0) interval = 1;
        while (actionTimer >= interval) {
            actionTimer -= interval;
            pierce(game, maxTargets(), maxRange(), getAttackdamage());
        }
    }

    protected int maxTargets() { return Integer.MAX_VALUE; }

    protected double maxRange() { return Double.MAX_VALUE; }

    protected void pierce(Game game, int targets, double range, double dmg) {
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
