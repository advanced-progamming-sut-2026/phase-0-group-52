package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Plant;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.plants.Shooter;
import pvz.model.entities.zombies.Zombie;

public class SeaShroom extends Shooter {

    private static final double LIFESPAN = 60;
    private static final double RANGE = 3;

    private double lifeTimer = 0;

    public SeaShroom(Vec2 position) {
        super(Plants.SEA_SHROOM, position);
    }

    public void resetLife() { lifeTimer = 0; }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        lifeTimer += 1;
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
            if (p instanceof SeaShroom) ((SeaShroom) p).resetLife();
        PlantCombat.removeDeadZombies(game);
    }
}
