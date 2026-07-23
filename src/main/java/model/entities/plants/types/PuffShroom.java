package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.Plant;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;
import model.entities.zombies.Zombie;

public class PuffShroom extends Shooter {

    private static final double LIFESPAN = 60;
    private static final double RANGE = 3;

    private double lifeTimer = 0;

    public PuffShroom(Vec2 position) {
        super(Plants.PUFF_SHROOM, position);
    }

    public void resetLife() { lifeTimer = 0; }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        lifeTimer += model.Game.SECONDS_PER_TICK;
        if (lifeTimer >= LIFESPAN) {
            setHp(0);
            PlantCombat.removePlant(game, this);
            return;
        }
        super.onTick(game);
    }

    @Override
    protected void shoot(Game game) {
        Zombie target = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (target == null || target.getPosition().x - getCol() > RANGE) return;
        target.takeDamage(shotDamage(game));
        PlantCombat.removeDeadZombies(game);
    }

    @Override
    public void onPlantFood(Game game) {
        Zombie target = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (target != null) target.takeDamage(30 * shotDamage(game));
        for (Plant p : game.getPlants())
            if (p instanceof PuffShroom) ((PuffShroom) p).resetLife();
        PlantCombat.removeDeadZombies(game);
    }
}
