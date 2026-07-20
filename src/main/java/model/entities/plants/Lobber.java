package model.entities.plants;

import model.Game;
import model.Vec2;
import model.entities.zombies.Zombie;

public class Lobber extends Plant {

    public Lobber(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        actionTimer += 1;
        double interval = getType().getActionInterval();
        if (interval <= 0) interval = 2.9;
        while (actionTimer >= interval) {
            actionTimer -= interval;
            lob(game);
        }
    }

    private void lob(Game game) {
        Zombie target = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (target == null) return;
        target.takeDamage(getAttackdamage());
        if (getType().getTags().contains(PlantTag.AOE)) {
            for (Zombie z : PlantCombat.zombiesInRow(game, getRow()))
                if (z != target && Math.abs(z.getPosition().x - target.getPosition().x) <= 1)
                    z.takeDamage(getAttackdamage() / 2);
        }
        PlantCombat.removeDeadZombies(game);
    }
}
