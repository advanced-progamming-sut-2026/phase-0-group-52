package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.Plants;
import model.entities.plants.StrikeThrough;

public class Cactus extends StrikeThrough {

    public Cactus(Vec2 position) {
        super(Plants.CACTUS, position);
    }

    @Override
    protected int maxTargets() { return 3; }

    @Override
    public void onPlantFood(Game game) {

        pierce(game, Integer.MAX_VALUE, Double.MAX_VALUE, 2 * getAttackdamage());
    }
}
