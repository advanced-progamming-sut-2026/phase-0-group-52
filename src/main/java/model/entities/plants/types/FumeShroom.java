package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.StrikeThrough;
import model.entities.zombies.Zombie;

public class FumeShroom extends StrikeThrough {

    private static final double RANGE = 4;

    public FumeShroom(Vec2 position) {
        super(Plants.FUME_SHROOM, position);
    }

    @Override
    protected double maxRange() { return RANGE; }

    @Override
    public void onPlantFood(Game game) {
        for (Zombie z : PlantCombat.zombiesInRow(game, getRow())) {
            if (z.getPosition().x < getCol()) continue;
            z.takeDamage(2 * getAttackdamage());
            z.getPosition().x += 2;
        }
        PlantCombat.removeDeadZombies(game);
    }
}
