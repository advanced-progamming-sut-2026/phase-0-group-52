package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.Plant;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;
import model.entities.zombies.Zombie;

public class SeaShroom extends Shooter {

    private static final double LIFESPAN = 60;
    private static final double RANGE = 3;
    private static final int VOLLEYS = 20;

    private double lifeTimer;

    public SeaShroom(Vec2 position) {
        super(Plants.SEA_SHROOM, position);
    }

    public void resetLife() {
        lifeTimer = 0;
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) {
            return;
        }
        lifeTimer += model.Game.SECONDS_PER_TICK;
        if (lifeTimer >= LIFESPAN) {
            setHp(0);
            PlantCombat.removePlant(game, this);
            return;
        }
        super.onTick(game);
    }

    @Override
    protected boolean hasTarget(Game game) {
        Zombie target = PlantCombat.frontmostAhead(game, getRow(), getCol());
        return target != null && target.getPosition().x - getCol() <= RANGE;
    }

    @Override
    protected int foodVolleys() {
        return VOLLEYS;
    }

    @Override
    protected void onFoodBurst(Game game) {
        for (Plant plant : game.getPlants()) {
            if (plant instanceof SeaShroom) {
                ((SeaShroom) plant).resetLife();
            }
        }
    }
}
