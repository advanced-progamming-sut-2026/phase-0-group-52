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
        actionTimer += model.Game.SECONDS_PER_TICK;
        double interval = getActionInterval();
        if (interval <= 0) interval = 2.9;
        while (actionTimer >= interval) {
            actionTimer -= interval;
            lob(game);
        }
    }

    private void lob(Game game) {
        if (getType() == Plants.PEPPER_PULT && game.getField() != null)
            PlantCombat.meltFrozenInRow(game, getRow(), 0);
        Zombie target = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (target == null) return;
        if (deflects(target)) return;
        boolean ice = getType().getTags().contains(PlantTag.ICE);
        target.takeDamage(getAttackdamage());
        if (ice) PlantCombat.slow(target);
        if (getType() == Plants.KERNEL_PULT && PlantCombat.RANDOM.nextDouble() < 0.25)
            PlantCombat.slow(target);
        if (getType().getTags().contains(PlantTag.AOE)) {
            for (Zombie z : PlantCombat.zombiesInRow(game, getRow()))
                if (z != target && !deflects(z) && Math.abs(z.getPosition().x - target.getPosition().x) <= 1) {
                    z.takeDamage(getAttackdamage() / 2);
                    if (ice) PlantCombat.slow(z);
                }
        }
        PlantCombat.removeDeadZombies(game);
    }

    private boolean deflects(Zombie z) {
        return z instanceof model.entities.zombies.types.ParasolZombie;
    }
}
