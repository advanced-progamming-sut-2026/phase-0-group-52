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
        actionTimer += 1;
        double interval = getType().getActionInterval();
        if (interval <= 0) interval = 1;
        while (actionTimer >= interval) {
            actionTimer -= interval;
            strike(game);
        }
    }

    private void strike(Game game) {
        boolean aoe = getType().getTags().contains(PlantTag.AOE);
        int radius = aoe ? 1 : 0;
        boolean hit = false;
        for (Zombie z : PlantCombat.zombiesInArea(game, getCol(), getRow(), radius)) {
            if (!aoe && Math.abs(z.getPosition().x - getCol()) > 1) continue;
            z.takeDamage(getAttackdamage());
            hit = true;
        }
        if (hit) PlantCombat.removeDeadZombies(game);
    }
}
