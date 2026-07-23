package model.entities.plants;

import model.Game;
import model.Vec2;
import model.entities.zombies.Zombie;

public class Shooter extends Plant {

    public Shooter(Plants type, Vec2 position) {
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
            shoot(game);
        }
    }

    protected void shoot(Game game) {
        Zombie target = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (target == null) return;
        target.takeDamage(shotDamage(game));
        PlantCombat.removeDeadZombies(game);
    }

    protected double shotDamage(Game game) {
        double dmg = getAttackdamage();
        if (getType().getTags().contains(PlantTag.PEA))
            dmg *= PlantCombat.torchwoodFactor(game, getRow(), getCol());
        return dmg;
    }
}
